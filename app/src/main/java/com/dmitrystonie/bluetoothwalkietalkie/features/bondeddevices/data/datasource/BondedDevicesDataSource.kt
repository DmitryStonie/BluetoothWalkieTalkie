package com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.data.datasource

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device
import javax.inject.Inject

class BondedDevicesDataSource @Inject constructor(private val mBtAdapter: BluetoothAdapter) {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getBondedDevices() = mBtAdapter.bondedDevices.map { bluetoothDevice -> Device(
        name = bluetoothDevice.name,
        mac = bluetoothDevice.address,
        isConnected = bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED
    ) }.toList()
}