package com.example.ori20

fun testMessageSystem() {

    val manager = MessageManager()

    // Create a message
    val message = manager.createMessage(
        senderId = "Device_A",
        receiverId = "Device_B",
        content = "Hello from Device A",
        ttl = 5
    )

    println("Message ID: ${message.messageId}")
    println("Content: ${message.content}")
    println("TTL: ${message.ttl}")
    println("Hop Count: ${message.hopCount}")

    // Store the message
    manager.sendMessage(message)

    // Try receiving the same message
    val firstReceive = manager.receiveMessage(message)
    println("First receive accepted: $firstReceive")

    // Try receiving the same message again
    val secondReceive = manager.receiveMessage(message)
    println("Second receive accepted: $secondReceive")
}