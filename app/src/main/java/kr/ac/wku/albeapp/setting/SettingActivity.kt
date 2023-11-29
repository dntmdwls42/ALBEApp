package kr.ac.wku.albeapp.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivitySettingBinding
import kr.ac.wku.albeapp.logins.LoginPageActivity
import kr.ac.wku.albeapp.logins.UserData

// 설정 화면 액티비티
class SettingActivity : AppCompatActivity() {
    // 실시간 데이터베이스에서 인스턴스 가져옴
    val database = FirebaseDatabase.getInstance()

    // 데이터바인딩 설정
    private lateinit var binding: ActivitySettingBinding

    // 로그인 페이지로 부터 전화번호 정보 받아옴.
    lateinit var phoneNumber: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)

        // MainActivity에서 넘어왔는지 확인
        val isFromMainActivity = intent.getBooleanExtra("isFromMainActivity", false)

        // MainActivity(개발자 모드)에서 넘어온 경우 회원 탈퇴 버튼을 비활성화
        if (isFromMainActivity) {
            binding.userdelete.isEnabled = false
        }


        // 전화번호를 가져옴
        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        // 특정 사용자(로그인한 사용자를 말함)을 참조
        val myRef = database.getReference("users").child(phoneNumber)

        // 로그인 세션 확인
        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.contains("phoneNumber") // phoneNumber 키가 존재하는지 확인

        // 로그아웃 버튼의 가시성 설정
        binding.userdelete.visibility = if (isLoggedIn) View.VISIBLE else View.GONE

        // 로그아웃 버튼을 눌렀을때 로그아웃을 하는 내용
        binding.logout.setOnClickListener {
            // SharedPreferences에서 사용자 세션 정보를 삭제합니다.
            val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear() // 세션 정보 삭제
            editor.apply()

            // '로그아웃합니다' 라는 토스트 메시지를 표시합니다.
            Toast.makeText(this, "로그아웃합니다", Toast.LENGTH_SHORT).show()

            // 로그인 액티비티로 이동합니다.
            val intent = Intent(this, LoginPageActivity::class.java)
            startActivity(intent)
            finish() // SettingActivity를 종료합니다.
        }


        //회원 탈퇴 버튼 숨김
        binding.userdelete.visibility = View.GONE //회원 탈퇴 버튼은 숨겨져 있음(공간차지 x)

        binding.moresetting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && isLoggedIn) { //스위치 버튼이 눌리면 and 로그인 세션이 있는 경우
                binding.userdelete.visibility = View.VISIBLE // 탈퇴버튼 표시
            } else { //스위치 버튼이 안눌리면
                binding.userdelete.visibility = View.GONE // 탈퇴버튼 숨김상태
            }
        }

        // userID에서 가져온 정보와 관련된 이벤트리스너
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(UserData::class.java)
                Log.d("로그인 테스트", "값은 바로: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("로그인 테스트", "값을 읽는데 실패했습니다.", error.toException())
            }
        })

        //백그라운드버튼 -> 앱 종료
        binding.backgroundoff.setOnClickListener {
            Toast.makeText(this, "앱을 종료합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 센서별 비활성화 라디오 버튼 이벤트
        binding.sensoroff.setOnCheckedChangeListener { _, isChecked ->
            // SharedPreferences에 센서 사용 설정 값을 저장합니다.
            val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("sensor_off", isChecked)
            editor.apply()

            // userState를 2로 설정합니다.
            myRef.child("userState").setValue(if (isChecked) 2 else 1)
        }

        // seekbar = 설정창에서 센서 시간 1시간 단위로 조정하는 내용
        binding.sensorsetting.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                // seekbar 조정하는 강도에 따라서 진행도가 TextView로 보여짐
                binding.sensorvalue.text = progress.toString() // 값

                // Toast 메시지로 SeekBar의 값을 표시
                Toast.makeText(this@SettingActivity, "현재 센서 값: $progress", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        // 회원 탈퇴를 눌렀을때 실시간 데이터베이스에서 삭제 하는 내용
        binding.userdelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    val inputPhoneNumber = phoneNumber.toString()

                    val userRef =
                        database.getReference("users").child(inputPhoneNumber)  // 특정 사용자 참조

                    userRef.removeValue().addOnSuccessListener {
                        Toast.makeText(
                            this@SettingActivity,
                            "회원 탈퇴가 완료되었습니다, 로그인 페이지로 돌아갑니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 로그인 액티비티로 이동하는 인텐트 생성
                        val intent = Intent(this, LoginPageActivity::class.java)
                        // 액티비티 시작
                        startActivity(intent)
                        // SettingActivity 종료
                        finish()

                    }.addOnFailureListener {
                        Toast.makeText(
                            this@SettingActivity,
                            "다시 시도해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("아니오", null)
                .show()


        }
    }
}