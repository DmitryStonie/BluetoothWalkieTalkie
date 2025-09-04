package com.dmitrystonie.bluetoothwalkietalkie

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dmitrystonie.bluetoothwalkietalkie.ui.theme.BluetoothWalkieTalkieTheme
import java.io.FileDescriptor
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


class MainActivity : ComponentActivity() {

    lateinit var mBtAdapter: BluetoothAdapter

    var deviceToConnect: BluetoothDevice? = null
    private var bluetoothService = BluetoothService()


    val NAME_SECURE: String = "BluetoothChatSecure"
    val MY_UUID_SECURE: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    val discoveredDevices: MutableLiveData<MutableSet<BluetoothDevice>> by lazy {
        MutableLiveData<MutableSet<BluetoothDevice>>()
    }

    var bondedDevices: List<BluetoothDevice> = listOf()


    private var recorder: AudioRecord? = null
    var audioTrack : AudioTrack? = null

    private val sampleRate = 16000; // 44100 for music
    private val channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    var minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private val status = true;

    //    private var recorder: MediaRecorder? = null
    private var fileName: String = ""
    private var player: MediaPlayer? = null


    var requestRecordAudioResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("INFO", "RECORD_AUDIO granted")
                record(true)
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

    @SuppressLint("MissingPermission")
    private fun startStreaming() {
        Log.d("INFO", "start streaming")
        bluetoothService.initStreamMicThread()
    }

    private fun stopRecording() {
        Log.d("INFO", "stop recording to $fileName")
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun onPlay(start: Boolean) = if (start) {
        bluetoothService.connectedThread?.parcelRead?.fileDescriptor?.let {
            startPlaying(it)
        }
//        startPlaying(bluetoothService.connectedThread?.parcelRead?.fileDescriptor)
    } else {
        stopPlaying()
    }

    private fun startPlaying(fileDescriptor: FileDescriptor) {

    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun record(start: Boolean) = if (start) {
        startStreaming()
    } else {
        stopRecording()
    }


    fun recordAudio() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                record(true)
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

    fun getBondedDevices() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            saveBondedDevices()
            return
        }
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED -> {
                saveBondedDevices()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) -> {
                // ui but no
                requestBluetoothConnectForBondedDevicesResultLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)

            }

            else -> {
                requestBluetoothConnectForBondedDevicesResultLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)

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

        checkBluetoothEnabled()
        setupDiscovery()

