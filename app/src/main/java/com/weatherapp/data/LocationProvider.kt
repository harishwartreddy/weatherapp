package com.weatherapp.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.weatherapp.domain.model.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationProvider @Inject constructor(
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): Result<GeoLocation> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }

        // Try multiple strategies as fallback - getCurrentLocation can fail on some devices
        tryGetCurrentLocation()?.let { return Result.success(it) }
        tryGetLastLocation()?.let { return Result.success(it) }
        tryRequestSingleLocation()?.let { return Result.success(it) }

        return Result.failure(Exception("Unable to get location"))
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetCurrentLocation(): GeoLocation? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val cancellationToken = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(GeoLocation(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener {
                    continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    cancellationToken.cancel()
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetLastLocation(): GeoLocation? {
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(GeoLocation(location.latitude, location.longitude))
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryRequestSingleLocation(): GeoLocation? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    1000L
                ).setMaxUpdates(1).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        val location = result.lastLocation
                        if (location != null) {
                            continuation.resume(GeoLocation(location.latitude, location.longitude))
                        } else {
                            continuation.resume(null)
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                continuation.invokeOnCancellation {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }
}
