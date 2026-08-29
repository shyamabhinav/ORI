package com.tomatoketchup.ori.nearby

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.Strategy

/**
 * Lightweight Nearby Connections relay service skeleton.
 * - Starts advertising and discovery so the device can participate in a store-and-forward mesh.
 * - Receives Payloads and inserts them into the local DB via broadcast/intent or by calling repository (TODO).
 *
 * This is intentionally minimal for the hackathon demo — expand to handle payload chunking, ACKs,
 * deduplication, and foreground notification.
 */
class NearbyRelayService : Service() {
    private lateinit var connectionsClient: ConnectionsClient
    private val TAG = "NearbyRelayService"

    override fun onCreate() {
        super.onCreate()
        connectionsClient = Nearby.getConnectionsClient(this)
        startAdvertising()
        startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            connectionsClient.stopAllEndpoints()
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
        } catch (e: Exception) {
            Log.w(TAG, "stop error", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startAdvertising(
            /* endpointName = */ android.os.Build.MODEL,
            /* serviceId = */ packageName,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.i(TAG, "Advertising started")
        }.addOnFailureListener {
            Log.e(TAG, "Advertising failed", it)
        }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startDiscovery(
            packageName,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.i(TAG, "Discovery started")
        }.addOnFailureListener {
            Log.e(TAG, "Discovery failed", it)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "Found endpoint: $endpointId / ${info.endpointName}")
            // Auto-request connection for demo purposes
            connectionsClient.requestConnection(android.os.Build.MODEL, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "Lost endpoint: $endpointId")
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.i(TAG, "Connection initiated with $endpointId")
            // Accept the connection for demo; production should verify identity
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.i(TAG, "Connection result for $endpointId: ${result.status}")
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "Disconnected: $endpointId")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when {
                payload.asBytes() != null -> {
                    val bytes = payload.asBytes()!!
                    val text = String(bytes)
                    Log.i(TAG, "Payload from $endpointId: $text")
                    // TODO: parse message JSON, insert into Room DB and/or broadcast to app using Intent/LocalBroadcast
                }
                payload.asFile() != null -> {
                    Log.i(TAG, "File payload from $endpointId")
                }
                payload.asStream() != null -> {
                    Log.i(TAG, "Stream payload from $endpointId")
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Optional: track progress
        }
    }
}
