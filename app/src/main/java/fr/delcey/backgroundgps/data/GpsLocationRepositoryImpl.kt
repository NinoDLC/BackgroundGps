package fr.delcey.backgroundgps.data

import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import fr.delcey.backgroundgps.domain.location.GpsLocationRepository
import fr.delcey.backgroundgps.domain.location.model.Latitude
import fr.delcey.backgroundgps.domain.location.model.LocationEntity
import fr.delcey.backgroundgps.domain.location.model.Longitude
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class GpsLocationRepositoryImpl @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
) : GpsLocationRepository {

    companion object {
        private val LOCATION_REQUEST_INTERVAL = 5.seconds
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    override fun getLocation(): Flow<LocationEntity> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    trySend(
                        LocationEntity(
                            latitude = Latitude(it.latitude),
                            longitude = Longitude(it.longitude),
                            time = LocalTime.now(),
                        )
                    )
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            LocationRequest.Builder(LOCATION_REQUEST_INTERVAL.inWholeMilliseconds)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            Dispatchers.IO.asExecutor(),
            callback,
        )

        awaitClose { fusedLocationProviderClient.removeLocationUpdates(callback) }
    }.flowOn(Dispatchers.IO)
}