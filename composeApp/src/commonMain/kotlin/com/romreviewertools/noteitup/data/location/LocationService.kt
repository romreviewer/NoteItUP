package com.romreviewertools.noteitup.data.location

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)

enum class LocationPermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED
}

sealed class LocationError {
    data object PermissionDenied : LocationError()
    data object LocationDisabled : LocationError()
    data object Timeout : LocationError()
    data object NotAvailable : LocationError()
    data class Unknown(val message: String) : LocationError()
}

expect class LocationService {
    fun getPermissionStatus(): LocationPermissionStatus
    suspend fun requestPermission(): Boolean
    suspend fun getCurrentLocation(): Result<LocationResult>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String>
    fun isLocationAvailable(): Boolean
}
