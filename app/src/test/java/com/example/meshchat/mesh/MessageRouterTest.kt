package com.example.meshchat.mesh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageRouterTest {

    private val router = MessageRouter(localUserId = "me")

    @Test
    fun deliversDirectMessageAndDoesNotRelay() {
        val message = MeshMessage(
            senderId = "alice",
            senderName = "Alice",
            recipientId = "me",
            body = "hello"
        )
        val decision = router.decide(message)
        assertTrue(decision.deliverLocally)
        assertNull(decision.relayCopy)
    }

    @Test
    fun relaysMessageMeantForSomeoneElse() {
        val message = MeshMessage(
            senderId = "alice",
            senderName = "Alice",
            recipientId = "bob",
            body = "hello bob",
            ttl = 4
        )
        val decision = router.decide(message)
        assertFalse(decision.deliverLocally)
        assertNotNull(decision.relayCopy)
        assertEquals(3, decision.relayCopy?.ttl)
        assertEquals(1, decision.relayCopy?.hop)
    }

    @Test
    fun ignoresDuplicatePackets() {
        val message = MeshMessage(
            id = "same",
            senderId = "alice",
            senderName = "Alice",
            recipientId = MeshMessage.BROADCAST_ID,
            body = "hi"
        )
        val first = router.decide(message)
        val second = router.decide(message)
        assertTrue(first.deliverLocally)
        assertFalse(second.deliverLocally)
        assertNull(second.relayCopy)
    }

    @Test
    fun emergencyIsDeliveredAndRelayed() {
        val message = MeshMessage(
            type = MeshMessageType.EMERGENCY,
            senderId = "alice",
            senderName = "Alice",
            recipientId = MeshMessage.BROADCAST_ID,
            body = "help",
            ttl = MeshMessage.EMERGENCY_TTL
        )
        val decision = router.decide(message)
        assertTrue(decision.deliverLocally)
        assertNotNull(decision.relayCopy)
    }
}
