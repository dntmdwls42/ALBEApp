package kr.ac.wku.albeapp.HomeMenu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.databinding.ActivityHomeMenuBinding
import kr.ac.wku.albeapp.databinding.ActivitySettingBinding
import kr.ac.wku.albeapp.setting.SettingActivity

class HomeMenu : AppCompatActivity() {
    // 실시간 파이어베이스 관련 세팅
    private lateinit var database: DatabaseReference

    // 파이어베이스 스토리지 관련 세팅
    private lateinit var storage: FirebaseStorage

    // 데이터바인딩 설정
    private lateinit var binding: ActivityHomeMenuBinding

    // 상태 표시
    private val ACTIVE = 1 // 활성 = 센서 작동중
    private val INACTIVE = 0 // 비활성 = 센서 없음 감지
    private val TEMP_INACTIVE = 2 // 센서 환경설정에서 비활성화 = 일부러 끔

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_menu)


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

        // 설정 화면 이벤트 이동
        binding.fromSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("phoneNumber", userPhoneNumber)  // 전화번호를 Intent에 추가
            Toast.makeText(this, "설정화면으로 이동합니다.", Toast.LENGTH_SHORT).show()

            // 환경 설정 레이아웃으로 이동
            startActivity(intent)
        }

        // 로그인 세션 확인
        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.contains("phoneNumber") // phoneNumber 키가 존재하는지 확인
        Log.d("로그인 세션 확인", "로그인 세션 상태: $isLoggedIn")


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

        // 친구 추가 버튼인 이미지 버튼을 눌렀을때 생기는 이벤트
        binding.addFriend.setOnClickListener {
            // 다이얼로그 생성
            val builder = AlertDialog.Builder(this@HomeMenu)
            builder.setTitle("친구 추가")

            // 입력 필드 설정
            val input = EditText(this@HomeMenu)
            input.inputType = InputType.TYPE_CLASS_PHONE  // 입력 타입을 전화번호로 설정
            builder.setView(input)

            // 검색 버튼을 눌렀을 때의 동작 설정
            builder.setPositiveButton("검색") { dialog, _ ->
                val searchPhoneNumber = input.text.toString()

                database.child("users").child(searchPhoneNumber).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val userName = dataSnapshot.child("userName").value as? String
                            val userStatus = dataSnapshot.child("userState").value as? Int

                            // 검색 결과를 다이얼로그로 보여줌
                            showSearchResultDialog(userName, userStatus)
                        } else {
                            Toast.makeText(this@HomeMenu, "해당 번호를 가진 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@HomeMenu, "데이터 검색에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                })
                dialog.dismiss()
            }

            // 취소 버튼을 눌렀을 때의 동작 설정
            builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }

            builder.show()
        }
    }

    // 사용자 검색 결과를 보여주는 함수
    // userName: 검색한 사용자의 이름
    // userStatus: 검색한 사용자의 상태
    private fun showSearchResultDialog(userName: String?, userStatus: Int?) {
        // AlertDialog.Builder를 사용하여 다이얼로그를 만듬
        val builder = AlertDialog.Builder(this@HomeMenu)
        // 다이얼로그의 제목을 설정
        builder.setTitle("검색 결과")

        // 검색한 사용자의 이름과 상태를 문자열로 만들어 메시지로 설정
        // 사용자의 상태는 ACTIVE, INACTIVE, TEMP_INACTIVE 중 하나밖에 없잖아
        // when을 사용하여 각 상태에 해당하는 문자열을 만듬
        val message = "이름: $userName\n상태: ${when (userStatus) {
            ACTIVE -> "활성"
            INACTIVE -> "비활성"
            TEMP_INACTIVE -> "일시적 비활성"
            else -> "알 수 없음"
        }}"

        // 만든 메시지를 다이얼로그에 설정함.
        builder.setMessage(message)
        // 다이얼로그에 '확인' 버튼을 추가하고, 이 버튼을 누르면 다이얼로그가 닫히도록 설정함
        builder.setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }

        // 설정이 완료된 다이얼로그를 보여준다
        builder.show()
    }

    private fun getFriendList(imageUrl: String): List<Friendlist.Friend> {
        // 여기에서는 실제 친구 목록을 가져와야 합니다.
        // 일단 임시로 빈 목록을 반환하도록 설정했습니다.
        return emptyList()
    }
}