        getBondedDevices()

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

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
                        onStartRecord = { recordAudio() },
                        onStopRecord = { record(false) },
                        onStartPlay = { onPlay(true) },
                        onStopPlay = { onPlay(false) },
                        onSendAudio = { sendAudio() })
                }
            }
        }
    }

    fun sendAudio() {
        bluetoothService.connectedThread?.write("Text example".encodeToByteArray())
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
                    val device: BluetoothDevice? =
                        p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
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

    private inner class BluetoothService() {

        private var acceptThread: AcceptThread? = null
        private var connectThread: ConnectThread? = null
        var connectedThread: ConnectedThread? = null
        var streamMicThread: StreamMicThread? = null

        private inner class AcceptThread(private val adapter: BluetoothAdapter) : Thread() {

            private var mmServerSocket: BluetoothServerSocket? = null

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun run() {
                if (mmServerSocket == null) {
                    mmServerSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_SECURE, MY_UUID_SECURE
                    )
                }
                var shouldLoop = true
                while (shouldLoop) {
                    val socket: BluetoothSocket? = try {
                        Log.d("INFO", "Server starting...")
                        mmServerSocket?.accept()
                    } catch (e: IOException) {
                        Log.e("INFO", "Socket's accept() method failed", e)
                        shouldLoop = false
                        null
                    }
                    socket?.also {
                        manageConnectedDevice(it)
//                    mmServerSocket?.close()
                        shouldLoop = false
                    }
                }
            }

            fun cancel() {
                try {
                    mmServerSocket?.close()
                } catch (e: IOException) {
                    Log.e("INFO", "Could not close the connect socket", e)
                }
            }
        }

        private inner class ConnectThread(
            private val device: BluetoothDevice, private val adapter: BluetoothAdapter
        ) : Thread() {

            private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                device.createRfcommSocketToServiceRecord(MY_UUID_SECURE)
            }

            @SuppressLint("MissingPermission")
            override fun run() {
                adapter.cancelDiscovery()
                try {
                    Log.d("INFO", "connecting to $device")
                    mmSocket!!.connect()
                    Log.d("INFO", "connected to $device")
                    manageConnectedDevice(mmSocket!!)
                } catch (e: IOException) {
                    Log.d("INFO", "failed to connect to $device", e)
                    cancel()
                }
            }

            fun cancel() {
                try {
                    mmSocket?.close()
                } catch (e: IOException) {
                    Log.e("INFO", "Could not close the client socket", e)
                }
            }
        }

        inner class ConnectedThread(
            val mmSocket: BluetoothSocket, private val handler: Handler
        ) : Thread() {

            private val mmInStream: InputStream = mmSocket.inputStream
            val mmOutStream: OutputStream = mmSocket.outputStream
            val bufferSize = 16384
            private val mmBuffer: ByteArray = ByteArray(bufferSize) // mmBuffer store for the stream

            var sent = 0
            var read = 0

            val descriptors = ParcelFileDescriptor.createPipe();
            val parcelRead = ParcelFileDescriptor(descriptors[0]);
            val parcelWrite = ParcelFileDescriptor(descriptors[1]);

            val inputStream =
                ParcelFileDescriptor.AutoCloseOutputStream(parcelWrite)

            override fun run() {
                var numBytes: Int
                Log.d("INFO", "started connected")
                while (true) {
                    numBytes = try {
                        mmInStream.read(mmBuffer)
                    } catch (e: IOException) {
                        Log.d("INFO", "Input stream was disconnected", e)
                        break
                    }
//                    Log.d("INFO", "read message ${mmBuffer}")
                    read+=numBytes
                    Log.d("INFO", "Got:  " + numBytes + "read ${read}")
//
//                    player = MediaPlayer().apply {
//                        try {
//                            setDataSource(parcelRead.fileDescriptor)
//                            prepare()
//                            start()
//                        } catch (e: IOException) {
//                            Log.e("INFO", "prepare() failed")
//                        }
//                    }

                    if(audioTrack == null) {
                        audioTrack = AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                            AudioTrack.MODE_STREAM
                        )
                        audioTrack!!.play()
                    }

                    audioTrack!!.write(mmBuffer, 0, numBytes)


//                    inputStream.write(mmBuffer, 0, numBytes)

//                    Log.d("INFO", "wrote:  " + numBytes)

//                    gdfaofdaiokimfdaj
//                    val readMsg = handler.obtainMessage(
//                        MESSAGE_READ, numBytes, -1, mmBuffer
//                    )
//                    readMsg.sendToTarget()
                }
            }

            fun write(bytes: ByteArray) {
                try {
                    mmOutStream.write(bytes)
                } catch (e: IOException) {
                    Log.e(TAG, "Error occurred when sending data", e)

                    val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                    val bundle = Bundle().apply {
                        putString(TOAST, "Couldn't send data to the other device")
                    }
                    writeErrorMsg.data = bundle
                    handler.sendMessage(writeErrorMsg)
                    return
                }
                sent+=bytes.size
                Log.d("INFO", "Me:  " + bytes.size + "sent ${sent}")
//                Log.d("INFO", "write message $mmBuffer")
                val writtenMsg = handler.obtainMessage(
                    MESSAGE_WRITE, -1, -1, mmBuffer
                )
                writtenMsg.sendToTarget()
            }

            fun cancel() {
                try {
                    mmSocket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the connect socket", e)
                }
            }

        }

        inner class StreamMicThread(
            val connectedThread: ConnectedThread
        ) : Thread() {

            @SuppressLint("MissingPermission")
            override fun run() {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufSize * 10
                )
                Log.d("VS", "Recorder initialized")
                recorder!!.startRecording();

                val buffer = ByteArray(minBufSize)

                while (status == true) {
                    minBufSize = recorder!!.read(buffer, 0, buffer.size);
                    connectedThread.write(buffer)
                    System.out.println("MinBufferSize: " + minBufSize);

                }

                recorder!!.startRecording()

            }
        }

        fun runAcceptThread() {
            acceptThread = AcceptThread(mBtAdapter)
            acceptThread!!.start()
        }

        fun runConnectThread(device: BluetoothDevice) {
            connectThread = ConnectThread(device, mBtAdapter)
            connectThread!!.start()
        }

        fun manageConnectedDevice(socket: BluetoothSocket) {

            connectedThread = ConnectedThread(
                socket, handler = mHandler
            )
            connectedThread!!.start()
        }

        fun initStreamMicThread() {
            if (streamMicThread != null) {
                streamMicThread!!.interrupt()
            }
            if (connectedThread != null) {
                streamMicThread = StreamMicThread(connectedThread!!)
                streamMicThread!!.start()
            } else {
                Log.d("INFO", "connected thread is null")
            }
        }
    }


    private val TAG = "MY_APP_DEBUG_TAG"

    val MESSAGE_READ: Int = 0
    val MESSAGE_WRITE: Int = 1
    val MESSAGE_TOAST: Int = 2

    val TOAST = "toast"

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val activity: MainActivity = this@MainActivity
            when (msg.what) {

                MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    val writeMessage = String(writeBuf)
//                    Log.d("INFO", "Me:  " + writeMessage)
//                    Toast.makeText(this@MainActivity, "Me:  " + writeMessage, Toast.LENGTH_LONG)
//                        .show()
                }

                MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray

                    val readMessage = String(readBuf, 0, msg.arg1)
