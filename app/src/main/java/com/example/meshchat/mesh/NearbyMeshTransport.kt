package com.example.meshchat.mesh

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy

/**
 * Nearby Connections P2P cluster: Bluetooth + Wi-Fi Direct.
 * The router on top of this is what turns one-hop links into a mesh.
 */
internal class NearbyMeshTransport(
    context: Context,
    private val identity: MeshIdentity,
    private val onPayload: (fromEndpointId: String, message: MeshMessage) -> Unit,
    private val onPeersChanged: (List<MeshPeer>) -> Unit,
    private val onStatus: (String) -> Unit,
    private val onError: (String) -> Unit
) : MeshTransport {

    private val client: ConnectionsClient = Nearby.getConnectionsClient(context.applicationContext)
    private val peers = linkedMapOf<String, MeshPeer>()
    private val pendingNames = mutableMapOf<String, String>()

    override fun start() {
        advertise()
        discover()
    }

    override fun stop() {
        client.stopAdvertising()
        client.stopDiscovery()
        client.stopAllEndpoints()
        peers.clear()
        pendingNames.clear()
        onPeersChanged(emptyList())
    }

    override fun send(message: MeshMessage, excludeEndpointId: String?) {
        val bytes = Payload.fromBytes(message.toJson().toByteArray(Charsets.UTF_8))
        val targets = peers.keys.filter { it != excludeEndpointId }
        if (targets.isEmpty()) {
            onStatus("No nearby phones to carry this message yet")
            return
        }
        targets.forEach { endpointId ->
            client.sendPayload(endpointId, bytes)
        }
    }

    override fun connectedPeers(): List<MeshPeer> = peers.values.toList()

    private fun advertise() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        client.startAdvertising(
            identity.endpointName(),
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            onStatus("Advertising mesh identity")
        }.addOnFailureListener { error ->
            onError("Advertise failed: ${error.message}")
        }
    }

    private fun discover() {
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        client.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            options
        ).addOnSuccessListener {
            onStatus("Looking for nearby MeshChat phones")
        }.addOnFailureListener { error ->
            onError("Discovery failed: ${error.message}")
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            pendingNames[endpointId] = info.endpointName
            client.requestConnection(
                identity.endpointName(),
                endpointId,
                connectionLifecycleCallback
            ).addOnFailureListener { error ->
                Log.w(TAG, "requestConnection: ${error.message}")
            }
        }

        override fun onEndpointLost(endpointId: String) {
            pendingNames.remove(endpointId)
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            pendingNames[endpointId] = info.endpointName
            // Hackathon: auto-accept so phones form a mesh without a pairing UI.
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                val rawName = pendingNames[endpointId] ?: "Unknown|$endpointId"
                val (displayName, userId) = MeshIdentity.parseEndpointName(rawName)
                peers[endpointId] = MeshPeer(userId, displayName, endpointId, connected = true)
                onPeersChanged(connectedPeers())
                onStatus("Linked with $displayName")
            } else {
                pendingNames.remove(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            val gone = peers.remove(endpointId)
            pendingNames.remove(endpointId)
            onPeersChanged(connectedPeers())
            if (gone != null) {
                onStatus("${gone.displayName} left radio range")
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            try {
                val message = MeshMessage.fromJson(String(bytes, Charsets.UTF_8))
                onPayload(endpointId, message)
            } catch (error: Exception) {
                Log.w(TAG, "Bad payload", error)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    companion object {
        private const val TAG = "NearbyMesh"
        const val SERVICE_ID = "com.example.meshchat.MESH"
        private val STRATEGY = Strategy.P2P_CLUSTER
    }
}
