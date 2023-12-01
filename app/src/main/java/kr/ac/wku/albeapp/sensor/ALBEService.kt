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
import com.google.firebase.database.FirebaseDatabase
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.logins.LoginSession

// Handler 선언
private val handler = Handler(Looper.getMainLooper())



// 센서 액티비티 백그라운드 동작을 담당하는곳
class ALBEService : Service() {
    private lateinit var sensorManager: SensorManager
    private val CHANNEL_ID = "ForegroundServiceChannel"


    // Runnable 선언
    private val runnable = object : Runnable {
        override fun run() {
            // 1분마다 실행할 작업을 작성합니다.
            Log.w("알비 서비스", "runnable 시작")
//            // 현재 로그인한 사용자의 전화번호를 가져옵니다.
//            val loginSession = LoginSession(this@ALBEService)
//            val phoneNumber = loginSession.phoneNumber
//
//            // 전화번호가 null이 아닌 경우에만 데이터베이스에 값을 저장합니다.
//            if (phoneNumber != null) {
//                // SharedPreferences에서 setState 값을 가져옵니다.
//                val sharedPref = getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
//                val setState = sharedPref.getInt("setState", 0)
//
//                val database = FirebaseDatabase.getInstance()
//                val myRef = database.getReference("users").child(phoneNumber)
//
//                // setState 값을 데이터베이스에 저장합니다.
//                Log.w("알비 서비스", "전화번호: $phoneNumber, setState: $setState")
//                myRef.child("userState").setValue(setState)
//                Log.w("알비 서비스", "userState 값 DB에 저장 완료")
//            }

            // 다음 실행을 위해 자신을 다시 호출합니다.
            Log.w("알비 서비스", "runnable 종료")
            handler.postDelayed(this, 60 * 1000L) // 1분마다 실행
        }
    }

    private val sensorStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sensorState = intent?.getStringExtra("sensor_state")
            Log.w("알비 서비스", "onReceive 호출: $sensorState")
            if (sensorState == "상태 위험!!") {
                // 상태가 "상태 위험!!"이고, 타이머가 10초를 넘었을 때
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
        registerReceiver(sensorStateReceiver, intentFilter , Context.RECEIVER_NOT_EXPORTED)
        val notification = createNotification("센서 서비스가 실행 중입니다.")
        startForeground(1, notification)

        handler.post(runnable)

        Log.w("ALBEService", "센서 동작중")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sensorStateReceiver)
        handler.removeCallbacks(runnable)
        Log.w("ALBEService", "센서 멈춤")
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
            .setSmallIcon(R.drawable.albe)

        if (contentText.contains("상태 위험!!") && contentText.contains("초 경과")) {
            builder.setDefaults(Notification.DEFAULT_SOUND) // 소리 추가
        }

        return builder.build()
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