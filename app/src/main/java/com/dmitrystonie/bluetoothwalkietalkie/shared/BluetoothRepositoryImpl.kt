package com.dmitrystonie.bluetoothwalkietalkie.shared

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerActivity
import javax.inject.Inject

class BluetoothRepositoryImpl @Inject constructor(
    private val mBtAdapter: BluetoothAdapter, private val activity: PermissionCheckerActivity
) {
    fun checkBluetoothEnabled() = mBtAdapter.isEnabled

    fun enableBluetooth() {
        if (!mBtAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableIntent)
        }
    }

}