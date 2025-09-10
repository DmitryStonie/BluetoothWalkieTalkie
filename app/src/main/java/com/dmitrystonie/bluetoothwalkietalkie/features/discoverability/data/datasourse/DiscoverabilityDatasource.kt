package com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.data.datasourse

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.widget.Adapter
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerActivity
import javax.inject.Inject

class DiscoverabilityDatasource @Inject constructor(private val mBtAdapter: BluetoothAdapter, private val activity: PermissionCheckerActivity){
    @SuppressLint("MissingPermission")
    fun getDiscoverability(): Boolean = (mBtAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)

    fun enableDiscoverability() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        activity.startActivityForResult(discoverableIntent)
    }

    fun disableDiscoverability() {
        // TODO
    }
}