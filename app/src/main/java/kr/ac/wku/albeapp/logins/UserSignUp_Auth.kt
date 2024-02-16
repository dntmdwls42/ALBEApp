package kr.ac.wku.albeapp.logins

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityLoginPageBinding
import kr.ac.wku.albeapp.databinding.ActivityUserSignup2Binding
import kr.ac.wku.albeapp.photos.AddPhotoActivity

// 여기는 회원정보 입력 관련 코드
class UserSignUp_Auth : AppCompatActivity() {

    // 데이터 바인딩
    lateinit var binding: ActivityUserSignup2Binding
    // activity_user_signup2.xml 레이아웃
    // signup에서 signup2로 진행

    lateinit var addPhotoBtn: Button //이미지 업로드 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_signup2)

        addPhotoBtn = binding.imageUpload // 이미지 업로드 버튼 데이터 바인딩

        // 이미지 업로드 하기 버튼을 눌렀을때 동작할 이벤트
        // 이미지를 업로드 하는 별도의 레이아웃으로 이동함.
        addPhotoBtn.setOnClickListener {
//            val userID = binding.newEmail.text.toString()  // 사용자의 이메일을 가져옵니다.
//            if (userID.isEmpty()) {
//                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
//            } else {
//                val intent = Intent(this, AddPhotoActivity::class.java)
//                intent.putExtra("phoneNumber", userID)  // 이메일을 Intent에 추가
//                startActivity(intent)
//            }
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
        builder.setTitle("AliveBeacon 개인정보 이용 동의")
            .setMessage(resources.getString(R.string.user_agree_policy))
        builder.setPositiveButton("이해했습니다", null)
        val alertDialog = builder.create()
        alertDialog.show()
    }
}