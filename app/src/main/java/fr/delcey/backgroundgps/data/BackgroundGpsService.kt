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
class BackgroundGpsService : Service() {

    @Inject
    lateinit var gpsLocationRepository: GpsLocationRepository

    @Inject
    lateinit var currentRideRepository: CurrentRideRepository

    private val scope = CoroutineScope(EmptyCoroutineContext)

    override fun onCreate() {
        super.onCreate()

        Log.v("BackgroundGpsService", "onCreate() called")
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("BackgroundGpsService", "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")

        val channelId = "RIDES"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ongoing rides"
            val descriptionText = "Notify you're doing a ride"
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
            .setContentTitle("Ongoing Ride")
            .setContentText("Let's go do the biiiiike")
            .setSmallIcon(R.drawable.share_location)
            .setContentIntent(onNotificationClickPendingIntent)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(R.drawable.stop, "Finish the ride", onStopRideClickPendingIntent)
            .build()
        startForeground(1, notification)

        scope.launch {
            gpsLocationRepository.getLocation().collect { location ->
                Log.v("BackgroundGpsService", "getLocation().collect() called with: location = $location")
                currentRideRepository.locationsFlow.update { it + location }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.v("BackgroundGpsService", "onDestroy() called")

        scope.cancel()
    }
}