package kr.ac.wku.albeapp.logins

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import kr.ac.wku.albeapp.R

class FindMyId : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_my_id)

        val cancelfindpassword: Button = findViewById(R.id.find_cancelfind_btn)

        cancelfindpassword.setOnClickListener {
            // 버튼을 누를 시 이전 페이지로 돌아갑니다.
            Toast.makeText(this, "취소했습니다. 되돌아가는 중..", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }
    }
}