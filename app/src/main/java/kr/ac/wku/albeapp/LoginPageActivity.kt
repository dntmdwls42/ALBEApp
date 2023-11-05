package kr.ac.wku.albeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }
}