package com.dmitrystonie.bluetoothwalkietalkie.features.discoverdevices.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.dmitrystonie.bluetoothwalkietalkie.MainActivity
import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device
import com.dmitrystonie.bluetoothwalkietalkie.features.discoverdevices.data.datasource.DiscoverDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DiscoverRepositoryImpl @Inject constructor(
    private val dataSource: DiscoverDataSource, private val activity: MainActivity
) {
    private var isInitialized = false
    private lateinit var discoveredDevices: Flow<Device>

    fun discoverDevices(): Flow<Device>? {
        if(!isInitialized){
            initDiscovery()
        }
        val isDiscovering = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            discoverDevicesModern()
        } else {
            discoverDevicesLegacy()
        }
        return if(isDiscovering == true){
            discoveredDevices
        } else{
            null
        }
    }

    private fun initDiscovery(){
        val discoverFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val discoveryEndFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        val mReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(p0: Context?, p1: Intent?) {
                val action = p1?.action
                when (action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                            p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else{
                            @Suppress("DEPRECATION")
                            p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        device?.let {
                            if(!::discoveredDevices.isInitialized){
                                discoveredDevices = flow{
                                    emit(Device(
                                        name = it.name,
                                        mac = it.address,
                                        isConnected = it.bondState == BluetoothDevice.BOND_BONDED
                                    ))
                                }
                            }
                        }
                        Log.d("INFO", "found ${device?.address} ${discoveredDevices}")
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.d("INFO", "discovery finished")
                    }

                    else -> {
                        Log.d("INFO", action.toString())
                    }
                }
            }
        }
        activity.mReceiver = mReceiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(mReceiver, discoverFilter, RECEIVER_EXPORTED)
            activity.registerReceiver(mReceiver, discoveryEndFilter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag") activity.registerReceiver(mReceiver, discoverFilter)
            @Suppress("UnspecifiedRegisterReceiverFlag") activity.registerReceiver(
                mReceiver, discoveryEndFilter
            )
        }
    }


    private fun discoverDevicesLegacy(): Boolean? {
        when {
            activity.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                return discoverOrEnableLocation()
            }

            else -> {
                activity.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                return if (activity.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    discoverOrEnableLocation()
                } else {
                    null
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun discoverDevicesModern(): Boolean? {
        when {
            activity.checkPermission(Manifest.permission.BLUETOOTH_SCAN) -> {
                return dataSource.discover()
            }

            else -> {
                activity.requestPermission(Manifest.permission.BLUETOOTH_SCAN)
                return if (activity.checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                    dataSource.discover()
                } else {
                    null
                }
            }
        }
    }

    private fun discoverOrEnableLocation(): Boolean? {
        val locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val enableIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivityForResult(enableIntent)
            return if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                dataSource.discover()
            } else {
                null
            }
        } else {
            return dataSource.discover()
        }
    }

}