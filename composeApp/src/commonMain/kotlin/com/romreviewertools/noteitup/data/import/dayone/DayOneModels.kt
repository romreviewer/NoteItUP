package com.romreviewertools.noteitup.data.import.dayone

import kotlinx.serialization.Serializable

@Serializable
data class DayOneExport(
    val metadata: DayOneMetadata,
    val entries: List<DayOneEntry>
)

@Serializable
data class DayOneMetadata(
    val version: String
)

@Serializable
data class DayOneEntry(
    val uuid: String,
    val creationDate: String, // ISO 8601
    val modifiedDate: String? = null,
    val text: String,
    val tags: List<String> = emptyList(),
    val starred: Boolean = false,
    val timeZone: String? = null,
    val location: DayOneLocation? = null,
    val photos: List<DayOnePhoto> = emptyList(),
    val weather: DayOneWeather? = null
)

@Serializable
data class DayOneLocation(
    val latitude: Double,
    val longitude: Double,
    val placeName: String? = null,
    val localityName: String? = null,
    val administrativeArea: String? = null,
    val country: String? = null
)

@Serializable
data class DayOnePhoto(
    val identifier: String,
    val type: String,
    val creationDate: String? = null,
    val filename: String? = null,
    val isPrimary: Boolean = false
)

@Serializable
data class DayOneWeather(
    val conditionsDescription: String? = null,
    val temperatureCelsius: Double? = null
)
