package com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.presentation

sealed interface DiscoverabilityState {
    data object Discoverable: DiscoverabilityState
    data object Undiscoverable: DiscoverabilityState
    data object Unknown: DiscoverabilityState
}