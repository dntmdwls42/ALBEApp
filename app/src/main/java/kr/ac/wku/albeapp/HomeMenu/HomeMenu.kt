package kr.ac.wku.albeapp.HomeMenu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kr.ac.wku.albeapp.R

class HomeMenu : AppCompatActivity() {
    // 실시간 파이어베이스 관련 세팅
    private lateinit var database: DatabaseReference
    // 파이어베이스 스토리지 관련 세팅
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_menu)

        // Firebase에서 데이터를 가져옵니다.
        database = FirebaseDatabase.getInstance().reference
        // 파이어베이스 스토리지에서 참조
        storage = FirebaseStorage.getInstance()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val friendList = mutableListOf<Friendlist.Friend>()

                // Firebase에서 가져온 데이터를 사용해 Friend 객체 리스트를 만듭니다.
                for (userSnapshot in dataSnapshot.children) {
                    val userName = userSnapshot.child("userName").value as? String
                    val userPhoneNumber = userSnapshot.child("userPhoneNumber").value as? String
                    val userStatus = userSnapshot.child("userStatus").value as? Int

                    // Firebase 스토리지에서 이미지 URL을 가져옵니다.
                    val imageRef = storage.getReference().child("image/$userPhoneNumber")
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        val friend = Friendlist.Friend(imageUrl, userName, userPhoneNumber, userStatus)
                        friendList.add(friend)

                        // RecyclerView에 어댑터를 설정합니다.
                        val adapter = FriendListAdapter(friendList)
                        val recyclerView: RecyclerView = findViewById(R.id.home_friendlist_recyclerview)
                        recyclerView.layoutManager = LinearLayoutManager(this@HomeMenu)
                        recyclerView.adapter = adapter
                    }.addOnFailureListener {
                        // 이미지 URL을 가져오는 데 실패했습니다.
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Firebase에서 데이터를 가져오는 데 실패했습니다.
                println("Failed to get data from Firebase: ${databaseError.toException()}")
            }
        })
    }
}