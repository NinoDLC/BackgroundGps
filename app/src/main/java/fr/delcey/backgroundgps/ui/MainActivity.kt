package fr.delcey.backgroundgps.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.delcey.backgroundgps.data.BackgroundGpsService
import fr.delcey.backgroundgps.data.CurrentRideRepository
import fr.delcey.backgroundgps.databinding.MainActivityBinding
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.time.toKotlinDuration

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_STOP_RIDE = "KEY_STOP_RIDE"

        fun navigate(context: Context, stopRide: Boolean = false): Intent = Intent(context, MainActivity::class.java).apply {
            putExtra(KEY_STOP_RIDE, stopRide)
        }
    }

    @Inject
    lateinit var currentRideRepository: CurrentRideRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainActivityBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        if (intent.getBooleanExtra(KEY_STOP_RIDE, false)) {
            finishRide()
        }

        binding.activityMainButtonStartService.setOnClickListener {
            currentRideRepository.start = Instant.now()
            ContextCompat.startForegroundService(this, Intent(this, BackgroundGpsService::class.java))
        }

        binding.activityMainButtonStopService.setOnClickListener {
            finishRide()
        }

        binding.activityMainButtonAskPermissions.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                0
            )
        }

        val adapter = MainAdapter()
        binding.activityMainRecyclerView.adapter = adapter

        lifecycleScope.launch {
            currentRideRepository.locationsFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { locationEntities ->
                    adapter.submitList(
                        locationEntities.map {
                            MainViewState(
                                location = "${it.latitude.value.toString().take(7)} , ${it.longitude.value.toString().take(7)}",
                                time = it.time.toString()
                            )
                        }
                    )
                }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.getBooleanExtra(KEY_STOP_RIDE, false) == true) {
            finishRide()
        }
    }

    private fun finishRide() {
        val capturedStart = currentRideRepository.start ?: return
        val duration = Duration.between(
            capturedStart,
            Instant.now()
        ).toKotlinDuration()

        Toast.makeText(
            this@MainActivity,
            "${currentRideRepository.locationsFlow.value.size} locations aggregated in $duration",
            Toast.LENGTH_LONG
        ).show()

        stopService(Intent(this, BackgroundGpsService::class.java))

        currentRideRepository.reset()
    }
}