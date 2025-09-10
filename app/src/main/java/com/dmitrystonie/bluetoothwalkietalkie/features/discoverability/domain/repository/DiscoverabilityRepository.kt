package com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.domain.repository

interface DiscoverabilityRepository {
    fun getDiscoverability(): Boolean
    fun enableDiscoverability()
    fun disableDiscoverability()
}