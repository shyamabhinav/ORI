package com.tomatoketchup.ori.data.repo

import com.tomatoketchup.ori.data.room.MessageDao
import com.tomatoketchup.ori.data.room.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageRepository(
    private val dao: MessageDao,
    // api client can be injected later for server send/ack
) {
    suspend fun enqueue(message: MessageEntity) {
        dao.insert(message)
        // TODO: attempt network send or enqueue for mesh broadcast
    }

    fun incomingFor(recipientId: String): Flow<List<MessageEntity>> =
        dao.getForRecipient(recipientId)

    suspend fun markDelivered(id: String) {
        dao.updateStatus(id, com.tomatoketchup.ori.data.room.MessageStatus.DELIVERED)
    }
}
