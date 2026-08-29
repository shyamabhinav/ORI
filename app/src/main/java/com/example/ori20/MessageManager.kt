package com.example.ori20

class MessageManager {

    private val storage = MessageStorage()
    private val duplicateDetector = MessageDuplicateDetector()
    private val relayManager = MessageRelayManager()

    fun createMessage(
        senderId: String,
        receiverId: String?,
        content: String,
        ttl: Int = 5
    ): Message {

        return Message(
            messageId = MessageIdGenerator.generateId(),
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis(),
            ttl = ttl,
            hopCount = 0
        )
    }

    fun sendMessage(message: Message) {
        storage.saveMessage(message)
    }

    fun receiveMessage(message: Message): Boolean {

        if (duplicateDetector.isDuplicate(message.messageId)) {
            return false
        }

        storage.saveMessage(message)
        return true
    }

    fun prepareMessageForRelay(message: Message): Message? {
        return relayManager.prepareForRelay(message)
    }

    fun getAllMessages(): List<Message> {
        return storage.getAllMessages()
    }

    fun getMessageById(messageId: String): Message? {
        return storage.getMessageById(messageId)
    }
}