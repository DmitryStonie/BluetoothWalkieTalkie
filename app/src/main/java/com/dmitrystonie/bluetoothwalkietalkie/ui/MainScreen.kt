package com.dmitrystonie.bluetoothwalkietalkie.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.dmitrystonie.bluetoothwalkietalkie.Greeting
import com.dmitrystonie.bluetoothwalkietalkie.ui.theme.BluetoothWalkieTalkieTheme

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothWalkieTalkieTheme {
        Greeting("Android")
    }
}


@SuppressLint("MissingPermission")
@Composable
fun PairedDevicesList(
    pairedDevices: List<BluetoothDevice>,
    padding: PaddingValues,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    LazyColumn(modifier = Modifier.height(300.dp).padding(padding)) {
        items(pairedDevices) { device ->
            Row(
                modifier = Modifier.clickable(
                    onClick = { onDeviceClick(device) }),
            ) {
                Text(
                    modifier = Modifier.padding(all = 20.dp),
                    text = device.name ?: "",
                )
                Text(
                    modifier = Modifier.padding(all = 20.dp),
                    text = device.address ?: "",
                )
            }
        }

    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    discoveredDevices: LiveData<MutableSet<BluetoothDevice>>,
    pairedDevices: List<BluetoothDevice>,
    innerPadding: PaddingValues,
    onDiscoverable: () -> Unit,
    onDiscover: () -> Unit,
    onEnableServer: () -> Unit,
    onSend: () -> Unit,
    onStartCall: () -> Unit,
    onStopCall: () -> Unit,
    onDeviceClick: (device: BluetoothDevice) -> Unit,
) {
    val state by discoveredDevices.observeAsState(arrayListOf())
    Column(
        modifier = modifier
            .padding(top = 60.dp)
            .scrollable(
                enabled = true, state = ScrollableState { 0F }, orientation = Orientation.Vertical
            )
    ) {
        Button(
            onClick = { onDiscoverable() }) {
            Text(
                text = "Make discoverable",
            )
        }
        Button(
            onClick = { onDiscover() }) {
            Text(
                text = "Discover devices",
            )
        }
        Button(
            onClick = { onEnableServer() }) {
            Text(
                text = "Enable server",
            )
        }
        Button(
            onClick = { onSend() }) {
            Text(
                text = "Send data",
            )
        }
        Button(
            onClick = { onStartCall() }) {
            Text(
                text = "Start call",
            )
        }
        Button(
            onClick = { onStopCall() }) {
            Text(
                text = "Stop call",
            )
        }
        Text(
            text = "Bonded devices"
        )
        PairedDevicesList(pairedDevices.toList(), innerPadding, onDeviceClick)

        Text(
            text = "Discovered devices"
        )
        PairedDevicesList(state.toList(), innerPadding, onDeviceClick)


    }
}