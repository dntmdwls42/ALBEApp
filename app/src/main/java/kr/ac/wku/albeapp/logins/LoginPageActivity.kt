package kr.ac.wku.albeapp.logins

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kr.ac.wku.albeapp.HomeMenu.HomeMenu
import kr.ac.wku.albeapp.MainActivity
import android.Manifest
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityLoginPageBinding

// 2024년 1월 기준 새 로그인 방식으로 변경중
// 로그인 페이지 액티비티 , 주 기능은 유저 데이터 유효성 검사
class LoginPageActivity : AppCompatActivity() {
    // 데이터 바인딩
    lateinit var binding: ActivityLoginPageBinding

    // 파이어베이스 인증 관련
    lateinit var mAuth: FirebaseAuth

    // 실시간 데이터베이스에서 인스턴스 가져옴
    val database = FirebaseDatabase.getInstance()

    // 특정 위치(userID)에서 데이터 참조
    val myRef = database.getReference("users").child("userID")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_page)
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인증 기능 초기화
        mAuth = FirebaseAuth.getInstance()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "FCM TOKEN Failed...", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM TOKEN : ${token}")
        }

        // 알림 권한 받는것
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
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

        // 생체 신호 센서 권한 받는것
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    Toast.makeText(this@LoginPageActivity, "생체 신호 센서 권한 허용됨.", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@LoginPageActivity, "생체 신호 센서 권한 거부됨.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .setDeniedMessage("권한을 주시지 않으면 생체 신호 센서를 사용할 수 없습니다.")
            .setPermissions(Manifest.permission.BODY_SENSORS) // 생체 신호 센서 권한
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
            val email = binding.loginpageEmail.text.toString()
            val password = binding.loginpagePassword.text.toString()

            val intent = Intent(this, AuthActivity::class.java)
            intent.putExtra("email", email) // 이메일 전달 authAct로
            intent.putExtra("password", password) // 비번 전달 authAct로
            startActivity(intent)
            
            Toast.makeText(this@LoginPageActivity, "로그인을 시도합니다", Toast.LENGTH_SHORT).show()
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

        val customerSupportButton: Button = findViewById(R.id.loginpage_customer_support_button)
        customerSupportButton.setOnClickListener {
            Toast.makeText(this, "이메일 앱으로 이동합니다..", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this, "문의하시려는 내용을 이메일로 작성 해 주세요.", Toast.LENGTH_LONG).show()
            }, 4000)//3sec

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:dntmdwls42@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Albe앱에 문제가 발생했습니다.")
                putExtra(Intent.EXTRA_TEXT, "Albe앱 사용 중 도움이 필요합니다.")
            }

            startActivity(Intent.createChooser(emailIntent, "이메일 클라이언트 선택:"))
        }

        // onCreate 공간

    }

    override fun onStart() {
        super.onStart()
        // 유저 체크 한번함
        if (!ALBEAuth.checkAuth()) {
            Toast.makeText(this@LoginPageActivity, "성공", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@LoginPageActivity, "실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, AuthActivity::class.java))
        return super.onOptionsItemSelected(item)
    }

}