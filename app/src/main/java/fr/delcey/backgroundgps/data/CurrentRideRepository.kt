package fr.delcey.backgroundgps.data

import fr.delcey.backgroundgps.domain.location.model.LocationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentRideRepository @Inject constructor() {

    val locationsFlow = MutableStateFlow<List<LocationEntity>>(emptyList())

    var start : Instant? = null

    fun reset() {
        locationsFlow.value = emptyList()
        start = null
    }
}
