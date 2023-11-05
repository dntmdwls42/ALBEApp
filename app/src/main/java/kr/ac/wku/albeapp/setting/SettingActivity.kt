package kr.ac.wku.albeapp.setting

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import kr.ac.wku.albeapp.R

// 설정 화면 액티비티
class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting)

        val moreSettingSwitch = findViewById<Switch>(R.id.moresetting)
        val userDeleteButton = findViewById<Button>(R.id.userdelete)

        userDeleteButton.visibility = View.GONE //회원 탈퇴 버튼은 숨겨져 있음(공간차지 x)

        moreSettingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) { //버튼이 켜지면
                userDeleteButton.visibility = View.VISIBLE // 탈퇴버튼 표시
            } else { //버튼이 꺼져 있다면
                userDeleteButton.visibility = View.GONE // 탈퇴버튼 숨김상태
            }
        }
    }
}