package com.example.myproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationProvider(context: Context) {

    private val context = context.applicationContext
    private val locationManager = this.context.getSystemService(LocationManager::class.java)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location {
        if (!hasPermission()) throw LocationPermissionException()
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) throw LocationDisabledException()

        val providers = locationManager.getProviders(true)
        val lastKnown = providers
            .mapNotNull { locationManager.getLastKnownLocation(it) }
            .maxByOrNull { it.elapsedRealtimeNanos }
        if (lastKnown != null && lastKnown.ageMillis() < MAX_LOCATION_AGE_MS) return lastKnown

        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { it in providers }
        val fresh = provider?.let { withTimeoutOrNull(LOCATION_TIMEOUT_MS) { requestLocation(it) } }
        return fresh ?: lastKnown ?: throw LocationUnavailableException()
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestLocation(provider: String): Location? =
        suspendCancellableCoroutine { cont ->
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                locationManager,
                provider,
                signal,
                ContextCompat.getMainExecutor(context)
            ) { location -> if (cont.isActive) cont.resume(location) }
        }

    private fun Location.ageMillis(): Long =
        (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000

    private companion object {
        const val MAX_LOCATION_AGE_MS = 10 * 60 * 1000L
        const val LOCATION_TIMEOUT_MS = 15_000L
    }
}
