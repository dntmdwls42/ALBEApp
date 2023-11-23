package kr.ac.wku.albeapp.logins

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            val userID = binding.newPhoneID.text.toString()  // 사용자의 전화번호를 가져옵니다.
            if (userID.isEmpty()) {
                Toast.makeText(this, "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AddPhotoActivity::class.java)
                intent.putExtra("phoneNumber", userID)  // 전화번호를 Intent에 추가
                startActivity(intent)
            }
        }

        //약관동의 작동 로직
        val checkBox1: CheckBox = findViewById(R.id.access1)
        val checkBox2: CheckBox = findViewById(R.id.access2)
        val completeSignupButton: Button = findViewById(R.id.completeSignup)

//        기본적으로 회원가입 버튼은 비활성 상태로 둠
        completeSignupButton.isEnabled = false

//        약관동의 체크박스 1과 2 모두 체크해야 회원가입 버튼 활성화 로직
        val checkListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            completeSignupButton.isEnabled = checkBox1.isChecked && checkBox2.isChecked
        }
        checkBox1.setOnCheckedChangeListener(checkListener)
        checkBox2.setOnCheckedChangeListener(checkListener)


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
            Toast.makeText(this, "회원가입을 취소합니다..", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }

        val btnTerms = findViewById<Button>(R.id.btn_terms)
        btnTerms.setOnClickListener {
            showTermsDialog()
        }

        val btnTermspolicy = findViewById<Button>(R.id.btn_terms_policy)
        btnTermspolicy.setOnClickListener {
            showTermsDialogpolicy()
        }
    }

    private fun showTermsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("AliveBeacon 이용 약관").setMessage(resources.getString(R.string.user_agree))
        builder.setPositiveButton("이해했습니다", null)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showTermsDialogpolicy() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("AliveBeacon 개인정보 이용 동의").setMessage(resources.getString(R.string.user_agree_policy))
        builder.setPositiveButton("이해했습니다", null)
        val alertDialog = builder.create()
        alertDialog.show()
    }
}