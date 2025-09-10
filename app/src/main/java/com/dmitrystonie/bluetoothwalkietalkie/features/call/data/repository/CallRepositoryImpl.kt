package com.dmitrystonie.bluetoothwalkietalkie.features.call.data.repository

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device
import com.dmitrystonie.bluetoothwalkietalkie.features.call.data.datasource.CallDataSource
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerActivity
import javax.inject.Inject

class CallRepositoryImpl @Inject constructor(
    private val dataSource: CallDataSource, private val activity: PermissionCheckerActivity
) {

    fun startCall() {

    }


    fun startServer(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            dataSource.startServer()
            return
        }
        when {
            activity.checkPermission(Manifest.permission.BLUETOOTH_CONNECT) -> {
                dataSource.startServer()
            }

            else -> {
                activity.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
                if(activity.checkPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                    dataSource.startServer()
                }
            }
        }
    }

    private fun startConnection(device: Device) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            dataSource.startConnection(device)
            return
        }
        when {
            activity.checkPermission(Manifest.permission.BLUETOOTH_CONNECT) -> {
                dataSource.startConnection(device)
            }

            else -> {
                activity.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
                if(activity.checkPermission(Manifest.permission.BLUETOOTH_CONNECT)){
                    dataSource.startConnection(device)
                } else{
//                    return null
                }
            }
        }
    }


    private fun beginCall() {
        when {
            activity.checkPermission(Manifest.permission.RECORD_AUDIO) -> {
                dataSource.startCall()
            }

            else -> {
                activity.requestPermission(Manifest.permission.RECORD_AUDIO)
                if (activity.checkPermission(Manifest.permission.RECORD_AUDIO)) {
                    dataSource.startCall()
                } else {
//                    return null
                }
            }
        }
    }

    fun endCall() {

    }
}