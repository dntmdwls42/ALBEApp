package kr.ac.wku.albeapp.setting

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kr.ac.wku.albeapp.logins.UserData
import kr.ac.wku.albeapp.sensor.AlbeService
import kr.ac.wku.albeapp.sensor.SensorService

/**
 * MVVM 패턴으로 구현된 환경 설정 관련 ViewModel
 */
class SettingViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val sharedPreferences =
        context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var phoneNumber: String
    private lateinit var myRef: DatabaseReference

    // LiveData 변수들
    val isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserDeleteVisible = MutableLiveData<Boolean>()
    val sensorOff = MutableLiveData<Boolean>(false)
    val seekBarProgress = MutableLiveData<Int>()
    val toastMessage = MutableLiveData<String>()

    // 추가된 부분: isMoreSettingChecked 변수
    val isMoreSettingChecked = MutableLiveData<Boolean>(false)

    // 이벤트 전달을 위한 LiveData
    private val _backButtonEvent = MutableLiveData<Unit>()
    val backButtonEvent: LiveData<Unit> get() = _backButtonEvent

    private val _showDeleteAccountDialogEvent = MutableLiveData<Unit>()
    val showDeleteAccountDialogEvent: LiveData<Unit> get() = _showDeleteAccountDialogEvent

    val sensorIntervals = arrayOf(
        10 * 1000L,                // 0: 10초
        4 * 60 * 60 * 1000L,       // 1: 4시간
        8 * 60 * 60 * 1000L,       // 2: 8시간
        12 * 60 * 60 * 1000L,      // 3: 12시간
        16 * 60 * 60 * 1000L,      // 4: 16시간
        20 * 60 * 60 * 1000L,      // 5: 20시간
        24 * 60 * 60 * 1000L       // 6: 24시간
    )

    // 뒤로 가기 버튼 클릭 이벤트
    fun onBackButtonClicked() {
        _backButtonEvent.value = Unit
    }

    // 회원 탈퇴 다이얼로그 표시 이벤트
    fun showDeleteAccountDialog() {
        _showDeleteAccountDialogEvent.value = Unit
    }

    fun initData(phoneNumber: String, isFromMainActivity: Boolean) {
        this.phoneNumber = phoneNumber
        myRef = database.getReference("users").child(phoneNumber)

        isUserLoggedIn.value = sharedPreferences.contains("phoneNumber")
        isUserDeleteVisible.value = !isFromMainActivity && isUserLoggedIn.value == true

        val sensorOffValue = sharedPreferences.getBoolean("sensor_off", false)
        sensorOff.value = sensorOffValue

        val savedProgress = sharedPreferences.getInt("seekBarProgress", 0)
        seekBarProgress.value = savedProgress

        // 사용자 데이터 변경 이벤트 리스너 추가
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(UserData::class.java)
                Log.d("로그인 테스트", "값은 바로: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("로그인 테스트", "값을 읽는데 실패했습니다.", error.toException())
            }
        })
    }

    fun logout() {
        auth.signOut()
        sharedPreferences.edit().clear().apply()
        isUserLoggedIn.value = false
        toastMessage.value = "로그아웃합니다"

        // 로그 출력 추가
        Log.d("SettingViewModel", "로그아웃 처리 완료")
    }

    fun toggleUserDeleteVisibility(isChecked: Boolean) {
        isMoreSettingChecked.value = isChecked
        isUserDeleteVisible.value = isChecked && isUserLoggedIn.value == true
    }

    fun deleteAccount() {
        myRef.removeValue().addOnSuccessListener {
            toastMessage.value = "회원 탈퇴가 완료되었습니다, 로그인 페이지로 돌아갑니다."
            stopServices()
            logout()
        }.addOnFailureListener {
            toastMessage.value = "다시 시도해주세요."
        }
    }

    fun stopServices() {
        val intentAlbe = Intent(context, AlbeService::class.java)
        val intentSensor = Intent(context, SensorService::class.java)
        context.stopService(intentAlbe)
        context.stopService(intentSensor)
        toastMessage.value = "백그라운드 센서를 종료합니다."
        Log.w("설정 서비스", "서비스 종료됨")
    }

    fun startServices() {
        val intentAlbe = Intent(context, AlbeService::class.java)
        val intentSensor = Intent(context, SensorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentAlbe)
            context.startForegroundService(intentSensor)
        } else {
            context.startService(intentAlbe)
            context.startService(intentSensor)
        }
        toastMessage.value = "백그라운드 센서를 시작합니다."
        Log.w("설정 서비스", "서비스 시작됨")
    }

    fun setSensorUsage(isOff: Boolean) {
        sharedPreferences.edit().putBoolean("sensor_off", isOff).apply()
        sensorOff.value = isOff
        Log.w("설정 서비스", "로그인 한 사람 : $myRef")

        if (isOff) {
            stopServices()
            toastMessage.value = "센서 사용을 종료합니다."
            Log.w("설정 서비스", "센서 서비스 종료")
        } else {
            startServices()
            myRef.child("userState").setValue(1)
            toastMessage.value = "센서를 다시 사용합니다."
            Log.w("설정 서비스", "센서 서비스 시작")
        }
    }

    fun updateSensorInterval(progress: Int) {
        if (progress in sensorIntervals.indices) {
            SensorService.interval = sensorIntervals[progress]
            val hours = when (progress) {
                0 -> "10초"
                else -> "${SensorService.interval / (60 * 60 * 1000L)}시간"
            }
            toastMessage.value = "설정 시간: $hours"

            // SeekBar 값을 SharedPreferences에 저장
            sharedPreferences.edit().putInt("seekBarProgress", progress).apply()
            seekBarProgress.value = progress

            // 로그 출력 추가
            Log.d("설정 서비스", "센서 감지 시간 설정: $hours")
        }
    }

    fun openNotificationSettings() {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        toastMessage.value = "앱 알림 설정 화면으로 이동합니다"
        Log.d("설정 서비스", "알림 설정 화면으로 이동")
    }
}
