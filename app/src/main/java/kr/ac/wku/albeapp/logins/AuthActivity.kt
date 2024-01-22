package kr.ac.wku.albeapp.logins

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityLoginPageBinding

// 인증을 진행하는 액티비티
class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ALBEAuth.checkAuth()) {
            Toast.makeText(this@AuthActivity, "인증 테스트1", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@AuthActivity, "인증 테스트2", Toast.LENGTH_SHORT).show()
        }

        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                ALBEAuth.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (
                            task.isSuccessful
                        ) {
                            ALBEAuth.email = account.email
                            Toast.makeText(this@AuthActivity, "인증 테스트3", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AuthActivity, "인증 테스트4", Toast.LENGTH_SHORT).show()
                        }


                    }

            } catch (e: ApiException) {
                Toast.makeText(this@AuthActivity, "인증 테스트5", Toast.LENGTH_SHORT).show()
            }
        }

//        binding..setOnClickListener {
//            // 로그아웃 내용
//            ALBEAuth.auth.signOut()
//            ALBEAuth.email = null
//            changeVisibility("logout")
//        }

//        binding.loginpageProceedButton.setOnClickListener {
//            Toast.makeText(this@AuthActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
//        }

//        binding.googleLoginBtn.setOnClickListener {
//            // 구글 로그인
//            val gso = GoogleSignInOptions
//                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString((R.string.default_web_client_id)))
//                .requestEmail()
//                .build()
//            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
//            requestLauncher.launch(signInIntent)
//        }


        binding.loginpageProceedButton.setOnClickListener {
            // 이메일 , 비밀번호 로그인
            val email = binding.loginpageEmail.text.toString() // 이메일
            val password = binding.loginpagePassword.text.toString() // 비밀번호
            Log.d("JOE", "email:$email,password:$password")

            ALBEAuth.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.loginpageEmail.text.clear() // 이메일
                    binding.loginpagePassword.text.clear() // 비밀번호
                    if (task.isSuccessful) {
                        if (ALBEAuth.checkAuth()) {
                            ALBEAuth.email = email
                            Toast.makeText(
                                this@AuthActivity,
                                "인증 액티비티가 일 잘하고있음",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                baseContext,
                                "전송된 메일로 이메일 인증이 되지 않았습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            baseContext, "로그인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}