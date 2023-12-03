package kr.ac.wku.albeapp.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.ac.wku.albeapp.HomeMenu.HomeMenu
import kr.ac.wku.albeapp.R

class MessagingService : FirebaseMessagingService() {
    var msg = ""
    var title = ""

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("MyLog", "FCM token : $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.d("MyLog", "FCM message : ${p0.notification}")
        Log.d("MyLog", "FCM message : ${p0.data}")

        title = p0.notification?.title.toString()
        msg = p0.notification?.body.toString()
        Log.d("MyLog", "FCM message : ${title}")
        Log.d("MyLog", "FCM message : ${msg}")

        if (p0.data.isNotEmpty()) {

        }

        p0.notification?.let {
            sendNotification(p0.notification?.body)
        }
    }

    private fun sendNotification(messageBody: String?) {
        val intent = Intent(this, HomeMenu::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent =
            PendingIntent.getActivity(this, 101, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)

        val channelId: String = "one-channel"

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //26버전 이상
            val channel = NotificationChannel(
                channelId,
                "My Channel One",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}
