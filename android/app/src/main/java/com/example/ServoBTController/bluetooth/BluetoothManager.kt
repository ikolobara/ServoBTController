package com.example.servocontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.*

class BluetoothManager {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var socket: BluetoothSocket? = null
    private var output: OutputStream? = null

    private val UUID_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun isBluetoothEnabled(): Boolean =
        adapter?.isEnabled == true

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    suspend fun connect(address: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val device = adapter?.getRemoteDevice(address)
                    ?: return@withContext false

                socket = device.createRfcommSocketToServiceRecord(UUID_SPP)

                adapter?.cancelDiscovery()

                socket?.connect()

                output = socket?.outputStream

                true
            } catch (e: Exception) {
                false
            }
        }


    @RequiresPermission(allOf =
        [
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    ])
    suspend fun autoConnectHC06(): Boolean {
        val devices = adapter?.bondedDevices
            ?.filter { it.name?.contains("BT", true) == true }
            ?: return false

        for (device in devices) {
            if (connect(device.address)) return true
        }

        return false
    }

    fun sendData(data: String): Boolean =
        try {
            output?.write("A:$data\n".toByteArray())
            output?.flush()
            true
        } catch (e: Exception) {
            false
        }

    fun disconnect() {
        try {
            output?.close()
            socket?.close()
        } catch (_: Exception) {}

        output = null
        socket = null
    }
}