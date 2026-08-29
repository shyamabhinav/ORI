package com.example.meshchat.mesh

internal interface MeshTransport {
    fun start()
    fun stop()
    fun send(message: MeshMessage, excludeEndpointId: String? = null)
    fun connectedPeers(): List<MeshPeer>
}
