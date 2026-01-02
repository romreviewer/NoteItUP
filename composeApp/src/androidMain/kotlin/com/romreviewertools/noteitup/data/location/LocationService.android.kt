package com.romreviewertools.noteitup.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

actual class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var pendingPermissionCallback: ((Boolean) -> Unit)? = null

    actual fun getPermissionStatus(): LocationPermissionStatus {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return when {
            fineLocation == PackageManager.PERMISSION_GRANTED ||
                    coarseLocation == PackageManager.PERMISSION_GRANTED -> LocationPermissionStatus.GRANTED
            else -> LocationPermissionStatus.DENIED
        }
    }

    actual suspend fun requestPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            pendingPermissionCallback = { granted ->
                pendingPermissionCallback = null
                continuation.resume(granted)
            }
            // Permission request is triggered by the UI layer
            // This will be resolved when onPermissionResult is called
        }

    fun onPermissionResult(granted: Boolean) {
        pendingPermissionCallback?.invoke(granted)
    }

    actual suspend fun getCurrentLocation(): Result<LocationResult> = withContext(Dispatchers.IO) {
        if (getPermissionStatus() != LocationPermissionStatus.GRANTED) {
            return@withContext Result.failure(Exception("Location permission not granted"))
        }

        if (!isLocationEnabled()) {
            return@withContext Result.failure(Exception("Location services are disabled"))
        }

        suspendCancellableCoroutine { continuation ->
            try {
                val cancellationToken = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            Result.success(
                                LocationResult(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    accuracy = location.accuracy
                                )
                            )
                        )
                    } else {
                        continuation.resume(Result.failure(Exception("Location is null")))
                    }
                }.addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }

                continuation.invokeOnCancellation {
                    cancellationToken.cancel()
                }
            } catch (e: SecurityException) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    actual suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            if (addresses.isNotEmpty()) {
                                val address = addresses[0]
                                val addressText = buildString {
                                    address.thoroughfare?.let { append(it) }
                                    address.locality?.let {
                                        if (isNotEmpty()) append(", ")
                                        append(it)
                                    }
                                    address.adminArea?.let {
                                        if (isNotEmpty()) append(", ")
                                        append(it)
                                    }
                                    address.countryName?.let {
                                        if (isNotEmpty()) append(", ")
                                        append(it)
                                    }
                                }
                                continuation.resume(addressText.ifEmpty { "Unknown location" })
                            } else {
                                continuation.resume("Unknown location")
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        buildString {
                            address.thoroughfare?.let { append(it) }
                            address.locality?.let {
                                if (isNotEmpty()) append(", ")
                                append(it)
                            }
                            address.adminArea?.let {
                                if (isNotEmpty()) append(", ")
                                append(it)
                            }
                            address.countryName?.let {
                                if (isNotEmpty()) append(", ")
                                append(it)
                            }
                        }.ifEmpty { "Unknown location" }
                    } else {
                        "Unknown location"
                    }
                }
            }
        }

    actual fun isLocationAvailable(): Boolean {
        return isLocationEnabled()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
