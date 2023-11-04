package kr.ac.wku.albeapp

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import kr.ac.wku.albeapp.databinding.ActivityMainBinding
import kr.ac.wku.albeapp.setting.SettingActivity

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
            
            var myIntent = Intent(this, SettingActivity::class.java)
            
            // 이미지 업로드 레이아웃으로 이동
            startActivity(myIntent)
        }

        // 맨 처음에 앱에서 갤러리에 접근을 허용할것인지 물어보는
        // 권한 체크 메시지 - 한번 허용하면 앱 데이터를 삭제하지 않는 이상 다시 뜨지 않음.
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

    }
}