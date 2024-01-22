package kr.ac.wku.albeapp.logins

import android.content.Context

// 로그인 세션 확인하는 클래스
//
class LoginSession(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)

    val phoneNumber: String?
        get() = sharedPreferences.getString("phoneNumber", null)

    val userName: String?
        get() = sharedPreferences.getString("userName", null)

    val isLoggedIn: Boolean
        get() = sharedPreferences.contains("phoneNumber") // phoneNumber 키가 존재하는지 확인
}