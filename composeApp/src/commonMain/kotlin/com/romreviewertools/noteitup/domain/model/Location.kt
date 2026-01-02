package com.romreviewertools.noteitup.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val placeName: String? = null
)
