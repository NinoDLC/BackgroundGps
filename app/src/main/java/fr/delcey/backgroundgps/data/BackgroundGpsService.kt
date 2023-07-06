package fr.delcey.backgroundgps.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.content.getSystemService
import dagger.hilt.android.AndroidEntryPoint
import fr.delcey.backgroundgps.R
import fr.delcey.backgroundgps.domain.location.GpsLocationRepository
import fr.delcey.backgroundgps.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

@AndroidEntryPoint
class BackgroundGpsService @Inject constructor() : Service() {

    @Inject
    lateinit var gpsLocationRepository: GpsLocationRepository

    @Inject
    lateinit var lastLocationRepository: CurrentRideRepository

    private val scope = CoroutineScope(EmptyCoroutineContext)

    override fun onCreate() {
        super.onCreate()

        Log.d("BackgroundGpsService", "onCreate() called")
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BackgroundGpsService", "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")

        val channelId = "RIDES"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ride en cours"
            val descriptionText = "Apparait lorsque vous faites une ride"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService<NotificationManager>()
            notificationManager?.createNotificationChannel(channel)
        }

        val onNotificationClickPendingIntent = PendingIntent.getActivity(
            this,
            0,
            MainActivity.navigate(this),
            PendingIntent.FLAG_IMMUTABLE
        )

        val onStopRideClickPendingIntent = PendingIntent.getActivity(
            this,
            1,
            MainActivity.navigate(this, stopRide = true),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Ride en cours")
            .setContentText("Let's go faire le véloooo")
            .setSmallIcon(R.drawable.share_location)
            .setContentIntent(onNotificationClickPendingIntent)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(R.drawable.stop, "Finir la ride", onStopRideClickPendingIntent)
            .build()
        startForeground(1, notification)

        scope.launch {
            gpsLocationRepository.getLocation().collect { location ->
                Log.d("BackgroundGpsService", "getLocation().collect() called with: location = $location")
                lastLocationRepository.locationsFlow.update { it + location }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("BackgroundGpsService", "onDestroy() called")

        scope.cancel()
    }
}