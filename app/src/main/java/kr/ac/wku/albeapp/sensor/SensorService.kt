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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kr.ac.wku.albeapp.logins.LoginSession
import kr.ac.wku.albeapp.logins.UserState


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

    var setState: Int = 0

    // 실시간 데이터베이스에서 인스턴스 가져옴
    val database = FirebaseDatabase.getInstance()
    lateinit var myRef: DatabaseReference
    private var userStatus = UserState.ACTIVE.status

    // SensorManager와 Sensor 변수 추가
    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    // 센서 값 저장을 위한 변수
    private var getSensorValue = FloatArray(3)
    private var getGravityValue = FloatArray(3)

    // Handler 선언 = 타이머 관련
    private val handler = Handler(Looper.getMainLooper())

    companion object{
        var interval: Long = 10 * 1000 // 10초
    }

    // Runnable 선언
    private val runnable = object : Runnable {
        override fun run() {
            Log.w("센서 서비스", "runnable 시작")
            // 10초마다 실행할 작업을 작성합니다.
            if (isTimer) { // 센서 미동작 시
                sensorState = "상태 위험!!"    //알람발생
                Log.w("센서 서비스", "센서 상태: $sensorState")
                sendSensorState("상태 위험!!")
                setState = 0
            } else { // 센서 동작 시
                sensorState = "센서 동작"
                Log.w("센서 서비스", "센서 상태: $sensorState")
                sendSensorState("센서 동작")
                setState = 1
            }

            // 다음 실행을 위해 자신을 다시 호출합니다.
            Log.w("센서 서비스", "runnable 종료")
            handler.postDelayed(this, interval) // 10초마다 실행
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
            userStatus = UserState.ACTIVE.status
        } else { // 이게 비활성화 했을때
            sensorManager.unregisterListener(this)
            userStatus = UserState.TEMP_INACTIVE.status
        }

        // Runnable 시작
        handler.post(runnable)

        return START_STICKY
    }

    // ⑤ 리스너 해제
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        userStatus = UserState.INACTIVE.status
        // Runnable 중지
        handler.removeCallbacks(runnable)
        Log.w("센서 서비스", "센서 서비스 중지됨.")
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {    // 센서 정밀도 변경시
        //이 메서드는 onSensorChanged를 사용하기 위해 필요한 함수이다 그리고 메인 클래스에서 SensorEventListener를 상속받아야 한다 (interface 상속)
    }

    //추가할 메서드 : 센서 동작 시간 설정 min : 10s, max : 24h
    fun setSensorAlarmTimer() {

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
                        gravityX = event.values[0]
                        gravityY = event.values[1]
                        gravityZ = event.values[2]

                        getGravityValue[0] = gravityX / SensorManager.GRAVITY_EARTH
                        getGravityValue[1] = gravityY / SensorManager.GRAVITY_EARTH
                        getGravityValue[2] = gravityZ / SensorManager.GRAVITY_EARTH
                    }
                }

                val fixGyroscopeVar: Float = 1.0f //회전값 오차 범위
                val checkSensor: Boolean =
                    (getSensorValue[0] >= fixGyroscopeVar || getSensorValue[0] <= -fixGyroscopeVar) //getSensorValue값이 fixGyroscopeVar보다 넘어갈 때
                            || (getSensorValue[1] >= fixGyroscopeVar || getSensorValue[1] <= -fixGyroscopeVar)
                            || (getSensorValue[2] >= fixGyroscopeVar || getSensorValue[2] <= -fixGyroscopeVar) //자이로센서값이 동작할 때
                val acceleratorValue: Double = Math.sqrt( ((getGravityValue[0]*getGravityValue[0])
                        + (getGravityValue[1]*getGravityValue[1])
                        + (getGravityValue[2]*getGravityValue[2])).toDouble() ) //가속도 값
                val checkGravity: Boolean = acceleratorValue != 1.000340463377946 //가속도 센서 감지 Double실수가 가속도 센서 멈출때 (1.000340463377946)값이 멈추는 값

                    /*

                val fixSensorVar : Float = 1.0f //오차 범위
                checkGyroscope = (getSensorValue[0] >= fixSensorVar || getSensorValue[0] <= -fixSensorVar)
                        || (getSensorValue[1] >= fixSensorVar || getSensorValue[1] <= -fixSensorVar)
                        || (getSensorValue[2] >= fixSensorVar || getSensorValue[2] <= -fixSensorVar) //자이로센서값이 동작할 때
                val fixGravityVar : Float = 0.01f   //이동값 오차 범위
                checkGravity = (getGravityValue[0] >= fixGravityValue[0] + fixGravityVar || getGravityValue[0] <= fixGravityValue[0] - fixGravityVar)
                        || (getGravityValue[1] >= fixGravityValue[1] + fixGravityVar || getGravityValue[1] <= fixGravityValue[1] - fixGravityVar)
                        || (getGravityValue[2] >= fixGravityValue[2] + fixGravityVar || getGravityValue[2] <= fixGravityValue[2] - fixGravityVar)

                 */
                if (checkSensor || checkGravity) { // 센서 동작
                    isTimer = false
                    setState = 1
                } else { // 센서 미동작
                    isTimer = true
                    setState = 0
                }
                Log.d("SensorService","${acceleratorValue}")
                Log.d("SensorService","${setState}")
            }
        }
    }

    //
    // 센서 상태가 변경될때 ALBE 서비스로 알림
    private fun sendSensorState(state: String) {
        Log.w("센서 서비스", "sendSensorState 호출: $state")
        val intent = Intent("kr.ac.wku.albeapp.sensor.SENSOR_STATE")
        intent.putExtra("sensor_state", state) // 센서 상태 알림창으로 전송
        sendBroadcast(intent)

        // 현재 로그인한 사용자의 전화번호를 가져옵니다.
        val loginSession = LoginSession(this)
        val phoneNumber = loginSession.phoneNumber

        // 전화번호가 null이 아닌 경우에만 데이터베이스에 값을 저장합니다.
        if (phoneNumber != null) {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users").child(phoneNumber)

            // 센서 상태에 따라 userState 값을 설정합니다.
            val userState = if (state == "상태 위험!!") 0 else 1

            // userState 값을 데이터베이스에 저장합니다.
            myRef.child("userState").setValue(userState)
            Log.w("SensorService", "userState 값 DB에 저장: $userState")
        }
    }

}