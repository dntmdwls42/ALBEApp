package kr.ac.wku.albeapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.ac.wku.albeapp.databinding.ActivityLoginPageBinding

class LoginPageActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this , R.layout.activity_login_page)

        binding.loginpageJoinButton.setOnClickListener {
            // 회원 가입 화면으로 이동하는 이벤트
            var myIntent = Intent(this, UserSignUp::class.java)

            // 회원가입 화면 레이아웃으로 이동
            startActivity(myIntent)
        }

        val customersupportbutton = findViewById<Button>(R.id.loginpage_customer_support_button)

        customersupportbutton.setOnClickListener {
            // 정상 작동 로그 출력
            Log.d("buttonclicked", "CS버튼 눌림")

            // 토스트 출력
            Toast.makeText(this, "고객지원 이메일로 연결합니다.", Toast.LENGTH_SHORT).show()
            val handler = Handler()
            handler.postDelayed({
                Toast.makeText(this, "이메일 앱을 통해 문의사항을 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }, 3000) // 3초(3000 밀리초) 딜레이

            val email = "dntmdwls42@gmail.com"
            val subject = "Albe 앱에 문제가 발생했습니다."

            val intent = Intent(Intent.ACTION_SEND)
            intent.data = Uri.parse("mailto:$email")

            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)

            // 사용자가 내용을 작성할 수 있도록 Intent.EXTRA_TEXT를 추가
            val initialText = "앱에 문제가 발생했습니다. 다음과 같은 문제를 겪고 있습니다:\n\n"
            intent.putExtra(Intent.EXTRA_TEXT, initialText)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "이메일 앱 선택"))
            } else {
                Toast.makeText(this, "이메일 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}