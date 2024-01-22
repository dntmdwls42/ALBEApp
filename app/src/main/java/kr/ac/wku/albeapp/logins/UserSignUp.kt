package kr.ac.wku.albeapp.logins

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityUserSignupBinding
import kr.ac.wku.albeapp.photos.AddPhotoActivity

// 2024년 1월 기준 구 버전의 로그인 방식임
// 1/22 기준 리뉴얼 진행중

// 회원 가입 페이지 레이아웃의 액티비티
class UserSignUp : AppCompatActivity() {
    lateinit var addPhotoBtn: Button //이미지 업로드 버튼
    lateinit var binding: ActivityUserSignupBinding

    // 실시간 데이터베이스 관련
    private lateinit var database: DatabaseReference

    // Firebase Cloud Messaging 토큰
    private lateinit var fcmToken: String

    // Firebase Authentication 관련
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_signup)

        // Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()

        addPhotoBtn = findViewById(R.id.imageUpload)

        // 실시간 데이터베이스 관련2 , users 라는 노드를 생성해서 회원정보 저장
        database = FirebaseDatabase.getInstance().getReference("users")

        // Firebase Cloud Messaging 토큰을 얻습니다
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("UserSignUp", "FCM 등록 토큰 얻기 실패", task.exception)
                return@addOnCompleteListener
            }
            fcmToken = task.result.toString()
            Log.d("UserSignUp", "FCM 토큰: $fcmToken")
        }

        // 이미지 업로드 하기 버튼을 눌렀을때 동작할 이벤트
        // 이미지를 업로드 하는 별도의 레이아웃으로 이동함.
        addPhotoBtn.setOnClickListener {
            val userID = binding.newEmail.text.toString()  // 사용자의 이메일을 가져옵니다.
            if (userID.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AddPhotoActivity::class.java)
                intent.putExtra("phoneNumber", userID)  // 이메일을 Intent에 추가
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


//        binding.completeSignup.setOnClickListener {
//            var userName = binding.newName.text.toString()
//            var userID = binding.newPhoneID.text.toString()
//            var userPW = binding.newPW.text.toString()
//            // 이 밑에 하단에 성별 , 이용약관 등 데이터 추가하라. 일단은 3개만
//
//            // Firebase Authentication을 사용하여 회원가입
//            auth.createUserWithEmailAndPassword(userID,userPW)
//                .addOnCompleteListener(this){task ->
//                    if(task.isSuccessful){
//                        // 회원 가입 성공
//                        Log.d("회원 가입 액티비티","createUserWithEmail:success")
//                        val uid = auth.currentUser?.uid // 사용자의 uid 얻음
//
//                        // 파이어베이스 실시간 데이터베이스 저장
//                        var user = auth.currentUser
//
//                        // 이메일 인증 메일 보내기
//                        user?.sendEmailVerification()
//                            ?.addOnCompleteListener { task ->
//                                if (task.isSuccessful) {
//                                    Log.d("회원 가입 액티비티","Email sent.")
//                                }
//                            }
//
//                        database.child(uid!!).setValue(user) // uid 밑에 데이터 저장되게
//
//                        // FCM 토큰을 Firestore에 저장
//                        saveTokenToFirestore(uid, fcmToken)
//
//                        // 회원가입 성공후 로그인 페이지로 다시 이동하게 함
//                        var myIntent = Intent(this, LoginPageActivity::class.java)
//                        startActivity(myIntent)
//                    }else{
//                        // 회원가입 실패
//                        Log.w("회원 가입 액티비티", "createUserWithEmail:failure", task.exception)
//                        Toast.makeText(baseContext, "인증 기능 실패", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//
//            // FCM 토큰을 Firestore에 저장
//            saveTokenToFirestore(userID, fcmToken)
//
//            var myIntent = Intent(this, LoginPageActivity::class.java)
//            startActivity(myIntent)
//        }

        // 2024/01/22 수정본
        // 2. 회원가입 후 사용자 정보 저장
        // 3. 이메일 인증이 성공했을 때 사용자 정보를 RealtimeDatabase에 저장
        binding.completeSignup.setOnClickListener {
            // 이메일 비밀번호 회원가입
            val email = binding.newEmail.text.toString() // 이메일
            val password = binding.newPW.text.toString() // 비밀번호
            ALBEAuth.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.newEmail.text.clear() // 이메일
                    binding.newPW.text.clear() // 비밀번호

                    if (task.isSuccessful) {
                        val user = ALBEAuth.auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { emailVerificationTask ->
                                if (emailVerificationTask.isSuccessful) {
                                    ALBEAuth.auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this) { loginTask ->
                                            if (loginTask.isSuccessful) {
                                                // 로그인이 성공하면 사용자 정보를 Firebase Realtime Database에 저장
                                                val userData = UserData(
                                                    userName = user.displayName,
                                                    userID = user.uid,
                                                    email = email
                                                )
                                                database.child("users").child(user.uid ?: "")
                                                    .setValue(userData)
                                            } else {
                                                // 로그인 실패시 오류 메시지 출력
                                                Log.e("Login", "로그인 실패", loginTask.exception)
                                                Toast.makeText(
                                                    baseContext, "로그인 실패",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        baseContext, "인증 메일 발송에 실패했습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // 회원가입 실패시 오류 메시지 출력
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                baseContext, "이미 사용 중인 이메일 주소입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.e("SignUp", "회원가입 실패", task.exception)
                            Toast.makeText(
                                baseContext, "회원가입 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
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

    private fun saveTokenToFirestore(userID: String, token: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(userID)

        val data = hashMapOf(
            "fcmToken" to token
            // 필요에 따라 추가 필드를 더합니다
        )

        userRef.set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserSignUp", "FCM 토큰이 Firestore에 성공적으로 저장되었습니다")
            }
            .addOnFailureListener { e ->
                Log.e("UserSignUp", "FCM 토큰을 Firestore에 저장하는 중 오류 발생", e)
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