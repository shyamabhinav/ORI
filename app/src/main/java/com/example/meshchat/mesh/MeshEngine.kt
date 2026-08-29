package com.example.meshchat.mesh

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Public backend API for the UI team.
 */
object MeshEngine {

    private const val ANNOUNCE_EVERY_MS = 8_000L
    private const val PEER_STALE_MS = 45_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var appContext: Context
    private lateinit var identityStore: MeshIdentity
    private lateinit var router: MessageRouter

    private var transport: NearbyMeshTransport? = null
    private var emergencyJob: Job? = null
    private var announceJob: Job? = null
    private var emergencyBody: String = "EMERGENCY — I need help"

    private val inbox = CopyOnWriteArrayList<MeshMessage>()
    private val directory = linkedMapOf<String, MeshPeer>()

    private val _identity = MutableStateFlow(MeshIdentity("", "Loading"))
    val identity: StateFlow<MeshIdentity> = _identity.asStateFlow()

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _emergencyMode = MutableStateFlow(false)
    val emergencyMode: StateFlow<Boolean> = _emergencyMode.asStateFlow()

    private val _peers = MutableStateFlow<List<MeshPeer>>(emptyList())
    val peers: StateFlow<List<MeshPeer>> = _peers.asStateFlow()

    private val _messages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val messages: StateFlow<List<MeshMessage>> = _messages.asStateFlow()

