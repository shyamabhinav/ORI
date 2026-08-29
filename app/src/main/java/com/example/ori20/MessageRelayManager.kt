package com.example.ori20

class MessageRelayManager {

    fun canRelay(message: Message): Boolean {
        return message.ttl > 0
    }

    fun prepareForRelay(message: Message): Message? {

        if (!canRelay(message)) {
            return null
        }

        message.ttl -= 1
        message.hopCount += 1

        return message
    }
}