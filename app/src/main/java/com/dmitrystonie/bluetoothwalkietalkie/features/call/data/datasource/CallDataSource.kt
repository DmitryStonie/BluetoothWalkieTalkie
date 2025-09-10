package com.dmitrystonie.bluetoothwalkietalkie.features.call.data.datasource

import android.bluetooth.BluetoothManager
import android.content.Context.BLUETOOTH_SERVICE
import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerActivity
import javax.inject.Inject

class CallDataSource @Inject constructor(private val activity: PermissionCheckerActivity) {
    val bluetoothService: BluetoothService

    init {
        val bluetoothManager = activity.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val mBtAdapter = bluetoothManager.adapter
        bluetoothService = BluetoothService(mBtAdapter)
    }

    fun startConnection(device: Device){
        bluetoothService.runConnectThread(device)
    }

    fun startServer(){
        bluetoothService.runAcceptThread()
    }

    fun startCall(){
        bluetoothService.startCall()
    }

    fun endCall(){
        bluetoothService.endCall()
    }
}