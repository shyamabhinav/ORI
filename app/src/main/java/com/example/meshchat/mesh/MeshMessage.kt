package com.example.meshchat.mesh

import org.json.JSONObject
import java.util.UUID

enum class MeshMessageType {
    CHAT,
    EMERGENCY,
    ANNOUNCE
}

data class MeshMessage(
    val id: String = UUID.randomUUID().toString(),
    val type: MeshMessageType = MeshMessageType.CHAT,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val ttl: Int = DEFAULT_TTL,
    val hop: Int = 0
) {
    val isBroadcast: Boolean get() = recipientId == BROADCAST_ID
    val isEmergency: Boolean get() = type == MeshMessageType.EMERGENCY
    val isAnnounce: Boolean get() = type == MeshMessageType.ANNOUNCE

    fun hopLabel(): String = when {
        hop <= 0 -> "Direct Bluetooth (no hop)"
        hop == 1 -> "Hopped through 1 phone"
        else -> "Hopped through $hop phones"
    }

    fun belongsToThread(localUserId: String, peerId: String): Boolean {
        if (type == MeshMessageType.EMERGENCY || type == MeshMessageType.ANNOUNCE) return false
        if (peerId == BROADCAST_ID) {
            return isBroadcast
        }
        return (senderId == peerId && (recipientId == localUserId || isBroadcast)) ||
            (senderId == localUserId && recipientId == peerId)
    }

    fun forRelay(): MeshMessage? {
        if (ttl <= 1) return null
        return copy(ttl = ttl - 1, hop = hop + 1)
    }

    fun toJson(): String = JSONObject().apply {
        put("id", id)
        put("type", type.name)
        put("senderId", senderId)
        put("senderName", senderName)
        put("recipientId", recipientId)
        put("body", body)
        put("createdAt", createdAt)
        put("ttl", ttl)
        put("hop", hop)
    }.toString()

    companion object {
        const val BROADCAST_ID = "*"
        const val DEFAULT_TTL = 6
        const val EMERGENCY_TTL = 10

        fun fromJson(raw: String): MeshMessage {
            val json = JSONObject(raw)
            return MeshMessage(
                id = json.getString("id"),
                type = runCatching {
                    MeshMessageType.valueOf(json.optString("type", MeshMessageType.CHAT.name))
                }.getOrDefault(MeshMessageType.CHAT),
                senderId = json.getString("senderId"),
                senderName = json.optString("senderName", "Unknown"),
                recipientId = json.getString("recipientId"),
                body = json.optString("body", ""),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                ttl = json.optInt("ttl", DEFAULT_TTL),
                hop = json.optInt("hop", 0)
            )
        }
    }
}
