package com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.domain.usecase.GetBondedDevicesUseCase
import kotlinx.coroutines.launch

class BondedDevicesViewModel(private val getBondedDevicesUseCase: GetBondedDevicesUseCase): ViewModel() {
    private val _state = MutableLiveData<BondedDevicesScreenState>(BondedDevicesScreenState.Initial)
    val state: LiveData<BondedDevicesScreenState> = _state

    fun loadBondedDevices(){
        viewModelScope.launch {
            val devices = getBondedDevicesUseCase.invoke()
            if (devices == null) {
                _state.value = BondedDevicesScreenState.Unavailable
            } else {
                _state.value = BondedDevicesScreenState.Content(devices)
            }
        }
    }
}