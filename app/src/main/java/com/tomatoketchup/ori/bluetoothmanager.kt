package com.tomatoketchup.ori

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.UUID

class BluetoothManager {

    // Phone's Bluetooth adapter
    private val bluetoothAdapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()

    // Connection between two phones
    private var socket: BluetoothSocket? = null

    companion object {
        // Both phones must use the same UUID
        val APP_UUID: UUID =
            UUID.fromString("12345678-1234-1234-1234-123456789abc")

        const val APP_NAME = "ORI"
    }

    // PHONE B: Wait for another phone to connect
    @SuppressLint("MissingPermission")
    fun startServer(onConnected: () -> Unit) {

        Thread {
            try {
                val serverSocket: BluetoothServerSocket? =
                    bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                        APP_NAME,
                        APP_UUID
                    )

                // Wait until Phone A connects
                socket = serverSocket?.accept()

                if (socket != null) {
                    onConnected()
                }

                serverSocket?.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    // PHONE A: Connect to Phone B
    @SuppressLint("MissingPermission")
    fun connectToDevice(
        device: BluetoothDevice,
        onConnected: () -> Unit
    ) {

        Thread {
            try {
                bluetoothAdapter?.cancelDiscovery()

                socket = device.createRfcommSocketToServiceRecord(
                    APP_UUID
                )

                socket?.connect()

                onConnected()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    // Send a message
    fun sendMessage(message: String) {

        Thread {
            try {
                val outputStream = socket?.outputStream

                outputStream?.write(message.toByteArray())
                outputStream?.flush()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    // Listen for incoming messages
    fun startListening(
        onMessageReceived: (String) -> Unit
    ) {

        Thread {
            try {
                val inputStream = socket?.inputStream
                val buffer = ByteArray(1024)

                while (true) {

                    val bytesRead =
                        inputStream?.read(buffer) ?: -1

                    if (bytesRead > 0) {

                        val message = String(
                            buffer,
                            0,
                            bytesRead
                        )

                        onMessageReceived(message)
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    // Close the connection
    fun closeConnection() {
        try {
            socket?.close()
            socket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}