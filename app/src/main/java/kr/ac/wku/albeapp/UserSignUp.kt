package kr.ac.wku.albeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.ac.wku.albeapp.databinding.ActivityUserSignupBinding
import kr.ac.wku.albeapp.photos.AddPhotoActivity

// 회원 가입 페이지 레이아웃의 액티비티ㅁ
class UserSignUp : AppCompatActivity() {
    lateinit var addPhotoBtn: Button

    lateinit var binding: ActivityUserSignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_signup)

        addPhotoBtn = findViewById(R.id.imageUpload)

        // 이미지 업로드 하기 버튼을 눌렀을때 동작할 이벤트
        // 이미지를 업로드 하는 별도의 레이아웃으로 이동함.
        addPhotoBtn.setOnClickListener {
            var intent = Intent(this, AddPhotoActivity::class.java)
            startActivity(intent)
        }

        binding.completeSignup.setOnClickListener {
            // 회원 가입 완료 클릭시 다시 메인화면으로
            var myIntent = Intent(this, MainActivity::class.java)

            // 처음 메인 화면 레이아웃으로 이동
            startActivity(myIntent)
        }
    }
}