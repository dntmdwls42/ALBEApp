package kr.ac.wku.albeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import kr.ac.wku.albeapp.databinding.ActivityMainBinding

// 초기 로그인 화면 액티비티
class MainActivity : AppCompatActivity() {
    // "메인 페이지" 데이터 바인딩 세팅 1
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // "메인 페이지" 데이터 바인딩 세팅 2
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        
        binding.fromImagePage.setOnClickListener { 
            // 테스트용 이미지 업로드 레이아웃으로 이동하는 이벤트 , 테스트용

            // 화면 이동 :  intent
            // imageupload 레이아웃이 도착지로 설정함.
            
            var myIntent = Intent(this, ImageUpload::class.java)
            
            // 이미지 업로드 레이아웃으로 이동
            startActivity(myIntent)
        }
    }
}