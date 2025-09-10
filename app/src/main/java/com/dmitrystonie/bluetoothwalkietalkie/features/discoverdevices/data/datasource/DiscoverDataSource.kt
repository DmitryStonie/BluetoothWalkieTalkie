package com.dmitrystonie.bluetoothwalkietalkie.features.discoverdevices.data.datasource

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.util.Log
import javax.inject.Inject

class DiscoverDataSource @Inject constructor(private val mBtAdapter: BluetoothAdapter) {
    @SuppressLint("MissingPermission")
    fun discover(): Boolean {
        if (mBtAdapter.isDiscovering) {
            mBtAdapter.cancelDiscovery()
        }
        if (!mBtAdapter.startDiscovery()) {
            Log.d("INFO", "disc not working")
            return false
        } else {
            Log.d("INFO", "disc working")
            return true
        }
    }

}