package kr.ac.wku.albeapp.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.ac.wku.albeapp.HomeMenu.HomeMenu
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.logins.LoginPageActivity

class MessagingService : FirebaseMessagingService() {
    // FCM 토큰 관련 코드 작성
}
