package com.example.ori20

class MessageStorage {

    private val messages = mutableListOf<Message>()

    fun saveMessage(message: Message) {
        messages.add(message)
    }

    fun getAllMessages(): List<Message> {
        return messages.toList()
    }

    fun getMessageById(messageId: String): Message? {
        return messages.find { it.messageId == messageId }
    }

    fun deleteMessage(messageId: String) {
        messages.removeAll { it.messageId == messageId }
    }

    fun clearMessages() {
        messages.clear()
    }
}