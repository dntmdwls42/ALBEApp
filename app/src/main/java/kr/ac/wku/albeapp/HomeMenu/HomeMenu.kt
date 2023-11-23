package kr.ac.wku.albeapp.HomeMenu

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kr.ac.wku.albeapp.R

class HomeMenu : AppCompatActivity() {
    // 실시간 파이어베이스 관련 세팅
    private lateinit var database: DatabaseReference

    // 파이어베이스 스토리지 관련 세팅
    private lateinit var storage: FirebaseStorage

    // 상태 표시
    private val ACTIVE = 1 // 활성 = 센서 작동중
    private val INACTIVE = 0 // 비활성 = 센서 없음 감지
    private val TEMP_INACTIVE = 2 // 센서 환경설정에서 비활성화 = 일부러 끔

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_menu)

        val userPhoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        // 의심 구간 1
        val imageRef = storageRef.child("image/$userPhoneNumber")

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            val profileImage: ImageView = findViewById(R.id.home_profileimage)
            Glide.with(this)
                .load(imageUrl)
                .into(profileImage)
        }.addOnFailureListener {
            // 이미지를 가져오지 못한 경우에 대한 처리
        }

        val recyclerView: RecyclerView = findViewById(R.id.home_friendlist_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Firebase에서 데이터를 가져옵니다.
        database = FirebaseDatabase.getInstance().reference
        // 파이어베이스 스토리지에서 참조
        storage = FirebaseStorage.getInstance()


        val userRef = database.child("users").child(userPhoneNumber!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println("데이타 스냅샷: $dataSnapshot") // 제대로 나오는지 로그 찍는거
                val userName = dataSnapshot.child("userName").value as? String
                val userStatus = dataSnapshot.child("userState").value as? Int

                // 화면에 사용자 정보를 표시합니다.
                findViewById<TextView>(R.id.home_username).text = userName
                findViewById<TextView>(R.id.home_userphonenumber).text = userPhoneNumber
                when (userStatus) {
                    ACTIVE -> {
                        findViewById<TextView>(R.id.home_userstatus_text).text = "활성 상태"
                    }
                    INACTIVE -> {
                        findViewById<TextView>(R.id.home_userstatus_text).text = "비활성 상태"
                    }
                    TEMP_INACTIVE -> {
                        findViewById<TextView>(R.id.home_userstatus_text).text = "일시적 비활성 상태"
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("로그인 한 사용자 정보 받기 실패: ${databaseError.toException()}")
            }
        })

        // database.child("users") 부분 추가
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val friendList = mutableListOf<Friendlist.Friend>()
                val totalUsers = dataSnapshot.childrenCount
                var loadedUsers = 0

                // Firebase에서 가져온 데이터를 사용해 Friend 객체 리스트를 만듭니다.
                for (userSnapshot in dataSnapshot.children) {
                    val phoneNumber = userSnapshot.key
                    val userName = userSnapshot.child("userName").value as? String
                    val userID = userSnapshot.child("userID").value as? String
                    val userStatus = userSnapshot.child("userState").value as? Int

                    // Firebase 스토리지에서 이미지 URL을 가져옵니다.
                    val imageRef = storage.getReference().child("image/$userPhoneNumber")
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        val friend =
                            Friendlist.Friend(imageUrl, userName, userID, userStatus)
                        friendList.add(friend)

                        loadedUsers++

                        // RecyclerView에 어댑터를 설정합니다.
                        if (loadedUsers == totalUsers.toInt()) {
                            val adapter = FriendListAdapter(friendList)
                            recyclerView.adapter = adapter
                        }

                    }.addOnFailureListener {
                        // 이미지 URL을 가져오는 데 실패했습니다. 대체 이미지를 사용합니다.
                        val imageUrl =
                            "https://via.placeholder.com/150"  // 대체 이미지 URL을 여기에 입력하세요.
                        val friend = Friendlist.Friend(imageUrl, userName, userID, userStatus)
                        friendList.add(friend)

                        loadedUsers++
                        if (loadedUsers == totalUsers.toInt()) {
                            val adapter = FriendListAdapter(friendList)
                            recyclerView.adapter = adapter
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Firebase에서 데이터를 가져오는 데 실패했습니다.
                println("다른 사용자 정보 받기 실패..: ${databaseError.toException()}")
            }
        })
    }

    private fun getFriendList(imageUrl: String): List<Friendlist.Friend> {
        // 여기에서는 실제 친구 목록을 가져와야 합니다.
        // 일단 임시로 빈 목록을 반환하도록 설정했습니다.
        return emptyList()
    }
}