package com.tomatoketchup.ori.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val recipientId: String,
    val body: String,        // encrypted payload / JSON
    val createdAt: Long,     // epoch millis
    val status: Int,         // use MessageStatus
    val ttlSeconds: Int = 3600,
    val hops: Int = 0
)
