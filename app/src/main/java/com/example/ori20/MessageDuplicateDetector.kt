package com.example.ori20

class MessageDuplicateDetector {

    private val receivedMessageIds = mutableSetOf<String>()

    fun isDuplicate(messageId: String): Boolean {
        return !receivedMessageIds.add(messageId)
    }

    fun clear() {
        receivedMessageIds.clear()
    }
}