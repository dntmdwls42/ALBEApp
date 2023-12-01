package kr.ac.wku.albeapp.message

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.ac.wku.albeapp.HomeMenu.Friendlist
import kr.ac.wku.albeapp.logins.UserState

// 메시지가 가게 하는 클래스
class MessagingService : FirebaseMessagingService() {

    // 클라우드 서버에 등록되었을 때 호출
    // 파라미터로 전달된 token 이 앱을 구분하기 위한 고유한 키가 된다
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // token 을 서버로 전송
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("메시지 서비스", "FCM토큰 얻기 실패", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d("메시지 서비스", token)
        })

    }

    // 클라우드 서버에서 메시지를 전송하면 자동으로 호출
    // 이 메서드 안에서 메시지를 처리하여 사용자에게 알림을 보내거나 할 수 있다
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            // 이 부분에서 데이터를 갱신하는 로직을 실행합니다.
            // 예를 들어 LiveData를 갱신하거나, BroadcastReceiver를 사용하여 HomeMenu에 알릴 수 있습니다.
            val intent = Intent("FCM_MESSAGE_RECEIVED")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

    }

}