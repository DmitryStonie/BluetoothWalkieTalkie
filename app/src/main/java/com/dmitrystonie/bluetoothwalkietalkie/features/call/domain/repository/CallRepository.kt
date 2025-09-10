package com.dmitrystonie.bluetoothwalkietalkie.features.call.domain.repository

interface CallRepository {
    suspend fun startCall()
    suspend fun endCall()
}