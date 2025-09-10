package com.dmitrystonie.bluetoothwalkietalkie.features.call.data.datasource

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import com.dmitrystonie.bluetoothwalkietalkie.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

class BluetoothService @Inject constructor(private val mBtAdapter: BluetoothAdapter) {
    val nameSecure: String = "BluetoothChatSecure"
    val uuidSecure: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

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
                    nameSecure, uuidSecure
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
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

    }

    private inner class ConnectThread(
        private val device: BluetoothDevice, private val adapter: BluetoothAdapter
    ) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(uuidSecure)
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
        mmSocket: BluetoothSocket
    ) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        val mmOutStream: OutputStream = mmSocket.outputStream
        val bufferSize = 4096
        private val mmBuffer: ByteArray = ByteArray(bufferSize)

        private var audioTrack: AudioTrack? = null

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

                if (audioTrack == null) {
                    initAudioTrack()
                    audioTrack!!.play()
                }

                audioTrack!!.write(mmBuffer, 0, numBytes)
            }
        }

        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("INFO", "Error occurred when sending data", e)
                return
            }
        }

        private fun initAudioTrack(){
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val audioFormat = AudioFormat.Builder()
                .setSampleRate(Constants.SAMPLE_RATE)
                .setEncoding(Constants.ENCODING)
                .setChannelMask(Constants.CHANNEL_MASK_OUT)
                .build()
            audioTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM,
                0
            )
        }
    }

    inner class StreamMicThread(
        val connectedThread: ConnectedThread
    ) : Thread() {
        private var recorder: AudioRecord? = null

        @SuppressLint("MissingPermission")
        override fun run() {
            var bytesRead: Int
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                Constants.SAMPLE_RATE,
                Constants.CHANNEL_MASK_IN,
                Constants.ENCODING,
                AudioRecord.getMinBufferSize(
                    Constants.SAMPLE_RATE,
                    Constants.CHANNEL_MASK_IN,
                    Constants.ENCODING
                )
            )
            Log.d("INFO", "Recorder initialized")
            recorder!!.startRecording()

            val buffer = ByteArray(
                AudioRecord.getMinBufferSize(
                    Constants.SAMPLE_RATE,
                    Constants.CHANNEL_MASK_IN,
                    Constants.ENCODING
                ))

            while (true) {
                bytesRead = recorder!!.read(buffer, 0, buffer.size)
                connectedThread.write(buffer)
                println("MinBufferSize: $bytesRead")
            }

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
            socket
        )
        connectedThread!!.start()
    }

    private fun initStreamMicThread() {
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

    fun startCall() {
        initStreamMicThread()
    }

    fun endCall() {
        if (streamMicThread != null) {
            streamMicThread!!.interrupt()
        }
        Log.d("INFO", "Call ended")
    }
}