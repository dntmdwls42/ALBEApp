package kr.ac.wku.albeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

// 로그인 후 메인화면 액티비티
class userViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_signup)
    }
}