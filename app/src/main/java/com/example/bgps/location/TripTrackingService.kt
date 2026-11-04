package com.example.bgps.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.bgps.MainActivity
import com.example.bgps.data.database.FuelVehicleType
import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.database.TripPointEntity
import com.example.bgps.data.database.VehicleType
import com.example.bgps.data.repository.TripRepository
import com.example.bgps.domain.trip.TripCalculator
import com.example.bgps.domain.trip.TripPoint
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

// Foreground service ghi vị trí trong lúc người dùng chủ động bấm "Bắt đầu
// hành trình". Chạy foreground (có thông báo cố định) để không cần quyền
// vị trí nền riêng cho chính việc ghi hành trình — quyền vị trí nền chỉ cần
// cho việc gắn toạ độ vào giao dịch tạo từ thông báo ngân hàng lúc không có
// hành trình nào đang chạy (xem LocationAccessScreen/LastKnownLocationStore).
@AndroidEntryPoint
class TripTrackingService : Service() {

    @Inject lateinit var tripRepository: TripRepository
    @Inject lateinit var lastKnownLocationStore: LastKnownLocationStore
    @Inject lateinit var activitySampler: TripActivitySampler
    @Inject lateinit var liveTripTracker: LiveTripTracker

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val points = mutableListOf<TripPoint>()
    private var activityPendingIntent: PendingIntent? = null
    private var fuelVehicleType: FuelVehicleType? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val point = TripPoint(location.latitude, location.longitude, System.currentTimeMillis())
            points.add(point)
            liveTripTracker.addPoint(point)
            lastKnownLocationStore.save(location.latitude, location.longitude)
            updateNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
        } else {
            val fuelType = intent?.getStringExtra(EXTRA_FUEL_VEHICLE_TYPE)
                ?.let { runCatching { FuelVehicleType.valueOf(it) }.getOrNull() }
            startTracking(fuelType)
        }
        return START_STICKY
    }

    private fun startTracking(fuelType: FuelVehicleType?) {
        points.clear()
        activitySampler.drain()
        fuelVehicleType = fuelType
        liveTripTracker.start(fuelType)
        ensureChannel()
        startForeground(NOTIFICATION_ID, buildNotification(0.0))
        startLocationUpdates()
        startActivityUpdates()
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, SAMPLE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
        } catch (e: SecurityException) {
            // Chưa có quyền vị trí — không ghi được gì, dừng service luôn
            // thay vì chạy foreground vô ích.
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startActivityUpdates() {
        val intent = Intent(this, TripActivityReceiver::class.java).apply {
            action = TripActivityReceiver.ACTION_ACTIVITY_UPDATE
        }
        // Phải dùng FLAG_MUTABLE: hệ thống cần ghi thêm ActivityRecognitionResult
        // vào Intent trước khi gửi broadcast, PendingIntent bất biến sẽ chặn việc đó.
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        activityPendingIntent = pendingIntent
        try {
            ActivityRecognition.getClient(this)
                .requestActivityUpdates(ACTIVITY_UPDATE_INTERVAL_MS, pendingIntent)
        } catch (e: SecurityException) {
            // Chưa có quyền ACTIVITY_RECOGNITION — bỏ qua, lúc lưu chuyến đi
            // sẽ dùng phân loại theo tốc độ làm phương án dự phòng.
        }
    }

    private fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        activityPendingIntent?.let { ActivityRecognition.getClient(this).removeActivityUpdates(it) }
        liveTripTracker.stop()

        val snapshot = points.toList()
        val activityHint = dominantVehicleType(activitySampler.drain())
        scope.launch {
            if (snapshot.size >= 2) {
                val summary = TripCalculator.summarize(snapshot, activityHint)
                val trip = TripEntity(
                    startTime = snapshot.first().timestampMillis,
                    endTime = snapshot.last().timestampMillis,
                    distanceMeters = summary.distanceMeters,
                    vehicleType = summary.vehicleType,
                    startLatitude = snapshot.first().latitude,
                    startLongitude = snapshot.first().longitude,
                    endLatitude = snapshot.last().latitude,
                    endLongitude = snapshot.last().longitude,
                    fuelVehicleType = fuelVehicleType
                )
                // Lưu cả đường đi (không chỉ điểm đầu/cuối) để vẽ lại được
                // trên bản đồ ở màn chi tiết hành trình.
                val pointEntities = snapshot.mapIndexed { index, point ->
                    TripPointEntity(
                        tripId = trip.id,
                        latitude = point.latitude,
                        longitude = point.longitude,
                        timestampMillis = point.timestampMillis,
                        sequence = index
                    )
                }
                tripRepository.saveWithPoints(trip, pointEntities)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun dominantVehicleType(samples: List<Int>): VehicleType? {
        if (samples.isEmpty()) return null
        val mostCommon = samples.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        return when (mostCommon) {
            DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> VehicleType.WALKING
            DetectedActivity.ON_BICYCLE -> VehicleType.CYCLING
            DetectedActivity.IN_VEHICLE -> VehicleType.VEHICLE
            else -> null
        }
    }

    private fun buildNotification(distanceMeters: Double): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val km = distanceMeters / 1000.0
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Đang ghi hành trình")
            .setContentText("Quãng đường: ${(km * 10).roundToInt() / 10.0} km")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val distance = TripCalculator.totalDistanceMeters(points)
        getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, buildNotification(distance))
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(CHANNEL_ID) == null) {
                manager?.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Ghi hành trình", NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.example.bgps.action.STOP_TRIP"
        const val EXTRA_FUEL_VEHICLE_TYPE = "com.example.bgps.extra.FUEL_VEHICLE_TYPE"
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "trip_tracking"
        private const val SAMPLE_INTERVAL_MS = 15_000L
        private const val MIN_DISTANCE_METERS = 20f
        private const val ACTIVITY_UPDATE_INTERVAL_MS = 15_000L
    }
}
