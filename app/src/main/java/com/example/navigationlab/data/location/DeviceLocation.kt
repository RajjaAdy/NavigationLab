package com.example.navigationlab.data.location

// Model domain: UI tidak bergantung langsung pada android.location.Location
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)