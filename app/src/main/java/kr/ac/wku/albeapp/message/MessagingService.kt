package kr.ac.wku.albeapp.message

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    // FCM 토큰 관련 코드 작성

    // 이거는 토큰이 전달이 됐을때 , call 되는 함수
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM","FCM TOKEN : ${token}")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // message.data 는 getdata() 로 서버에서 받아 온 데이터를 말함.
        Log.d("FCM","SERVER MESSAGE : ${message.data}")
    }
}
