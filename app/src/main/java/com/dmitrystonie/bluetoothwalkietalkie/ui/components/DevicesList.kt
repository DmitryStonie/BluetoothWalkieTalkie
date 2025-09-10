package com.dmitrystonie.bluetoothwalkietalkie.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device

@Composable
fun DevicesList(
    devices: List<Device>,
    padding: PaddingValues,
    onDeviceClick: (Device) -> Unit
) {
    LazyColumn(modifier = Modifier.height(300.dp).padding(padding)) {
        items(devices) { device ->
            Row(
                modifier = Modifier.clickable(
                    onClick = { onDeviceClick(device) }),
            ) {
                Text(
                    modifier = Modifier.padding(all = 20.dp),
                    text = device.name,
                )
                Text(
                    modifier = Modifier.padding(all = 20.dp),
                    text = device.mac,
                )
            }
        }

    }
}