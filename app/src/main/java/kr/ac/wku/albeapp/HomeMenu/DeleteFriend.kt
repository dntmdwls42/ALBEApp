package kr.ac.wku.albeapp.HomeMenu

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

// 친구 삭제하는 내용을 가진 클래스
class DeleteFriend(private val context: Context) {
    private val database = FirebaseDatabase.getInstance().reference

    fun deleteFriend(userPhoneNumber: String, friendPhoneNumber: String) {
        Log.d("친구삭제 확인","${userPhoneNumber},${friendPhoneNumber}")
        // 현재 사용자의 친구 목록에서 친구를 삭제합니다.
        database.child("users").child(userPhoneNumber).child("Friends").child(friendPhoneNumber)
            .removeValue()
            .addOnSuccessListener {
                // 친구의 친구 목록에서 현재 사용자를 삭제합니다.
                database.child("users").child(friendPhoneNumber).child("Friends").child(userPhoneNumber)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "친구가 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        // 친구 삭제 후 다시 로딩
                        (context as HomeMenu).loadFriendsData()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "친구의 친구 목록에서 현재 사용자를 삭제하는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "현재 사용자의 친구 목록에서 친구를 삭제하는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}