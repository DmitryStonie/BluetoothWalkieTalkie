package com.dmitrystonie.bluetoothwalkietalkie.features.discoverability.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun EnableDiscoverabilityButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
    ){
        Text(
            text = "Enable discoverability",
        )
    }
}

@Composable
fun DisableDiscoverabilityButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
    ){
        Text(
            text = "Disable discoverability",
        )
    }
}