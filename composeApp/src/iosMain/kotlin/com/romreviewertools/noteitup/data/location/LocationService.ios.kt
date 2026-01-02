package com.romreviewertools.noteitup.data.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class LocationService {

    private val locationManager = CLLocationManager()
    private val geocoder = CLGeocoder()
    private var locationDelegate: LocationDelegate? = null

    actual fun getPermissionStatus(): LocationPermissionStatus {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> LocationPermissionStatus.GRANTED
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> LocationPermissionStatus.DENIED
            kCLAuthorizationStatusNotDetermined -> LocationPermissionStatus.NOT_DETERMINED
            else -> LocationPermissionStatus.NOT_DETERMINED
        }
    }

    actual suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didChangeAuthorizationStatus: CLAuthorizationStatus
            ) {
                when (didChangeAuthorizationStatus) {
                    kCLAuthorizationStatusAuthorizedAlways,
                    kCLAuthorizationStatusAuthorizedWhenInUse -> {
                        continuation.resume(true)
                    }
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        continuation.resume(false)
                    }
                    kCLAuthorizationStatusNotDetermined -> {
                        // Still waiting for user decision
                    }
                    else -> {
                        continuation.resume(false)
                    }
                }
            }
        }

        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
    }

    actual suspend fun getCurrentLocation(): Result<LocationResult> =
        suspendCancellableCoroutine { continuation ->
            if (getPermissionStatus() != LocationPermissionStatus.GRANTED) {
                continuation.resume(Result.failure(Exception("Location permission not granted")))
                return@suspendCancellableCoroutine
            }

            if (!CLLocationManager.locationServicesEnabled()) {
                continuation.resume(Result.failure(Exception("Location services are disabled")))
                return@suspendCancellableCoroutine
            }

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    if (location != null) {
                        locationManager.stopUpdatingLocation()
                        location.coordinate.useContents {
                            continuation.resume(
                                Result.success(
                                    LocationResult(
                                        latitude = latitude,
                                        longitude = longitude,
                                        accuracy = location.horizontalAccuracy.toFloat()
                                    )
                                )
                            )
                        }
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    locationManager.stopUpdatingLocation()
                    continuation.resume(Result.failure(Exception(didFailWithError.localizedDescription)))
                }
            }

            locationDelegate = delegate
            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.startUpdatingLocation()

            continuation.invokeOnCancellation {
                locationManager.stopUpdatingLocation()
            }
        }

    actual suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val location = CLLocation(latitude = latitude, longitude = longitude)

            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception(error.localizedDescription)))
                    return@reverseGeocodeLocation
                }

                val placemark = placemarks?.firstOrNull()
                if (placemark != null) {
                    val addressParts = mutableListOf<String>()

                    @Suppress("UNCHECKED_CAST")
                    val pm = placemark as? platform.CoreLocation.CLPlacemark
                    pm?.let {
                        it.thoroughfare?.let { street -> addressParts.add(street) }
                        it.locality?.let { city -> addressParts.add(city) }
                        it.administrativeArea?.let { state -> addressParts.add(state) }
                        it.country?.let { country -> addressParts.add(country) }
                    }

                    val address = if (addressParts.isNotEmpty()) {
                        addressParts.joinToString(", ")
                    } else {
                        "Unknown location"
                    }
                    continuation.resume(Result.success(address))
                } else {
                    continuation.resume(Result.success("Unknown location"))
                }
            }
        }

    actual fun isLocationAvailable(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }
}
