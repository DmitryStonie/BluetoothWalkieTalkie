package com.dmitrystonie.bluetoothwalkietalkie.domain.entity

data class Device(
    val name: String,
    val mac: String,
    val isConnected: Boolean
)
