package kr.ac.wku.albeapp.HomeMenu

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddFriend {
    private lateinit var database: DatabaseReference

    init {
        database = FirebaseDatabase.getInstance().reference
    }

    fun addNewFriend(currentUserPhoneNumber: String, newFriendPhoneNumber: String) {
        // 새로운 친구의 userState 값을 가져옵니다.
        database.child("users").child(newFriendPhoneNumber).child("userState")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friendUserState = snapshot.value as? Int ?: 0

                    // 현재 사용자의 친구 목록에 새로운 친구를 추가함
                    val userRef = database.child("users").child(currentUserPhoneNumber).child("Friends").child(newFriendPhoneNumber)
                    val friendData = mapOf(
                        "phoneNumber" to newFriendPhoneNumber,
                        "userState" to friendUserState
                    )
                    userRef.setValue(friendData)

                    // 새로운 친구의 친구 목록에 현재 사용자를 추가함
                    // 현재 사용자의 userState 값을 가져옵니다.
                    database.child("users").child(currentUserPhoneNumber).child("userState")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val currentUserState = snapshot.value as? Int ?: 0

                                val friendRef = database.child("users").child(newFriendPhoneNumber).child("Friends").child(currentUserPhoneNumber)
                                val userData = mapOf(
                                    "phoneNumber" to currentUserPhoneNumber,
                                    "userState" to currentUserState
                                )
                                friendRef.setValue(userData)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                println("현재 사용자의 userState 받기 실패: ${error.toException()}")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    println("새 친구의 userState 받기 실패: ${error.toException()}")
                }
            })
    }
}