package com.dmitrystonie.bluetoothwalkietalkie

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.dmitrystonie.bluetoothwalkietalkie.ui.MainScreen
import com.dmitrystonie.bluetoothwalkietalkie.ui.theme.BluetoothWalkieTalkieTheme
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerActivity
import com.dmitrystonie.bluetoothwalkietalkie.util.permissionschecker.PermissionCheckerImpl
import javax.inject.Inject


class MainActivity: PermissionCheckerActivity(){

    lateinit var mBtAdapter: BluetoothAdapter

    var deviceToConnect: BluetoothDevice? = null
    lateinit var bluetoothService: BluetoothService

    val discoveredDevices: MutableLiveData<MutableSet<BluetoothDevice>> by lazy {
        MutableLiveData<MutableSet<BluetoothDevice>>()
    }
    var bondedDevices: List<BluetoothDevice> = listOf()

    private fun startCall() {
        bluetoothService.startCall()
    }

    private fun endCall() {
        bluetoothService.endCall()
    }


    fun startCallWithPerm() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCall()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.RECORD_AUDIO
            ) -> {
                // ui but no
                requestRecordAudioResultLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> {
                requestRecordAudioResultLauncher.launch(Manifest.permission.RECORD_AUDIO)

            }
        }
    }

    @SuppressLint("MissingPermission")
    fun saveBondedDevices() {
        bondedDevices = mBtAdapter.bondedDevices.toList()
    }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBtAdapter = bluetoothManager.adapter
        bluetoothService = BluetoothService(
            mBtAdapter = mBtAdapter,
        )

        checkBluetoothEnabled()
        setupDiscovery()

        getBondedDevices()


        setContent {
            BluetoothWalkieTalkieTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        pairedDevices = bondedDevices,
                        innerPadding = innerPadding,
                        onDiscoverable = {
                            ensureDiscoverable()
                        },
                        onDiscover = { discoverDevices() },
                        discoveredDevices = discoveredDevices,
                        onEnableServer = { startServer() },
                        onDeviceClick = { device -> startConnection(device) },
                        onSend = {
                            writeMessage()
                        },
                        modifier = Modifier,
                        onStartCall = { startCallWithPerm() },
                        onStopCall = { endCall() },
                    )
                }
            }
        }
    }

    fun writeMessage() {
        Log.d("INFO", "try to write ... to  ${bluetoothService.connectedThread}")

        bluetoothService.connectedThread?.write("Text example".encodeToByteArray())
    }

    fun startServer() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            bluetoothService.runAcceptThread()
            return
        }
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED -> {
                bluetoothService.runAcceptThread()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) -> {
                // ui but no
                requestBluetoothConnectForStartServerResultLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }

            else -> {
                requestBluetoothConnectForStartServerResultLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }

    fun startConnection(device: BluetoothDevice) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            bluetoothService.runConnectThread(device)
            return
        }
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED -> {
                bluetoothService.runConnectThread(device)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) -> {
                // ui but no
                deviceToConnect = device
                requestBluetoothConnectForStartConnectionResultLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }

            else -> {
                deviceToConnect = device
                requestBluetoothConnectForStartConnectionResultLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }

    fun checkBluetoothEnabled() {
        if (!mBtAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothResultLauncher.launch(enableIntent)
        }
    }

    fun setupDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, discoverFilter, RECEIVER_EXPORTED)
            registerReceiver(mReceiver, discoveryEndFilter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag") registerReceiver(mReceiver, discoverFilter)
            @Suppress("UnspecifiedRegisterReceiverFlag") registerReceiver(
                mReceiver, discoveryEndFilter
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun ensureDiscoverable() {
        if (mBtAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            ensureDiscoverableResultLauncher.launch(discoverableIntent)
        }
    }

    fun discoverOrEnableLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val enableIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            enableLocationResultLauncher.launch(enableIntent)
        } else {
            discover()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    fun discoverDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            discoverDevicesModern()
        } else {
            discoverDevicesLegacy()
        }
    }

    fun discoverDevicesLegacy() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                //ui with ask to enable location
                discoverOrEnableLocation()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) -> {
                // ui but no
                requestFineLocationResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                requestFineLocationResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun discoverDevicesModern() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED -> {
                //ui with ask to enable location
                discover()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) -> {
                // ui but no
                requestBluetoothScanResultLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
            }

            else -> {
                requestBluetoothScanResultLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun discover() {
        if (mBtAdapter.isDiscovering) {
            mBtAdapter.cancelDiscovery()
        }
        discoveredDevices.value = mutableSetOf()
        if (!mBtAdapter.startDiscovery()) {
            Log.d("INFO", "disc not working")
        } else {
            Log.d("INFO", "disc working")
        }
    }

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
                        if (discoveredDevices.value == null) {
                            discoveredDevices.value = mutableSetOf()
                        } else {
                            discoveredDevices.value = discoveredDevices.value!!.toMutableSet()
                        }
                        discoveredDevices.value?.add(device)
                    }
                    Log.d("INFO", "found ${device?.address} ${discoveredDevices.value}")
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
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}



