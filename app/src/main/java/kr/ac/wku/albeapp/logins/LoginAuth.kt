package kr.ac.wku.albeapp.logins

import androidx.multidex.MultiDexApplication
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

// 2024년 1월 기준 새 로그인 방식
// 새 로그인 인증 방식으로 변경된 내용
class LoginAuth : MultiDexApplication() {
    companion object {
        lateinit var auth: FirebaseAuth
        var albe_email: String? = null // 이메일 저장 공간

        lateinit var db: FirebaseFirestore
        lateinit var storage: FirebaseStorage

        fun checkAuth(): Boolean {
            var currentUser = auth.currentUser
            return currentUser?.let {

                albe_email = currentUser.email
                currentUser.isEmailVerified

            } ?: let {
                false
            }

        }

    }

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage

    }
}