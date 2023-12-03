package kr.ac.wku.albeapp.HomeMenu

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RealtimeDataObserver(private val activity: HomeMenu) {
    private val database = FirebaseDatabase.getInstance().reference

    fun observeUserInfo(userPhoneNumber: String) {
        val userRef = database.child("users").child(userPhoneNumber)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                activity.updateUserInfo(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("RealtimeDataObserver", "사용자 정보 갱신 실패: ${databaseError.toException()}")
            }
        })
    }
}