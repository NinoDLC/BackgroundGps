package fr.delcey.backgroundgps.domain.location

import fr.delcey.backgroundgps.domain.location.model.LocationEntity
import kotlinx.coroutines.flow.Flow

interface GpsLocationRepository {
    fun getLocation(): Flow<LocationEntity>
}