//package kr.ac.wku.albeapp.sensor
//
//import android.Manifest
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.os.Bundle
//import android.os.SystemClock
//import android.util.Log
//import android.widget.Chronometer
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase // 추가
//import kr.ac.wku.albeapp.R
//import kr.ac.wku.albeapp.logins.LoginSession
//
//class SensorActvitiy : AppCompatActivity(), SensorEventListener {
//
//
//
//    //퍼미션 (권한요청)
//    val MY_PERMISSION_SENSOR = 100
//
//    /*센서동작할 속성들*/
//    //가속도 센서
//    //각 센서가 움직임을 감지받는 축
//    lateinit var sensorX: TextView
//    lateinit var sensorY: TextView
//    lateinit var sensorZ: TextView
//
//    lateinit var sensorState: TextView
//    private var activityPermission: Boolean = false
//
//    //이동동작을 위한 중력속성값
//    lateinit var gravityX: TextView
//    lateinit var gravityY: TextView
//    lateinit var gravityZ: TextView
//
//    //추가할 속성
//    lateinit var stateTimer: Chronometer
//
//    // 알람을 울리기 위한 타이머  (우선 0으로 초기)
//    private var secondAlarm: Int = 0
//    private var minuteAlarm: Int = 0
//    private var hourAlarm: Int = 0
//    private var dayAlarm: Int = 0
//
//    private var isTimer: Boolean = false   // 타이머 실행 상태
//
//    private var elapsedMillis: Long = 0
//    private var nowSecond: Int = 0
//    private var nowMinute: Int = 0
//    private var nowHour: Int = 0
//    private var nowDay: Int = 0
//
//    var setState: Int = 0
//
//    // 실시간 데이터베이스에서 인스턴스 가져옴
//    val database = FirebaseDatabase.getInstance()
//    lateinit var myRef: DatabaseReference
//
//
//
//    private val ACTIVE = 1 // 센서 감지 = 활성 상태
//    private val INACTIVE = 0 // 센서 감지 없음 = 비활성 상태
//    private val TEMP_INACTIVE = 2 // 설정에서 비활성화 = 일시적 비활성 상태
//
//    private var userStatus = ACTIVE
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_sensor_actvitiy)
//
//        // 유저 ID 넘어옴
//        val loginSession = LoginSession(this)
//        val receiveID = loginSession.phoneNumber // 로그인 세션에서 phoneNumber를 가져옴
//        Log.w("센서 액티비티 로그인한 사람 확인","${receiveID}")
//
//        // Null 체크 추가
//        if(receiveID != null) {
//            myRef = database.getReference("users").child(receiveID)
//        } else {
//            Log.e("확인확인", "로그인 세션에서 ID를 가져오지 못했습니다.")
//        }
//
//
//        // SharedPreferences에서 센서 사용 설정 값을 불러옵니다.
//        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
//        val isSensorOff = sharedPreferences.getBoolean("sensor_off", false)
//
//        //가속도 센서
//        sensorX = findViewById(R.id.sensorX)
//        sensorY = findViewById(R.id.sensorY)
//        sensorZ = findViewById(R.id.sensorZ)
//
//        sensorState = findViewById(R.id.sensorState)
//
//        gravityX = findViewById(R.id.gravityX)
//        gravityY = findViewById(R.id.gravityY)
//        gravityZ = findViewById(R.id.gravityZ)
//
//        // 퍼미션 확인해주는 데이터
//        val sensorPermission = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.BODY_SENSORS
//        )
//        val sensorBackgroundPermission = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.BODY_SENSORS_BACKGROUND
//        )
//        if (sensorPermission == PackageManager.PERMISSION_GRANTED || //센서 퍼미션이 권한이 확인되면
//            sensorBackgroundPermission == PackageManager.PERMISSION_GRANTED
//        ) {
//            activityPermission = true
//        } else    // 안되있으면 설정 요청
//        {
//            activityPermission = false
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.BODY_SENSORS),
//                MY_PERMISSION_SENSOR
//            )
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.BODY_SENSORS_BACKGROUND),
//                MY_PERMISSION_SENSOR
//            )
//        }
//
//        //타이머 속성
//        stateTimer = findViewById(R.id.stateTimer)
//        stateTimer.start()
//
//        //알람 타이머 지정
//        setAlarmTimer(0, 0,0,10)
//    }
//
//    public fun setAlarmTimer(d: Int, h: Int, m: Int = 1, s: Int = 10) // 설정할 알람 타이머
//    {
//        dayAlarm = d
//        hourAlarm = h
//        minuteAlarm = m
//        secondAlarm = s
//    }
//
//    var getSensorValue = Array(3,{0.0f})
//    var getGravityValue = Array(3,{0.0f})
//    var fixGravityValue = Array(3,{0.0f})
//    private val gyroscopeSensor by lazy {           // 지연된 초기화는 딱 한 번 실행됨
//        getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    }
//    private val gravitySensor by lazy{
//        getSystemService(Context.SENSOR_SERVICE) as SensorManager
//    }
//
//    override fun onResume() {    //센서 등록 메서드
//        super.onResume()
//
//        Log.w("센서액티비티", "액티비티 실행중")
//
//        // SharedPreferences에서 센서 사용 설정 값을 불러옵니다.
//        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
//        val isSensorOff = sharedPreferences.getBoolean("sensor_off", false)
//
//        // if - else로 설정 화면에서 센서 비활성화 하면 센서 동작 안함
//        if (!isSensorOff) {
//            gyroscopeSensor.registerListener(
//                this,
//                gyroscopeSensor.getDefaultSensor(Sensor.TYPE_GYROSCOPE),    //가속도 혹은 자이로 중에 하나를 선택할텐데, 우선 설계하기 쉬운것부터 해보고
//                SensorManager.SENSOR_DELAY_NORMAL
//            )
//            gravitySensor.registerListener(
//                this,
//                gravitySensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_UI
//            )
//            userStatus = ACTIVE
//        } else { // 이게 비활성화 했을때
//            gyroscopeSensor.unregisterListener(this)
//            gravitySensor.unregisterListener(this)
//            userStatus = TEMP_INACTIVE
//        }
//
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {    // 센서 정밀도 변경시
//        //이 메서드는 onSensorChanged를 사용하기 위해 필요한 함수이다 그리고 메인 클래스에서 SensorEventListener를 상속받아야 한다 (interface 상속)
//    }
//
//
//    override fun onSensorChanged(event: SensorEvent?) {               // 센서 값 변경시
//        //타이머 리셋 (동작시 상태 활성)
//        if (activityPermission) {
//            event?.let {
//                when(event.sensor.type){
//                    Sensor.TYPE_GYROSCOPE -> {
//                        //회전 센서 받아오는 값
//                        getSensorValue[0] = event!!.values[0]
//                        getSensorValue[1] = event.values[1]
//                        getSensorValue[2] = event.values[2]
//                    }
//                    Sensor.TYPE_ACCELEROMETER -> {
//                        if(fixGravityValue[0] == 0f) fixGravityValue[0] = event.values[0]
//                        if(fixGravityValue[1] == 0f) fixGravityValue[1] = event.values[1]
//                        if(fixGravityValue[2] == 0f) fixGravityValue[2] = event.values[2]
//                        getGravityValue[0] = event.values[0]
//                        getGravityValue[1] = event.values[1]
//                        getGravityValue[2] = event.values[2]
//                    }
//                }
//                sensorX.text = getSensorValue[0].toString()
//                sensorY.text = getSensorValue[1].toString()
//                sensorZ.text = getSensorValue[2].toString()
//
//                gravityX.text = getGravityValue[0].toString()
//                gravityY.text = getGravityValue[1].toString()
//                gravityZ.text = getGravityValue[2].toString()
//
//                val fixSensorVar : Float = 1.0f //오차 범위
//                val checkSensor: Boolean = (getSensorValue[0] >= fixSensorVar || getSensorValue[0] <= -fixSensorVar)
//                        || (getSensorValue[1] >= fixSensorVar || getSensorValue[1] <= -fixSensorVar)
//                        || (getSensorValue[2] >= fixSensorVar || getSensorValue[2] <= -fixSensorVar) //자이로센서값이 동작할 때
//                val fixGravityVar : Float = 0.01f
//                val checkGravity: Boolean = (getGravityValue[0] <= fixGravityValue[0] - fixGravityVar || getGravityValue[0] >= fixGravityValue[0] + fixGravityVar)
//                        || (getGravityValue[1] <= fixGravityValue[1] - fixGravityVar || getGravityValue[1] >= fixGravityValue[1] + fixGravityVar)
//                        || (getGravityValue[2] <= fixGravityValue[2] - fixGravityVar || getGravityValue[2] >= fixGravityValue[2] + fixGravityVar)
//
//                if (checkSensor)
//                {
//                    sensorState.text = "센서 동작" //타이머 리셋
//                    sendSensorState("센서 동작")
//                    isTimer = false
//                    stateTimer.base = SystemClock.elapsedRealtime()
//                    stateTimer.stop()
//                    setState = 1
//                }
//                else
//                {
//                    sensorState.text = "센서 미동작"//타이머 실행
//                    sendSensorState("센서 미동작")
//                    isTimer = true
//                    stateTimer.start()
//                    setState = 0    //setState를 firebase의 userState로 전송
//                    //가속도 센서값 고정 (이유 : 가속도센서가 동작하지 않으면 0으로 초기되지 않아서)
//                    fixGravityValue[0] = getGravityValue[0]
//                    fixGravityValue[1] = getGravityValue[1]
//                    fixGravityValue[2] = getGravityValue[2]
//                }
//                // 코드 추가
//                myRef.child("userState").setValue(setState)
//                //Log.d("SensorActvitiy","State : ${myState}")
//
//                // [0] x축값, [1] y축값, [2] z축값
//            }
//
//            //실행한 타이머 갱신
//            elapsedMillis = SystemClock.elapsedRealtime() - stateTimer.getBase()
//            nowHour = (elapsedMillis / 3600000).toInt()
//            nowMinute = (elapsedMillis - nowHour * 3600000).toInt() / 60000
//            nowSecond = (elapsedMillis - nowHour * 3600000 - nowMinute * 60000).toInt() / 1000
//            nowDay = (nowHour * 24).toInt()
//
//            if (nowDay >= dayAlarm && nowHour >= hourAlarm && nowMinute >= minuteAlarm && nowSecond >= secondAlarm)
//                sensorState.text = "상태 위험!!"    //알람발생
//                sendSensorState("상태 위험!!")
//            //Log.d("MainActivity", " x:${nowHour}, y:${nowMinute}, z:${nowSecond} ")
//            //Log.d("MainActivity", " x:${event!! .values[0]}, y:${event.values[1]}, z:${event.values[2]} ")
//        }
//    }
//
//    // 센서 상태가 변경될때 ALBE 서비스로 알림
//    private fun sendSensorState(state: String) {
//        val timerInfo = "${nowHour}h ${nowMinute}m ${nowSecond}s"
//        val intent = Intent("kr.ac.wku.albeapp.sensor.SENSOR_STATE")
//        intent.putExtra("sensor_state", state) // 센서 상태 알림창으로 전송
//        intent.putExtra("timer_info", timerInfo) // 타이머도
//        sendBroadcast(intent)
//    }
//
//    // ⑤ 리스너 해제
//    override fun onPause() {
//        super.onPause()
//        gyroscopeSensor.unregisterListener(this)
//        gravitySensor.unregisterListener(this)
//        userStatus = INACTIVE
//    }
//
//    //퍼미션 동작
//    override fun onRequestPermissionsResult(    //퍼미션 권한이 해제되면 경고창 표시
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == MY_PERMISSION_SENSOR)
//            if (grantResults.size > 0) {
//                for (grant in grantResults) {
//                    if (grant != PackageManager.PERMISSION_GRANTED) {
//                        //System.exit(0)
//                        Toast.makeText(this, "퍼미션 권한 해제", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//    }
//}