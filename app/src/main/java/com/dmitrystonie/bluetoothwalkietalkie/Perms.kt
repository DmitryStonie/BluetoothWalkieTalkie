package com.dmitrystonie.bluetoothwalkietalkie

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions

fun checkScan(activity: MainActivity, code: Int) {
    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d("INFO", "BLUETOOTH_SCAN not granted")
        requestPermissions(
            activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), code
        );
        return
    }
}

fun checkAdmin(activity: MainActivity, code: Int) {
    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_ADMIN
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d("INFO", "BLUETOOTH_ADMIN not granted")
        requestPermissions(
            activity, arrayOf(Manifest.permission.BLUETOOTH_ADMIN), code
        );
        return
    }
}

fun checkBluetooth(activity: MainActivity, code: Int) {
    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d("INFO", "BLUETOOTH not granted")
        requestPermissions(
            activity, arrayOf(Manifest.permission.BLUETOOTH), code
        );
        return
    }
}

fun checkCoarse(activity: MainActivity, code: Int) {
    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d("INFO", "ACCESS_COARSE_LOCATION not granted")
        requestPermissions(
            activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), code
        );
        return
    }
}

fun checkBluetoothConnect(activity: MainActivity, code: Int) {
    if (ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d("INFO", "BLUETOOTH_CONNECT not granted")
        requestPermissions(
            activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), code
        );
        return
    }
}

//fun checkAdmin(activity: MainActivity){
//
//}
//
