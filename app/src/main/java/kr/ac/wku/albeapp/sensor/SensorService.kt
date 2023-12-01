package kr.ac.wku.albeapp.sensor

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kr.ac.wku.albeapp.logins.LoginSession


// 센서'액티비티' -> 센서'서비스'
class SensorService : Service(), SensorEventListener {

    /*센서동작할 속성들*/
    //가속도 센서
    //각 센서가 움직임을 감지받는 축
    private var sensorX: Float = 0.0f
    private var sensorY: Float = 0.0f
    private var sensorZ: Float = 0.0f

    private var sensorState: String = ""
    private var activityPermission: Boolean = false

    //이동동작을 위한 중력속성값
    private var gravityX: Float = 0.0f
    private var gravityY: Float = 0.0f
    private var gravityZ: Float = 0.0f

    private var isTimer: Boolean = false   // 타이머 실행 상태

    private var nowSecond: Int = 0
    private var nowMinute: Int = 0
    private var nowHour: Int = 0

    var setState: Int = 0

    // 실시간 데이터베이스에서 인스턴스 가져옴
    val database = FirebaseDatabase.getInstance()
    lateinit var myRef: DatabaseReference

    private val ACTIVE = 1 // 센서 감지 = 활성 상태
    private val INACTIVE = 0 // 센서 감지 없음 = 비활성 상태
    private val TEMP_INACTIVE = 2 // 설정에서 비활성화 = 일시적 비활성 상태

    private var userStatus = ACTIVE

    // SensorManager와 Sensor 변수 추가
    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    // 센서 값 저장을 위한 변수
    private var getSensorValue = FloatArray(3)
    private var fixGravityValue = FloatArray(3)
    private var getGravityValue = FloatArray(3)

    // Handler 선언 = 타이머 관련
    private val handler = Handler(Looper.getMainLooper())

    // Runnable 선언
    private val runnable = object : Runnable {
        override fun run() {
            // 10초마다 실행할 작업을 작성합니다.
            if (isTimer) { // 센서 미동작 시
                sensorState = "상태 위험!!"    //알람발생
                sendSensorState("상태 위험!!")
                setState = 0
            } else { // 센서 동작 시
                sensorState = "센서 동작"
                sendSensorState("센서 동작")
                setState = 1
            }

            // 다음 실행을 위해 자신을 다시 호출합니다.
            handler.postDelayed(this, 10 * 1000L) // 10초마다 실행
        }
    }

    // 이상없음 1
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // SensorManager와 Sensor 초기화
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 유저 ID 넘어옴
        val loginSession = LoginSession(this)
        val receiveID = loginSession.phoneNumber // 로그인 세션에서 phoneNumber를 가져옴
        Log.w("센서 액티비티 로그인한 사람 확인", "${receiveID}")

        // Null 체크 추가
        if (receiveID != null) {
            myRef = database.getReference("users").child(receiveID)
        } else {
            Log.e("확인확인", "로그인 세션에서 ID를 가져오지 못했습니다.")
        }


        // SharedPreferences에서 센서 사용 설정 값을 불러옵니다.
        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val isSensorOff = sharedPreferences.getBoolean("sensor_off", false)


        // 퍼미션 확인해주는 데이터
        val sensorPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        )
        val sensorBackgroundPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS_BACKGROUND
        )
        if (sensorPermission == PackageManager.PERMISSION_GRANTED || //센서 퍼미션이 권한이 확인되면
            sensorBackgroundPermission == PackageManager.PERMISSION_GRANTED
        ) {
            activityPermission = true
        } else    // 안되있으면 설정 요청
        {
            activityPermission = false
            Toast.makeText(this, "센서 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //센서 등록 메서드
        Log.w("센서액티비티", "액티비티 실행중")

        // SharedPreferences에서 센서 사용 설정 값을 불러옵니다.
        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val isSensorOff = sharedPreferences.getBoolean("sensor_off", false)

        // if - else로 설정 화면에서 센서 비활성화 하면 센서 동작 안함
        if (!isSensorOff) {
            sensorManager.registerListener(
                this,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            sensorManager.registerListener(
                this,
                gravitySensor,
                SensorManager.SENSOR_DELAY_UI
            )
            userStatus = ACTIVE
        } else { // 이게 비활성화 했을때
            sensorManager.unregisterListener(this)
            userStatus = TEMP_INACTIVE
        }

        // Runnable 시작
        handler.post(runnable)

        return START_STICKY
    }

    // ⑤ 리스너 해제
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        userStatus = INACTIVE
        // Runnable 중지
        handler.removeCallbacks(runnable)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {    // 센서 정밀도 변경시
        //이 메서드는 onSensorChanged를 사용하기 위해 필요한 함수이다 그리고 메인 클래스에서 SensorEventListener를 상속받아야 한다 (interface 상속)
    }


    override fun onSensorChanged(event: SensorEvent?) {               // 센서 값 변경시
        //타이머 리셋 (동작시 상태 활성)
        if (activityPermission) {
            event?.let {
                when (event.sensor.type) {
                    Sensor.TYPE_GYROSCOPE -> {
                        //회전 센서 받아오는 값
                        sensorX = event.values[0]
                        sensorY = event.values[1]
                        sensorZ = event.values[2]

                        getSensorValue[0] = sensorX
                        getSensorValue[1] = sensorY
                        getSensorValue[2] = sensorZ
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        if (fixGravityValue[0] == 0f) fixGravityValue[0] = event.values[0]
                        if (fixGravityValue[1] == 0f) fixGravityValue[1] = event.values[1]
                        if (fixGravityValue[2] == 0f) fixGravityValue[2] = event.values[2]

                        gravityX = event.values[0]
                        gravityY = event.values[1]
                        gravityZ = event.values[2]

                        getGravityValue[0] = gravityX
                        getGravityValue[1] = gravityY
                        getGravityValue[2] = gravityZ
                    }
                }

                val fixSensorVar: Float = 1.0f //오차 범위
                val checkSensor: Boolean =
                    (getSensorValue[0] >= fixSensorVar || getSensorValue[0] <= -fixSensorVar)
                            || (getSensorValue[1] >= fixSensorVar || getSensorValue[1] <= -fixSensorVar)
                            || (getSensorValue[2] >= fixSensorVar || getSensorValue[2] <= -fixSensorVar) //자이로센서값이 동작할 때
                val fixGravityVar: Float = 0.01f
                val checkGravity: Boolean =
                    (getGravityValue[0] <= fixGravityValue[0] - fixGravityVar || getGravityValue[0] >= fixGravityValue[0] + fixGravityVar)
                            || (getGravityValue[1] <= fixGravityValue[1] - fixGravityVar || getGravityValue[1] >= fixGravityValue[1] + fixGravityVar)
                            || (getGravityValue[2] <= fixGravityValue[2] - fixGravityVar || getGravityValue[2] >= fixGravityValue[2] + fixGravityVar)

                if (checkSensor || checkGravity) { // 센서 동작
                    isTimer = false
                    setState = 1
                } else { // 센서 미동작
                    isTimer = true
                    setState = 0
                }
            }
        }
    }

    // 센서 상태가 변경될때 ALBE 서비스로 알림
    private fun sendSensorState(state: String) {
        val intent = Intent("kr.ac.wku.albeapp.sensor.SENSOR_STATE")
        intent.putExtra("sensor_state", state) // 센서 상태 알림창으로 전송
        intent.putExtra("timer_info", isTimer) // 타이머도
        sendBroadcast(intent)
    }

}