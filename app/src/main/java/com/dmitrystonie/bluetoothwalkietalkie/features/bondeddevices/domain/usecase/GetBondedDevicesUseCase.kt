package com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.domain.usecase

import com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.domain.repository.BondedDevicesRepository

class GetBondedDevicesUseCase (private val repository: BondedDevicesRepository) {
    suspend fun invoke() = repository.getBondedDevices()
}