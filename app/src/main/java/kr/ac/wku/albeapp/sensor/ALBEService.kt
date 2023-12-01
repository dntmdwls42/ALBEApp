package kr.ac.wku.albeapp.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kr.ac.wku.albeapp.R

// 센서 액티비티 백그라운드 동작을 담당하는곳
class ALBEService : Service() {
    private lateinit var sensorManager: SensorManager
    private val CHANNEL_ID = "ForegroundServiceChannel"

    private val sensorStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sensorState = intent?.getStringExtra("sensor_state")
            val timerInfo = intent?.getStringExtra("timer_info")

            val notificationContent = """$sensorState"""

            updateNotification(notificationContent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        Log.w("ALBEService", "Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("센서 서비스가 실행 중입니다.")
        startForeground(1, notification)

        Log.w("ALBEService", "Service Started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.w("ALBEService", "Service Destroyed")
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, SensorActvitiy::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("센서동작")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setSmallIcon(R.drawable.albe)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(sensorState: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(sensorState)
        notificationManager.notify(1, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}