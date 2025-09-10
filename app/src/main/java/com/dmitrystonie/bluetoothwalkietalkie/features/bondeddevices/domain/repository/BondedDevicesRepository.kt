package com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.domain.repository

import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device

interface BondedDevicesRepository {
    suspend fun getBondedDevices(): List<Device>?
}