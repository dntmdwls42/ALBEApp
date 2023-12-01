package kr.ac.wku.albeapp.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sensorState = intent.getStringExtra("sensor_state")
        val timerInfo = intent.getStringExtra("timer_info")

        when (sensorState) {
            "센서 ON" -> {

            }
            "센서 OFF" -> {

            }
        }
    }
}