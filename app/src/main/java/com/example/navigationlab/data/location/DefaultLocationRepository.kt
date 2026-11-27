package com.example.navigationlab.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class DefaultLocationRepository(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationRepository {

    // Izin sudah dicek di dalam fungsi ini sebelum client dipanggil
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<DeviceLocation> {
        return try {
            val fineGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            check(fineGranted || coarseGranted) { "Location permission belum diberikan" }

            // FINE tersedia -> akurasi tinggi; hanya COARSE -> approximate
            val priority = if (fineGranted) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            val location = client.getCurrentLocation(priority, null).await()
                ?: error("Lokasi belum tersedia")

            Result.success(
                DeviceLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
            )
        } catch (e: CancellationException) {
            throw e // jangan ditelan: pembatalan coroutine harus tetap berjalan
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}