    private val _events = MutableSharedFlow<MeshEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<MeshEvent> = _events.asSharedFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        identityStore = MeshIdentity.load(appContext)
        _identity.value = identityStore
        router = MessageRouter(identityStore.userId)
    }

    fun setDisplayName(name: String) {
        MeshIdentity.saveDisplayName(appContext, name)
        identityStore = MeshIdentity.load(appContext)
        _identity.value = identityStore
    }

    fun startMesh() {
        if (!MeshPermissions.hasAll(appContext)) {
            emit(MeshEvent.Error("Grant Bluetooth, Nearby, and Location permissions first"))
            return
        }
        val intent = Intent(appContext, MeshForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
        _running.value = true
    }

    fun stop() {
        setEmergencyMode(false)
        _running.value = false
        stopTransport()
        appContext.stopService(Intent(appContext, MeshForegroundService::class.java))
    }

    internal fun startTransport() {
        stopTransport()
        identityStore = MeshIdentity.load(appContext)
        _identity.value = identityStore
        router = MessageRouter(identityStore.userId)
        transport = NearbyMeshTransport(
            context = appContext,
            identity = identityStore,
            onPayload = { endpointId, message -> handleIncoming(endpointId, message) },
            onPeersChanged = { nearby -> mergeDirectPeers(nearby) },
            onStatus = { emit(MeshEvent.Status(it)) },
            onError = { emit(MeshEvent.Error(it)) }
        ).also { it.start() }
        _running.value = true
        startAnnouncing()
        emit(MeshEvent.Status("Mesh radio started"))
    }

    internal fun stopTransport() {
        announceJob?.cancel()
        announceJob = null
        transport?.stop()
        transport = null
        directory.clear()
        publishPeers()
        emit(MeshEvent.Status("Mesh radio stopped"))
    }

    fun sendChat(text: String, recipientId: String = MeshMessage.BROADCAST_ID) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val message = MeshMessage(
            type = MeshMessageType.CHAT,
            senderId = identityStore.userId,
            senderName = identityStore.displayName,
            recipientId = recipientId,
            body = trimmed,
            ttl = MeshMessage.DEFAULT_TTL
        )
        publishLocal(message)
        transport?.send(message)
    }

    fun sendEmergency(text: String = emergencyBody) {
        val trimmed = text.trim().ifBlank { "EMERGENCY — I need help" }
        emergencyBody = trimmed
        val message = MeshMessage(
            type = MeshMessageType.EMERGENCY,
            senderId = identityStore.userId,
            senderName = identityStore.displayName,
            recipientId = MeshMessage.BROADCAST_ID,
            body = trimmed,
            ttl = MeshMessage.EMERGENCY_TTL
        )
        publishLocal(message)
        transport?.send(message)
        emit(MeshEvent.Status("Emergency packet flooded into the mesh"))
    }

    fun setEmergencyMode(enabled: Boolean, message: String = emergencyBody) {
        _emergencyMode.value = enabled
        emergencyJob?.cancel()
        emergencyJob = null
        if (!enabled) return
        emergencyBody = message.trim().ifBlank { emergencyBody }
        sendEmergency(emergencyBody)
        emergencyJob = scope.launch {
            while (isActive && _emergencyMode.value) {
                delay(15_000)
                sendEmergency(emergencyBody)
            }
        }
    }

    private fun startAnnouncing() {
        announceJob?.cancel()
        announceJob = scope.launch {
            while (isActive) {
                sendAnnounce()
                pruneStalePeers()
                delay(ANNOUNCE_EVERY_MS)
            }
        }
    }

    private fun sendAnnounce() {
        val message = MeshMessage(
            type = MeshMessageType.ANNOUNCE,
            senderId = identityStore.userId,
            senderName = identityStore.displayName,
            recipientId = MeshMessage.BROADCAST_ID,
            body = identityStore.userId,
            ttl = MeshMessage.DEFAULT_TTL
        )
        transport?.send(message)
    }

    private fun handleIncoming(fromEndpointId: String, message: MeshMessage) {
        val decision = router.decide(message)
        if (message.isAnnounce) {
            if (decision.deliverLocally && message.senderId != identityStore.userId) {
                rememberAnnounced(message)
            }
            decision.relayCopy?.let { transport?.send(it, excludeEndpointId = fromEndpointId) }
            return
        }
        if (decision.deliverLocally) {
            publishLocal(message)
            emit(MeshEvent.Incoming(message))
        }
        val relay = decision.relayCopy ?: return
        transport?.send(relay, excludeEndpointId = fromEndpointId)
    }

    private fun rememberAnnounced(message: MeshMessage) {
        val existing = directory[message.senderId]
        if (existing?.connected == true && existing.hopCount <= 0) {
            directory[message.senderId] = existing.copy(
                displayName = message.senderName,
                lastSeen = System.currentTimeMillis()
            )
        } else {
            directory[message.senderId] = MeshPeer(
                userId = message.senderId,
                displayName = message.senderName,
                connected = message.hop <= 0,
                hopCount = message.hop,
                lastSeen = System.currentTimeMillis()
            )
        }
        publishPeers()
    }

    private fun mergeDirectPeers(nearby: List<MeshPeer>) {
        val nearbyIds = nearby.map { it.userId }.toSet()
        nearby.forEach { peer ->
            directory[peer.userId] = peer.copy(
                connected = true,
                hopCount = 0,
                lastSeen = System.currentTimeMillis()
            )
        }
        val drop = directory.filter { (id, peer) ->
            peer.connected && peer.hopCount <= 0 && id !in nearbyIds &&
                System.currentTimeMillis() - peer.lastSeen > PEER_STALE_MS
        }.keys.toList()
        drop.forEach { directory.remove(it) }
        directory.keys.filter { it !in nearbyIds }.forEach { id ->
            val peer = directory[id] ?: return@forEach
            if (peer.connected && peer.hopCount <= 0) {
                directory[id] = peer.copy(connected = false)
            }
        }
        publishPeers()
        emit(MeshEvent.PeerChanged(_peers.value))
    }

    private fun pruneStalePeers() {
        val now = System.currentTimeMillis()
        val stale = directory.filter { (_, peer) ->
            !peer.connected && now - peer.lastSeen > PEER_STALE_MS
        }.keys
        stale.forEach { directory.remove(it) }
        if (stale.isNotEmpty()) publishPeers()
    }

    private fun publishPeers() {
        _peers.value = directory.values
            .filter { it.userId != identityStore.userId }
            .sortedWith(compareByDescending<MeshPeer> { it.connected }.thenBy { it.hopCount })
    }

    private fun publishLocal(message: MeshMessage) {
        if (message.isAnnounce) return
        if (message.isEmergency) {
            val old = inbox.filter { it.isEmergency && it.senderId == message.senderId }
            inbox.removeAll(old)
        }
        if (inbox.any { it.id == message.id }) return
        inbox.add(0, message)
        _messages.value = inbox.toList()
    }

    private fun emit(event: MeshEvent) {
        _events.tryEmit(event)
    }
}
