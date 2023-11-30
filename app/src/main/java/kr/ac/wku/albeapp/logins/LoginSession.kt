package kr.ac.wku.albeapp.logins

import android.content.Context
// 파이어베이스 인증 관련
import com.google.firebase.auth.FirebaseAuth

// 로그인 세션 확인하는 클래스
class LoginSession(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
    // 파이어베이스 구글 로그인 인증 관련
    private val firebaseAuth = FirebaseAuth.getInstance()

    val phoneNumber: String?
        get() = sharedPreferences.getString("phoneNumber", null)

    val userName: String?
        get() = sharedPreferences.getString("userName", null)

    val isLoggedIn: Boolean
        get() = sharedPreferences.contains("phoneNumber") // phoneNumber 키가 존재하는지 확인

    val uid: String?
        get() = firebaseAuth.currentUser?.uid // 현재 로그인한 Firebase 사용자의 고유 ID 반환
}