//                    Log.d("INFO", "Got:  " + readMessage.size)

//                    Toast.makeText(this@MainActivity, "Me:  " + readMessage, Toast.LENGTH_LONG)
//                        .show()

                }


                MESSAGE_TOAST -> if (null != activity) {
//                    Toast.makeText(
//                        activity, msg.getData().getString(TOAST), Toast.LENGTH_SHORT
//                    ).show()
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothWalkieTalkieTheme {
        Greeting("Android")
    }
}


@SuppressLint("MissingPermission")
@Composable
fun PairedDevicesList(
    pairedDevices: List<BluetoothDevice>,
    padding: PaddingValues,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    LazyColumn(modifier = Modifier.height(400.dp)) {
        items(pairedDevices) { device ->
            Row(
                modifier = Modifier.clickable(
                    onClick = { onDeviceClick(device) }),
            ) {
                Text(
                    modifier = Modifier.padding(all = 20.dp),
                    text = device.name ?: "",
                )
                Text(
                    modifier = Modifier.padding(all = 20.dp),
                    text = device.address ?: "",
                )
            }
        }

    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    discoveredDevices: LiveData<MutableSet<BluetoothDevice>>,
    pairedDevices: List<BluetoothDevice>,
    innerPadding: PaddingValues,
    onDiscoverable: () -> Unit,
    onDiscover: () -> Unit,
    onEnableServer: () -> Unit,
    onSend: () -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onStartPlay: () -> Unit,
    onStopPlay: () -> Unit,
    onSendAudio: () -> Unit,
    onDeviceClick: (device: BluetoothDevice) -> Unit,
) {
    val state by discoveredDevices.observeAsState(arrayListOf())
    Column(
        modifier = modifier
            .padding(top = 60.dp)
            .scrollable(
                enabled = true, state = ScrollableState { 0F }, orientation = Orientation.Vertical
            )
    ) {
        Button(
            onClick = { onDiscoverable() }) {
            Text(
                text = "Make discoverable",
            )
        }
        Button(
            onClick = { onDiscover() }) {
            Text(
                text = "Discover devices",
            )
        }
        Button(
            onClick = { onEnableServer() }) {
            Text(
                text = "Enable server",
            )
        }
        Button(
            onClick = { onSend() }) {
            Text(
                text = "Send data",
            )
        }
        Button(
            onClick = { onStartRecord() }) {
            Text(
                text = "Start record",
            )
        }
        Button(
            onClick = { onStopRecord() }) {
            Text(
                text = "Stop record",
            )
        }
        Button(
            onClick = { onStartPlay() }) {
            Text(
                text = "Start play",
            )
        }
        Button(
            onClick = { onStopPlay() }) {
            Text(
                text = "Stop play",
            )
        }
        Button(
            onClick = { onSendAudio() }) {
            Text(
                text = "Send audio",
            )
        }
        Text(
            text = "Bonded devices"
        )
        PairedDevicesList(pairedDevices.toList(), innerPadding, onDeviceClick)

        Text(
            text = "Discovered devices"
        )
        PairedDevicesList(state.toList(), innerPadding, onDeviceClick)


    }
}

