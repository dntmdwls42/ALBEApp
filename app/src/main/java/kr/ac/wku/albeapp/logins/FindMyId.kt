package kr.ac.wku.albeapp.logins

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kr.ac.wku.albeapp.HomeMenu.HomeMenu
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityFindMyIdBinding
import kr.ac.wku.albeapp.databinding.ActivitySettingBinding

class FindMyId : AppCompatActivity() {

    // 데이터바인딩 설정
    private lateinit var binding: ActivityFindMyIdBinding
    val database = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_my_id)

        // 레이아웃을 초기 상태에서는 안보이게 설정합니다.
        binding.findpwbar.visibility = View.GONE // 여기서 yourLayoutId는 실제 XML에서 레이아웃의 id입니다.

        // 체크박스의 체크 상태가 변경될 때마다 호출되는 리스너를 설정합니다.
        binding.findpwcheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 체크박스가 체크되었을 때, 레이아웃을 보이게 합니다.
                binding.findpwbar.visibility = View.VISIBLE
            } else {
                // 체크박스가 해제되었을 때, 레이아웃을 안보이게 합니다.
                binding.findpwbar.visibility = View.GONE
            }
        }

        binding.findMyPasswordBtn.setOnClickListener {

            val inputPhoneNumber = binding.findphonenum.text.toString()
            val newPassword = binding.updatepwnum.text.toString()

            val userRef = database.getReference("users").child(inputPhoneNumber)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userData = dataSnapshot.getValue(UserData::class.java)

                    if (userData != null) {
                        // 비밀번호 변경
                        userData.userPW = newPassword
                        userRef.setValue(userData)

                        Toast.makeText(
                            this@FindMyId,
                            "비밀번호가 변경되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 비밀번호 변경 후 로그인 페이지로 돌아갑니다.
                        val intent = Intent(this@FindMyId, LoginPageActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 액티비티를 종료합니다.
                    } else {
                        Toast.makeText(
                            this@FindMyId,
                            "유효하지 않은 전화번호입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.findphonenum.text.clear()
                    binding.updatepwnum.text.clear()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리

                }
            })
        }

    }
}