package com.tomatoketchup.ori

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy

class NearbyManager(
    private val context: Context,
    private val userName: String,
    private val onMessageReceived: (String, String) -> Unit,
    private val onDeviceConnected: (String) -> Unit
) {

    companion object {
        private const val TAG = "ORI_NEARBY"

        // All ORI devices must use the same service ID
        private const val SERVICE_ID = "com.tomatoketchup.ori"

        // Suitable for multiple nearby devices
        private val STRATEGY = Strategy.P2P_CLUSTER
    }

    private val connectionsClient: ConnectionsClient =
        Nearby.getConnectionsClient(context)

    // Stores all successfully connected nearby devices
    private val connectedEndpoints = mutableSetOf<String>()

    // Prevent requesting the same connection repeatedly
    private val requestedEndpoints = mutableSetOf<String>()

    // -------------------------------
    // START EVERYTHING
    // -------------------------------

    fun startNearby() {
        startAdvertising()
        startDiscovery()
    }

    // -------------------------------
    // ADVERTISE THIS ORI DEVICE
    // -------------------------------

    private fun startAdvertising() {

        val options = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startAdvertising(
            userName,
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        )
            .addOnSuccessListener {
                Log.d(TAG, "ORI advertising started")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Advertising failed", e)
            }
    }

    // -------------------------------
    // DISCOVER OTHER ORI DEVICES
    // -------------------------------

    private fun startDiscovery() {

        val options = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            options
        )
            .addOnSuccessListener {
                Log.d(TAG, "ORI discovery started")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Discovery failed", e)
            }
    }

    // -------------------------------
    // WHEN A DEVICE IS FOUND
    // -------------------------------

    private val endpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {

            override fun onEndpointFound(
                endpointId: String,
                info: DiscoveredEndpointInfo
            ) {

                Log.d(
                    TAG,
                    "Found ORI device: ${info.endpointName}"
                )

                // Avoid repeated connection requests
                if (
                    endpointId !in connectedEndpoints &&
                    endpointId !in requestedEndpoints
                ) {

                    requestedEndpoints.add(endpointId)

                    requestConnection(
                        endpointId,
                        info.endpointName
                    )
                }
            }

            override fun onEndpointLost(
                endpointId: String
            ) {

                Log.d(
                    TAG,
                    "ORI device lost: $endpointId"
                )

                requestedEndpoints.remove(endpointId)
            }
        }

    // -------------------------------
    // REQUEST CONNECTION
    // -------------------------------

    private fun requestConnection(
        endpointId: String,
        endpointName: String
    ) {

        connectionsClient.requestConnection(
            userName,
            endpointId,
            connectionLifecycleCallback
        )
            .addOnSuccessListener {

                Log.d(
                    TAG,
                    "Connection requested: $endpointName"
                )
            }
            .addOnFailureListener { e ->

                requestedEndpoints.remove(endpointId)

                Log.e(
                    TAG,
                    "Connection request failed",
                    e
                )
            }
    }

    // -------------------------------
    // CONNECTION EVENTS
    // -------------------------------

    private val connectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {

            override fun onConnectionInitiated(
                endpointId: String,
                connectionInfo: ConnectionInfo
            ) {

                Log.d(
                    TAG,
                    "Connection initiated with: ${connectionInfo.endpointName}"
                )

                /*
                 * For the hackathon prototype:
                 * automatically accept the nearby ORI connection.
                 */
                connectionsClient.acceptConnection(
                    endpointId,
                    payloadCallback
                )
            }

            override fun onConnectionResult(
                endpointId: String,
                result: ConnectionResolution
            ) {

                val statusCode =
                    result.status.statusCode

                if (
                    result.status.isSuccess
                ) {

                    connectedEndpoints.add(endpointId)

                    Log.d(
                        TAG,
                        "Connected: $endpointId"
                    )

                    onDeviceConnected(endpointId)

                } else {

                    requestedEndpoints.remove(endpointId)

                    Log.d(
                        TAG,
                        "Connection failed: $statusCode"
                    )
                }
            }

            override fun onDisconnected(
                endpointId: String
            ) {

                connectedEndpoints.remove(endpointId)
                requestedEndpoints.remove(endpointId)

                Log.d(
                    TAG,
                    "Disconnected: $endpointId"
                )
            }
        }

    // -------------------------------
    // RECEIVE MESSAGES
    // -------------------------------

    private val payloadCallback =
        object : PayloadCallback() {

            override fun onPayloadReceived(
                endpointId: String,
                payload: Payload
            ) {

                if (
                    payload.type == Payload.Type.BYTES
                ) {

                    val bytes =
                        payload.asBytes()

                    if (bytes != null) {

                        val message =
                            String(
                                bytes,
                                Charsets.UTF_8
                            )

                        Log.d(
                            TAG,
                            "Message received: $message"
                        )

                        onMessageReceived(
                            endpointId,
                            message
                        )
                    }
                }
            }

            override fun onPayloadTransferUpdate(
                endpointId: String,
                update: PayloadTransferUpdate
            ) {

                // For simple text messages,
                // no special handling is needed yet.
            }
        }

    // -------------------------------
    // SEND MESSAGE TO ONE DEVICE
    // -------------------------------

    fun sendMessage(
        endpointId: String,
        message: String
    ) {

        val payload =
            Payload.fromBytes(
                message.toByteArray(
                    Charsets.UTF_8
                )
            )

        connectionsClient.sendPayload(
            endpointId,
            payload
        )
    }

    // -------------------------------
    // SEND MESSAGE TO ALL CONNECTED
    // DEVICES
    // -------------------------------

    fun broadcastMessage(
        message: String
    ) {

        if (connectedEndpoints.isEmpty()) {
            Log.d(
                TAG,
                "No devices connected"
            )
            return
        }

        val payload =
            Payload.fromBytes(
                message.toByteArray(
                    Charsets.UTF_8
                )
            )

        connectionsClient.sendPayload(
            connectedEndpoints.toList(),
            payload
        )
    }

    // -------------------------------
    // GET CONNECTED DEVICES
    // -------------------------------

    fun getConnectedEndpoints(): Set<String> {
        return connectedEndpoints.toSet()
    }

    // -------------------------------
    // STOP EVERYTHING
    // -------------------------------

    fun stopNearby() {

        connectionsClient.stopAdvertising()

        connectionsClient.stopDiscovery()

        connectionsClient.stopAllEndpoints()

        connectedEndpoints.clear()

        requestedEndpoints.clear()
    }
}