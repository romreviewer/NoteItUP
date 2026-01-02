package com.romreviewertools.noteitup.data.location

actual class LocationService {

    actual fun getPermissionStatus(): LocationPermissionStatus {
        // Desktop doesn't have location permission concept
        return LocationPermissionStatus.DENIED
    }

    actual suspend fun requestPermission(): Boolean {
        // Location not available on desktop
        return false
    }

    actual suspend fun getCurrentLocation(): Result<LocationResult> {
        return Result.failure(Exception("Location services are not available on desktop"))
    }

    actual suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String> {
        return Result.failure(Exception("Geocoding is not available on desktop"))
    }

    actual fun isLocationAvailable(): Boolean {
        return false
    }
}
