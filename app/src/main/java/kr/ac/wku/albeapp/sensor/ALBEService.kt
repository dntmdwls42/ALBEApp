package kr.ac.wku.albeapp.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.logins.LoginSession
import kr.ac.wku.albeapp.logins.UserState

// Handler 선언
private val handler = Handler(Looper.getMainLooper())


// 센서 액티비티 백그라운드 동작을 담당하는곳
class ALBEService : Service() {
    private lateinit var sensorManager: SensorManager
    private val CHANNEL_ID = "ForegroundServiceChannel"

    var userStatus = UserState.ACTIVE.status // 유저 상태

    // Runnable 선언
    private val timerRunnable = object : Runnable {
        var seconds = 0

        override fun run() {

            if (userStatus != UserState.INACTIVE.status) {
                return
            }

            seconds++

            if (seconds % 10 == 0) {
                val sensorState = "친구의 상태가 위험합니다, $seconds 초 경과"
                updateNotification(sensorState, true)
            }

            handler.postDelayed(this, 1000L) // 1초마다 실행
        }
    }

    private val sensorStateReceiver = object : BroadcastReceiver() {
        var previousState = userStatus

        override fun onReceive(context: Context?, intent: Intent?) {
            val sensorState = intent?.getStringExtra("sensor_state")
            Log.w("알비 서비스", "onReceive 호출: $sensorState")
            if (sensorState == "상태 위험!!") {
                userStatus = UserState.INACTIVE.status
                handler.post(timerRunnable)
            } else {
                userStatus = UserState.ACTIVE.status
                handler.removeCallbacks(timerRunnable)
            }

            if (previousState != userStatus) {
                if (userStatus == UserState.ACTIVE.status) {
                    updateNotification("센서를 사용중입니다.")
                }
                previousState = userStatus
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        Log.w("ALBEService", "서비스 생성됨.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentFilter = IntentFilter("kr.ac.wku.albeapp.sensor.SENSOR_STATE")
        ContextCompat.registerReceiver(
            this,
            sensorStateReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val notification = createNotification("센서 서비스가 실행 중입니다.")
        startForeground(1, notification)

        handler.post(timerRunnable)

        Log.w("ALBEService", "센서 동작중")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sensorStateReceiver)
        handler.removeCallbacks(timerRunnable)
        Log.w("알비 서비스", "센서 서비스 중지됨.")
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
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ALBE")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setSmallIcon(R.mipmap.albeicontransparent_round)

        return builder.build()
    }

    private fun updateNotification(sensorState: String, sound: Boolean = false) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(sensorState)
        if (sound) {
            notification.defaults = Notification.DEFAULT_SOUND // 소리 추가
        }
        notificationManager.notify(1, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}