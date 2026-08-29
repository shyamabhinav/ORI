package com.example.ori20

data class Message(
    val messageId: String,
    val senderId: String,
    val receiverId: String?,
    val content: String,
    val timestamp: Long,
    var ttl: Int,
    var hopCount: Int
)