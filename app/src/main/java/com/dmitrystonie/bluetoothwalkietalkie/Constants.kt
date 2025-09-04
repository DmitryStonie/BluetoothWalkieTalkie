package com.dmitrystonie.bluetoothwalkietalkie

import android.media.AudioFormat

object Constants {
    const val SAMPLE_RATE = 16000
    const val CHANNEL_MASK_IN = AudioFormat.CHANNEL_IN_MONO
    const val CHANNEL_MASK_OUT = AudioFormat.CHANNEL_OUT_MONO
    const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
}