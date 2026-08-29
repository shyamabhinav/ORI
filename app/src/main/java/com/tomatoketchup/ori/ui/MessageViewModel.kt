package com.tomatoketchup.ori.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatoketchup.ori.data.repo.MessageRepository
import com.tomatoketchup.ori.data.room.MessageEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MessageViewModel(
    private val repo: MessageRepository,
    private val api: com.tomatoketchup.ori.network.MessageApi
) : ViewModel() {

    fun incomingFor(recipientId: String) =
        repo.incomingFor(recipientId)

    fun sendMessage(senderId: String, recipientId: String, body: String) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = MessageEntity(id, senderId, recipientId, body, now, com.tomatoketchup.ori.data.room.MessageStatus.QUEUED)

        viewModelScope.launch {
            repo.enqueue(entity)

            // Try server send; ignore failures (device may be offline)
            try {
                val payload = mapOf(
                    "id" to id,
                    "sender_id" to senderId,
                    "recipient_id" to recipientId,
                    "body" to body,
                    "created_at" to now
                )
                api.sendMessage(payload)
            } catch (e: Exception) {
                // offline — the NearbyRelayService will handle mesh broadcast
            }
        }
    }

    fun markDelivered(id: String) {
        viewModelScope.launch {
            repo.markDelivered(id)
            try {
                api.ack(mapOf("id" to id))
            } catch (_: Exception) {}
        }
    }
}
