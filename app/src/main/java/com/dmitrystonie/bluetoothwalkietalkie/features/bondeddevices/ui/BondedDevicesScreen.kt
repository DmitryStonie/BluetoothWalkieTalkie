package com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.presentation.BondedDevicesScreenState
import com.dmitrystonie.bluetoothwalkietalkie.features.bondeddevices.presentation.BondedDevicesViewModel
import com.dmitrystonie.bluetoothwalkietalkie.ui.components.DevicesList

@Composable
fun BondedDevicesScreen() {
    val viewModel = hiltViewModel<BondedDevicesViewModel>()
    val state by viewModel.state.observeAsState(BondedDevicesScreenState.Initial)

    LaunchedEffect(Unit) {
        viewModel.loadBondedDevices()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Bonded devices",
            fontSize = 20.sp
        )
        when(state){
            is BondedDevicesScreenState.Content -> {
                DevicesList(
                    devices = (state as BondedDevicesScreenState.Content).devices,
                    padding = PaddingValues(0.dp),
                    onDeviceClick = { }
                )
            }
            BondedDevicesScreenState.Initial -> {}
            BondedDevicesScreenState.Unavailable -> {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bonded devices",
                        fontSize = 30.sp,
                    )
                }
            }
        }
    }
}