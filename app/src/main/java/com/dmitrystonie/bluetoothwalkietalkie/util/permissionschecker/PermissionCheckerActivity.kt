package com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import javax.inject.Inject

abstract class PermissionCheckerActivity @Inject constructor() : ComponentActivity() {
    fun checkPermission(permission: String) = ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRationale(permission: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(
            this, permission
        )

    fun requestPermission(permission: String) {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", permission)
            } else {
                Log.d("INFO", permission)
            }
        }.launch(permission)
    }

    fun startActivityForResult(intent: Intent) {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("INFO", "action made")
            } else {
                Log.d("INFO", "action not made")
            }
        }.launch(intent)
    }

    var requestRecordAudioResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", "RECORD_AUDIO granted")
                // делает repository
                startCall()
            } else {
                Log.d("INFO", "RECORD_AUDIO not granted")
            }
        }

    var requestBluetoothConnectForBondedDevicesResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", "BLUETOOTH_CONNECT granted")
                saveBondedDevices()
            } else {
                Log.d("INFO", "BLUETOOTH_CONNECT not granted")
                Log.d("INFO", "BLUETOOTH_CONNECT not granted")
            }
        }

    @SuppressLint("MissingPermission")
    var requestBluetoothConnectForStartServerResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", "BLUETOOTH_CONNECT granted")
                bluetoothService.runAcceptThread()
            } else {
                Log.d("INFO", "BLUETOOTH_CONNECT not granted")
            }
        }

    var requestBluetoothConnectForStartConnectionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", "BLUETOOTH_CONNECT granted")
                deviceToConnect?.let {
                    bluetoothService.runConnectThread(it)
                }
            } else {
                Log.d("INFO", "BLUETOOTH_CONNECT not granted")
            }
        }


    var requestFineLocationResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", "ACCESS_FINE_LOCATION granted")
                discoverOrEnableLocation()
            } else {
                Log.d("INFO", "ACCESS_FINE_LOCATION not granted")
            }
        }

    var requestBluetoothScanResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", "BLUETOOTH_SCAN granted")
                discover()
            } else {
                Log.d("INFO", "BLUETOOTH_SCAN not granted")
            }
        }

    var enableBluetoothResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "BT enabled", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("INFO", "BT not enabled")
            }
        }
    var ensureDiscoverableResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_CANCELED) {
                Log.d("INFO", "Device discoverable")
            } else {
                Log.d("INFO", "Device not discoverable")
            }
        }

    var enableLocationResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("INFO", "Location enabled")
                discover()
            } else {
                Log.d("INFO", "Location not enabled")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionChecker.attach(this@PermissionCheckerActivity)
    }

    open fun onPermissionResult(result: Boolean) {
    }
}
