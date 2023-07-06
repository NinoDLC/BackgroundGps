package fr.delcey.backgroundgps.domain.location.model

import java.time.LocalTime

data class LocationEntity(
    val latitude: Latitude,
    val longitude: Longitude,
    val time: LocalTime,
)