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
import kr.ac.wku.albeapp.MainActivity
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityLoginPageBinding

// 로그인 페이지 액티비티
class LoginPageActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginPageBinding

    // 실시간 데이터베이스에서 인스턴스 가져옴
    val database = FirebaseDatabase.getInstance()

    // 특정 위치(userID)에서 데이터 참조
    val myRef = database.getReference("userID")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_page)

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
                val value = dataSnapshot.getValue(String::class.java)
                Log.d("로그인 테스트", "값은 바로: $value")
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
                    // 비밀번호를 가져와서 저장
                    val savedPassword = dataSnapshot.child("userPW").getValue(String::class.java)
                    // 사용자 이름을 가져와서 저장
                    val userName = dataSnapshot.child("userName").getValue(String::class.java)
                    if (savedPassword == inputPassword) {
                        Toast.makeText(this@LoginPageActivity, "${userName}님 환영합니다.", Toast.LENGTH_SHORT).show()
                        // 로그인 성공
                        val intent = Intent(this@LoginPageActivity, MainActivity::class.java)
                        intent.putExtra("phoneNumber", inputPhoneNumber) // 전화번호를 Intent에 추가
                        startActivity(intent)
                    } else {
                        // 로그인 실패
                        Toast.makeText(
                            this@LoginPageActivity,
                            "전화번호나 비밀번호를 확인해보세요",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // 로그인 버튼 누른 후 , EditText 내용 비우는 코드
                    binding.loginpagePhonenumber.text.clear()
                    binding.loginpagePassword.text.clear()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("로그인 버튼후", "값을 읽는데 실패했습니다.", error.toException())
                }
            })
        }
        
        // 아이디 찾기 버튼을 눌렀을때 아이디찾기 화면으로 이동하는 이벤트
        binding.loginpageSearchIdButton.setOnClickListener {
            // 아이디찾기 화면으로 이동하는 이벤트
            var myIntent = Intent(this, FindMyId::class.java)

            // 아이디찾기 화면 레이아웃으로 이동
            startActivity(myIntent)
        }
    }
}