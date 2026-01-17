
package hu.zoli.wifibuhera

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class KeepAliveService : Service() {

    private lateinit var wifiManager: WifiManager
    private var wifiLock: WifiManager.WifiLock? = null

    private lateinit var powerManager: PowerManager
    private var cpuWakeLock: PowerManager.WakeLock? = null

    private lateinit var fused: FusedLocationProviderClient
    private var fallbackLocationManager: LocationManager? = null

    override fun onCreate() {
        super.onCreate()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        fused = LocationServices.getFusedLocationProviderClient(this)
        fallbackLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        startForeground(NOTIF_ID, buildNotification())
        acquireWifiLock()
        acquireCpuWakeLock()
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        releaseWifiLock()
        releaseCpuWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "keep_wifi_gps_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Wi‑Fi és GPS ébrentartás",
                NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("WIFIbuhera fut")
            .setContentText("Wi‑Fi & GPS ébren tartva")
            .setSmallIcon(R.drawable.ic_stat_keepalive)
            .setOngoing(true)
            .build()
    }

    private fun acquireWifiLock() {
        try {
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "WIFIbuhera:WifiLock").apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (_: Exception) { }
    }

    private fun releaseWifiLock() {
        try {
            wifiLock?.let { if (it.isHeld) it.release() }
            wifiLock = null
        } catch (_: Exception) { }
    }

    private fun acquireCpuWakeLock() {
        try {
            cpuWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WIFIbuhera:CpuLock").apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (_: Exception) { }
    }

    private fun releaseCpuWakeLock() {
        try {
            cpuWakeLock?.let { if (it.isHeld) it.release() }
            cpuWakeLock = null
        } catch (_: Exception) { }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(0f)
            .build()
        try {
            fused.requestLocationUpdates(req, fusedCallback, Looper.getMainLooper())
        } catch (_: Exception) {
            tryFallbackGps()
        }
    }

    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            // helyfrissítések feldolgozása
        }
    }

    private fun tryFallbackGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        try {
            fallbackLocationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                0f,
                gpsListener,
                Looper.getMainLooper()
            )
        } catch (_: Exception) { }
    }

    private val gpsListener = LocationListener { _: Location ->
        // GPS fallback frissítés
    }

    private fun stopLocationUpdates() {
        try { fused.removeLocationUpdates(fusedCallback) } catch (_: Exception) {}
        try { fallbackLocationManager?.removeUpdates(gpsListener) } catch (_: Exception) {}
    }

    companion object {
        private const val NOTIF_ID = 1001
    }
}
