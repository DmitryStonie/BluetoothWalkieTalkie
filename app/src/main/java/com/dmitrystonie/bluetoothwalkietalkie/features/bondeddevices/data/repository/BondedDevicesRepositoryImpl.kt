package com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device
import com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.data.datasource.BondedDevicesDataSource
import com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.domain.repository.BondedDevicesRepository
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerActivity

class BondedDevicesRepositoryImpl(
    private val dataSource: BondedDevicesDataSource, private val activity: PermissionCheckerActivity
) : BondedDevicesRepository {
    @SuppressLint("MissingPermission")
    override suspend fun getBondedDevices(): List<Device>? {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return dataSource.getBondedDevices()
        }
        when {
            activity.checkPermission(Manifest.permission.BLUETOOTH_CONNECT) -> {
                return dataSource.getBondedDevices()
            }

//            activity.shouldShowRationale(Manifest.permission.BLUETOOTH_CONNECT) -> {
//                // ui but how?
//            }

            else -> {
                activity.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
                return if (activity.checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    dataSource.getBondedDevices()
                } else{
                    null
                }
            }
        }
    }
}