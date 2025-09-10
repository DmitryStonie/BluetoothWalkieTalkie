package com.dmitrystonie.bluetoothwalkietalkie

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.dmitrystonie.bluetoothwalkietalkie.ui.MainScreen
import com.dmitrystonie.bluetoothwalkietalkie.ui.theme.BluetoothWalkieTalkieTheme
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerActivity


class MainActivity: PermissionCheckerActivity(){
    lateinit var mReceiver : BroadcastReceiver

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkBluetoothEnabled()
        setupDiscovery()
        getBondedDevices()


        setContent {
            BluetoothWalkieTalkieTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        pairedDevices = bondedDevices,
                        innerPadding = innerPadding,
                        onDiscoverable = {
                            ensureDiscoverable()
                        },
                        onDiscover = { discoverDevices() },
                        discoveredDevices = discoveredDevices,
                        onEnableServer = { startServer() },
                        onDeviceClick = { device -> startConnection(device) },
                        onSend = {
                            writeMessage()
                        },
                        modifier = Modifier,
                        onStartCall = { startCallWithPerm() },
                        onStopCall = { endCall() },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::mReceiver.isInitialized) {
            unregisterReceiver(mReceiver)
        }
    }
}



