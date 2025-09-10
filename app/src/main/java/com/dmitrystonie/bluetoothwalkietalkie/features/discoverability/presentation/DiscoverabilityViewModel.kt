package com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.domain.usecase.DisableDiscoverabilityUseCase
import com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.domain.usecase.EnableDiscoverabilityUseCase
import com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.domain.usecase.GetDiscoverabilityUseCase

class DiscoverabilityViewModel(
    private val getDiscoverabilityUseCase: GetDiscoverabilityUseCase,
    private val enableDiscoverabilityUseCase: EnableDiscoverabilityUseCase,
    private val disableDiscoverabilityUseCase: DisableDiscoverabilityUseCase
) : ViewModel() {
    private val _state = MutableLiveData<DiscoverabilityState>(DiscoverabilityState.Unknown)
    val state: LiveData<DiscoverabilityState> = _state

    fun getDiscoverability() {
        if (getDiscoverabilityUseCase()) {
            _state.value = DiscoverabilityState.Discoverable
        } else {
            _state.value = DiscoverabilityState.Undiscoverable
        }
    }

    fun enableDiscoverability() {
        enableDiscoverabilityUseCase()
        if (getDiscoverabilityUseCase()) {
            _state.value = DiscoverabilityState.Discoverable
        } else {
            _state.value = DiscoverabilityState.Undiscoverable
        }
    }

    fun disableDiscoverability() {
        disableDiscoverabilityUseCase()
        if (getDiscoverabilityUseCase()) {
            _state.value = DiscoverabilityState.Discoverable
        } else{
            _state.value = DiscoverabilityState.Undiscoverable
        }
    }

}