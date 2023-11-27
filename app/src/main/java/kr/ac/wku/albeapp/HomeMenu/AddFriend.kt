package kr.ac.wku.albeapp.HomeMenu

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddFriend {
    private lateinit var database: DatabaseReference

    init {
        database = FirebaseDatabase.getInstance().reference
    }

    fun addNewFriend(currentUserPhoneNumber: String, newFriendPhoneNumber: String) {
        // 현재 사용자의 친구 목록에 새로운 친구를 추가함
        val userRef = database.child("users").child(currentUserPhoneNumber).child("Friends")
        val friendData = mapOf(
            newFriendPhoneNumber to true
            // 필요한 다른 정보를 여기에 추가하세요.
        )
        userRef.updateChildren(friendData)

        // 새로운 친구의 친구 목록에 현재 사용자를 추가함
        val friendRef = database.child("users").child(newFriendPhoneNumber).child("Friends")
        val userData = mapOf(
            currentUserPhoneNumber to true
            // 필요한 다른 정보를 여기에 추가하세요.
        )
        friendRef.updateChildren(userData)
    }
}