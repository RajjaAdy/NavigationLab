package com.example.navigationlab.ui.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog permission ditangani di UI (bukan di ViewModel)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        // Approximate (COARSE saja) tetap dianggap izin yang sah
        if (coarse || fine) viewModel.refreshLocation()
    }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Lokasi", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = {
            if (hasLocationPermission) {
                viewModel.refreshLocation()
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }) {
            Text("Ambil Lokasi Saya")
        }

        when (val state = uiState) {
            LocationUiState.Idle -> Text("Lokasi belum diminta")
            LocationUiState.Loading -> CircularProgressIndicator()
            is LocationUiState.Success -> {
                Text("Latitude: ${state.location.latitude}")
                Text("Longitude: ${state.location.longitude}")
                Text("Accuracy: ${state.location.accuracy ?: 0f} m")
            }
            is LocationUiState.Error -> Text(state.message)
        }

        OutlinedButton(onClick = onBack) {
            Text("Kembali")
        }
    }
}