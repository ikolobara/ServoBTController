package com.example.servocontroller

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ServoBTController.ui.theme.ServoBTControllerTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: BluetoothManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager = BluetoothManager()
        requestPermissions()

        setContent {
            ServoBTControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ServoControlScreen(bluetoothManager)
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        permissionLauncher.launch(permissions)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.disconnect()
    }
}

@Composable
fun ServoControlScreen(btManager: BluetoothManager) {

    var isConnected by remember { mutableStateOf(false) }
    var servoAngle by remember { mutableStateOf(90f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(true) }
    var retryTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(retryTrigger) {
        errorMessage = null
        isConnecting = true

        if (!btManager.isBluetoothEnabled()) {
            errorMessage = "Bluetooth is turned OFF."
            isConnecting = false
            return@LaunchedEffect
        }

        @SuppressLint("MissingPermission")
        val success = btManager.autoConnectHC06()

        isConnected = success

        if (!success) {
            errorMessage = "Auto-connect failed. HC-06 not found."
        }

        isConnecting = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "Servo Controller",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (isConnecting) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("Connecting...")
        }

        if (!isConnected && !isConnecting) {

            Button(
                onClick = {
                    retryTrigger++
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry connect")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (isConnected) {

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    btManager.disconnect()
                    isConnected = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Disconnect")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "${servoAngle.toInt()}°",
                style = MaterialTheme.typography.displayLarge
            )

            Slider(
                value = servoAngle,
                onValueChange = { servoAngle = it },
                onValueChangeFinished = {
                    btManager.sendData(servoAngle.toString())
                },
                valueRange = 0f..180f
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                Button(onClick = {
                    servoAngle = 0f
                    btManager.sendData("0")
                }) { Text("0°") }

                Button(onClick = {
                    servoAngle = 90f
                    btManager.sendData("90")
                }) { Text("90°") }

                Button(onClick = {
                    servoAngle = 180f
                    btManager.sendData("180")
                }) { Text("180°") }
            }
        }
    }
}