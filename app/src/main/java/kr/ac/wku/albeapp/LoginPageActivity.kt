package kr.ac.wku.albeapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
    }
}