package kr.ac.wku.albeapp.logins

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityUserSignupBinding
import kr.ac.wku.albeapp.photos.AddPhotoActivity

// 사용자 데이터를 담을 데이터 클래스 정의
data class UserData(
    var userName: String? = null,
    var userID: String? = null,
    var userPW: String? = null,
    // 필요한 정보가 더 있다면 추가하세요.
    var userState: Int? = null // 유저 상태 정상 : 1 , 비활성 : 0 이외 : 2
)

// 회원 가입 페이지 레이아웃의 액티비티
class UserSignUp : AppCompatActivity() {
    lateinit var addPhotoBtn: Button //이미지 업로드 버튼


    lateinit var binding: ActivityUserSignupBinding

    // 실시간 데이터베이스 관련
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_signup)
        addPhotoBtn = findViewById(R.id.imageUpload)

        // 실시간 데이터베이스 관련2 , users 라는 노드를 생성해서 회원정보 저장
        database = FirebaseDatabase.getInstance().getReference("users")


        // 이미지 업로드 하기 버튼을 눌렀을때 동작할 이벤트
        // 이미지를 업로드 하는 별도의 레이아웃으로 이동함.
        addPhotoBtn.setOnClickListener {
            var intent = Intent(this, AddPhotoActivity::class.java)
            startActivity(intent)
        }

        binding.completeSignup.setOnClickListener {
            var userName = binding.newName.text.toString()
            var userID = binding.newPhoneID.text.toString()
            var userPW = binding.newPW.text.toString()
            // 이 밑에 하단에 성별 , 이용약관 등 데이터 추가하라. 일단은 3개만

            // 파이어베이스 실시간 데이터베이스에 저장
            var user = UserData(userName, userID, userPW, userState = 1) // 유저 상태 기본값 1
            database.child(userID).setValue(user)

            var myIntent = Intent(this, LoginPageActivity::class.java)
            startActivity(myIntent)
        }
        val cancelSignup: Button = findViewById(R.id.cancelSignup)

        cancelSignup.setOnClickListener {
            // 버튼을 누를 시 이전 페이지로 돌아갑니다.
            onBackPressed()
        }
    }
}