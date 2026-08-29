package com.example.meshchat.mesh

data class MeshPeer(
    val userId: String,
    val displayName: String,
    val endpointId: String = "",
    val connected: Boolean = false,
    val hopCount: Int = 0,
    val lastSeen: Long = System.currentTimeMillis()
) {
    fun statusLabel(): String {
        val shortId = userId.take(8)
        return when {
            connected && hopCount <= 0 -> "Direct Bluetooth · id $shortId"
            hopCount > 0 -> "Out of range · via $hopCount hop(s) · id $shortId"
            else -> "On mesh · id $shortId"
        }
    }
}

sealed class MeshEvent {
    data class Status(val text: String) : MeshEvent()
    data class Incoming(val message: MeshMessage) : MeshEvent()
    data class PeerChanged(val peers: List<MeshPeer>) : MeshEvent()
    data class Error(val text: String) : MeshEvent()
}

data class RelayDecision(
    val deliverLocally: Boolean,
    val relayCopy: MeshMessage?
)

class MessageRouter(
    private val localUserId: String,
    private val seenLimit: Int = 500
) {
    private val seenIds = LinkedHashSet<String>()

    fun decide(message: MeshMessage): RelayDecision {
        if (!markUnseen(message.id)) {
            return RelayDecision(deliverLocally = false, relayCopy = null)
        }

        val forMe = message.isBroadcast ||
            message.isEmergency ||
            message.isAnnounce ||
            message.recipientId == localUserId

        val shouldRelay = message.isBroadcast ||
            message.isEmergency ||
            message.isAnnounce ||
            message.recipientId != localUserId

        val relayCopy = if (shouldRelay) message.forRelay() else null
        return RelayDecision(deliverLocally = forMe, relayCopy = relayCopy)
    }

    private fun markUnseen(id: String): Boolean {
        if (!seenIds.add(id)) return false
        if (seenIds.size > seenLimit) {
            val iterator = seenIds.iterator()
            if (iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            }
        }
        return true
    }
}
