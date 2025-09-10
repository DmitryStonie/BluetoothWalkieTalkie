package com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.domain.usecase

import com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.domain.repository.DiscoverabilityRepository

class DisableDiscoverabilityUseCase(val repository: DiscoverabilityRepository) {
    operator fun invoke() = repository.disableDiscoverability()
}