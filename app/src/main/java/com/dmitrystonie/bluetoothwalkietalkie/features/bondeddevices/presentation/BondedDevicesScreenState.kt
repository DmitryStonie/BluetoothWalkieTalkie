package com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.presentation

import com.dmitrystonie.bluetoothwalkietalkie.domain.entity.Device

sealed interface BondedDevicesScreenState{
    data object Initial: BondedDevicesScreenState
    data class Content(val devices: List<Device>): BondedDevicesScreenState
    data object Unavailable: BondedDevicesScreenState
}