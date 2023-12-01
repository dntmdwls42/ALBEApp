package kr.ac.wku.albeapp.logins

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kr.ac.wku.albeapp.HomeMenu.HomeMenu
import kr.ac.wku.albeapp.MainActivity
import android.Manifest
import android.os.Build
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityLoginPageBinding
import kr.ac.wku.albeapp.permission.NotificationManagerHelper


// 로그인 페이지 액티비티
class LoginPageActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginPageBinding

    // 실시간 데이터베이스에서 인스턴스 가져옴
    val database = FirebaseDatabase.getInstance()

    // 특정 위치(userID)에서 데이터 참조
    val myRef = database.getReference("users").child("userID")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_page)


        TedPermission.create()
            .setPermissionListener(object:PermissionListener{
                override fun onPermissionGranted() {
                    Toast.makeText(this@LoginPageActivity, "알림 권한 허용됨.", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@LoginPageActivity, "알림 권한 거부됨.", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("권한을 주시지 않으면 알림을 받을 수 없습니다.")
            .setPermissions(Manifest.permission.POST_NOTIFICATIONS) // 알림 권한 = post~~~
            .check()

        binding.loginpageJoinButton.setOnClickListener {
            // 회원 가입 화면으로 이동하는 이벤트
            var myIntent = Intent(this, UserSignUp::class.java)

            // 회원가입 화면 레이아웃으로 이동
            startActivity(myIntent)
        }

        val customersupportbutton = findViewById<Button>(R.id.loginpage_customer_support_button)

        customersupportbutton.setOnClickListener {
            val email = "dntmdwls42@gmail.com"
            val subject = "Albe 앱에 문제가 발생했습니다."

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:dntmdwls42@gmail.com")

            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
//            정상작동 로그출력
            Log.d("buttonclicked", "CS버튼 눌림")
        }

        // userID에서 가져온 정보와 관련된 이벤트리스너
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(UserData::class.java)
                Log.w("로그인 테스트", "값은 바로: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("로그인 테스트", "값을 읽는데 실패했습니다.", error.toException())
            }
        })

        // 로그인 버튼을 눌렀을때 전화번호와 비밀번호를 검증함
        binding.loginpageProceedButton.setOnClickListener {
            val inputPhoneNumber = binding.loginpagePhonenumber.text.toString()
            val inputPassword = binding.loginpagePassword.text.toString()

            val userRef = database.getReference("users").child(inputPhoneNumber)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userData = dataSnapshot.getValue(UserData::class.java)

                    if (userData != null) {
                        val savedPassword = userData.userPW
                        val userName = userData.userName

                        if (savedPassword == inputPassword) {

                            Toast.makeText(
                                this@LoginPageActivity,
                                "${userName}님 환영합니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // SharedPreferences(세션)에 전화번호와 사용자 이름 저장함
                            val sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("phoneNumber", inputPhoneNumber)
                            editor.putString("userName", userName)
                            editor.apply()

                            // 이제 홈메뉴(진짜 메인)에 전화번호 정보를 intent로 넘기게 함
                            val intent = Intent(this@LoginPageActivity, HomeMenu::class.java)
                            intent.putExtra("phoneNumber", inputPhoneNumber)  // 전화번호를 Intent에 추가
                            startActivity(intent)

                        } else {
                            Toast.makeText(
                                this@LoginPageActivity,
                                "전화번호나 비밀번호를 확인해보세요",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginPageActivity,
                            "유효하지 않은 전화번호입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.loginpagePhonenumber.text.clear()
                    binding.loginpagePassword.text.clear()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("로그인 버튼후", "값을 읽는데 실패했습니다.", error.toException())
                }
            })
        }


        // 비밀번호 찾기 버튼을 눌렀을때 아이디찾기 화면으로 이동하는 이벤트
        binding.loginpageSearchIdButton.setOnClickListener {
            // 비밀번호 찾기 화면으로 이동하는 이벤트
            var myIntent = Intent(this, FindMyId::class.java)

            // 비밀번호 찾기 화면 레이아웃으로 이동
            startActivity(myIntent)
        }

        // 비밀번호 찾기 버튼을 "길게" 눌렀을때 개발자 모드(MainActivity) 진입
        binding.loginpageSearchIdButton.setOnLongClickListener {
            Toast.makeText(this, "디버깅 메뉴로 진입합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            true
        }
    }



}