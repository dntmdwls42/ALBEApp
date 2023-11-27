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
import kr.ac.wku.albeapp.logins.LoginPageActivity
import kr.ac.wku.albeapp.setting.SettingActivity
import kr.ac.wku.albeapp.HomeMenu.Friendlist.Friend
import kr.ac.wku.albeapp.HomeMenu.FriendListAdapter
import kr.ac.wku.albeapp.HomeMenu.Friendlist
import kr.ac.wku.albeapp.HomeMenu.AddFriend

class HomeMenu : AppCompatActivity() {
    // 실시간 파이어베이스 관련 세팅
    private lateinit var database: DatabaseReference

    // 파이어베이스 스토리지 관련 세팅
    private lateinit var storage: FirebaseStorage

    // 데이터바인딩 설정
    private lateinit var binding: ActivityHomeMenuBinding

    // 사용자 전화번호와 검색 전화번호 변수 추가 ( 친구 추가 관련 )
    private var userPhoneNumber: String? = null
    private var searchPhoneNumber: String? = null

    //뒤로 가기 토스트 앱종료 기능 측정 변수
    private var backPressedTime: Long = 0
    private val FINISH_INTERVAL_TIME: Long = 2000 //(2초)

    // 상태 표시
    companion object {
        const val ACTIVE = 1 // 활성 = 센서 작동중
        const val INACTIVE = 0 // 비활성 = 센서 없음 감지
        const val TEMP_INACTIVE = 2 // 센서 환경설정에서 비활성화 = 일부러 끔
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_menu)

        // 로그인 세션 확인
        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val phoneNumber = sharedPreferences.getString("phoneNumber", null)
        val userName = sharedPreferences.getString("userName", null)
        val isLoggedIn = sharedPreferences.contains("phoneNumber") // phoneNumber 키가 존재하는지 확인
        Log.d("로그인 세션 확인", "로그인 세션 상태: $isLoggedIn")
        Log.d("정보확인 1", "로그인 한 사용자 이름: $userName")
        Log.d("정보확인 2", "로그인 한 ID 확인 : $phoneNumber")

        if (phoneNumber != null && userName != null) {
            // 로그인한 사용자가 있는 경우
            // phoneNumber와 userName을 사용하는 코드
            // 예를 들어, userPhoneNumber에 phoneNumber를 할당할 수 있습니다.
            userPhoneNumber = phoneNumber
        } else {
            // 로그인한 사용자가 없는 경우
            // 로그인 페이지로 이동하거나 사용자에게 로그인하라는 메시지를 보여주는 등의 처리를 수행
            // 예를 들어, 다음과 같이 로그인 페이지로 이동할 수 있습니다.
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
            return
        }


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
                searchPhoneNumber = input.text.toString()

                // 검색한 전화번호를 가진 사용자의 데이터를 파이어베이스에서 가져옴
                database.child("users").child(searchPhoneNumber!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val userName = dataSnapshot.child("userName").value as? String
                                val userStatus = dataSnapshot.child("userState").value as? Int

                                // 파이어베이스 스토리지에서 사용자의 프로필 이미지를 가져옵니다.
                                storage.getReference()
                                    .child("image/$searchPhoneNumber").downloadUrl.addOnSuccessListener { uri ->
                                        // 검색 결과를 다이얼로그로 보여줌
                                        showSearchResultDialog(uri.toString(), userName, userStatus)
                                    }.addOnFailureListener {
                                        // 이미지를 가져오는 데 실패한 경우, 기본 이미지를 사용합니다.
                                        showSearchResultDialog(null, userName, userStatus)
                                    }

                            } else {
                                Toast.makeText(
                                    this@HomeMenu,
                                    "해당 번호를 가진 사용자를 찾을 수 없습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(this@HomeMenu, "데이터 검색에 실패했습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                dialog.dismiss()
            }

            // 취소 버튼을 눌렀을 때의 동작 설정
            builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }

            builder.show()
        }


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
        database.child("users").child(userPhoneNumber!!).child("Friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val friendList = mutableListOf<Friendlist.Friend>()
                    val totalFriends = dataSnapshot.childrenCount
                    var loadedFriends = 0

                    // Firebase에서 가져온 데이터를 사용해 Friend 객체 리스트를 만듭니다.
                    for (friendSnapshot in dataSnapshot.children) {
                        val friendPhoneNumber = friendSnapshot.key
                        val isFriend = friendSnapshot.value as? Boolean

                        if (isFriend == true) {
                            database.child("users").child(friendPhoneNumber!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val userName = snapshot.child("userName").value as? String
                                        val userID = snapshot.child("userID").value as? String
                                        val userStatus = snapshot.child("userState").value as? Int

                                        // Firebase 스토리지에서 이미지 URL을 가져옵니다.
                                        val imageRef =
                                            storage.getReference().child("image/$friendPhoneNumber")
                                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                                            val imageUrl = uri.toString()

                                            val friend =
                                                Friendlist.Friend(
                                                    imageUrl,
                                                    userName,
                                                    userID,
                                                    userStatus
                                                )
                                            friendList.add(friend)

                                            loadedFriends++

                                            // RecyclerView에 어댑터를 설정합니다.
                                            if (loadedFriends == totalFriends.toInt()) {
                                                val adapter = FriendListAdapter(friendList)
                                                recyclerView.adapter = adapter
                                            }

                                        }.addOnFailureListener {
                                            // 이미지 URL을 가져오는 데 실패했습니다. 대체 이미지를 사용합니다.
                                            val imageUrl =
                                                "https://via.placeholder.com/150"  // 대체 이미지 URL을 여기에 입력하세요.
                                            val friend = Friendlist.Friend(
                                                imageUrl,
                                                userName,
                                                userID,
                                                userStatus
                                            )
                                            friendList.add(friend)

                                            loadedFriends++
                                            if (loadedFriends == totalFriends.toInt()) {
                                                val adapter = FriendListAdapter(friendList)
                                                recyclerView.adapter = adapter
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        println("Friend 정보 받기 실패..: ${error.toException()}")
                                    }
                                })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Firebase에서 데이터를 가져오는 데 실패했습니다.
                    println("다른 사용자 정보 받기 실패..: ${databaseError.toException()}")
                }

            })


    }

    // 사용자 검색 결과를 보여주는 함수
    // imageUrl: 검색한 사용자의 프로필 이미지 URL
    // userName: 검색한 사용자의 이름
    // userStatus: 검색한 사용자의 상태
    private fun showSearchResultDialog(imageUrl: String?, userName: String?, userStatus: Int?) {
        // AlertDialog.Builder를 사용하여 다이얼로그를 만듬
        val builder = AlertDialog.Builder(this@HomeMenu)
        // 다이얼로그의 제목을 설정
        builder.setTitle("검색 결과")

        // 검색한 사용자의 이름과 상태를 문자열로 만들어 메시지로 설정
        // 사용자의 상태는 ACTIVE, INACTIVE, TEMP_INACTIVE 중 하나밖에 없잖아
        // when을 사용하여 각 상태에 해당하는 문자열을 만듬
        val message = "이름: $userName\n상태: ${
            when (userStatus) {
                ACTIVE -> "활성"
                INACTIVE -> "비활성"
                TEMP_INACTIVE -> "일시적 비활성"
                else -> "알 수 없음"
            }
        }\n이미지 URL: ${imageUrl ?: "이미지 없음"}"

        // 만든 메시지를 다이얼로그에 설정함.
        builder.setMessage(message)

        // 다이얼로그에 '친구추가' 버튼을 추가하고, 이 버튼을 누르면 추가 기능을 수행하도록 설정함
        builder.setPositiveButton("친구추가") { dialog, _ ->
            // 친구 추가 기능 구현
            val addFriend = AddFriend()

            if (userPhoneNumber != null && searchPhoneNumber != null) {
                addFriend.addNewFriend(userPhoneNumber!!, searchPhoneNumber!!)
                Toast.makeText(this@HomeMenu, "친구가 추가되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@HomeMenu, "전화번호 정보가 없어 친구를 추가할 수 없습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
            dialog.dismiss()
        }

        // 다이얼로그에 '취소' 버튼을 추가하고, 이 버튼을 누르면 다이얼로그가 닫히도록 설정함
        builder.setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }

        // 설정이 완료된 다이얼로그를 보여준다
        builder.show()


    }


    private fun getFriendList(imageUrl: String): List<Friendlist.Friend> {
        // 여기에서는 실제 친구 목록을 가져와야 합니다.
        // 일단 임시로 빈 목록을 반환하도록 설정했습니다.
        return emptyList()
    }

    //    여기는 홈메뉴 뒤로가기 토스트와 종료 기능입니다.
    override fun onBackPressed() { //빨간줄이 있다면 정상입니다. 건너뛰세요.
//        super.onBackPressed() <- IDE에서 추가를 권장하지만 이걸 추가하면 뒤로가기가 되어버림 사용 x
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            finishAffinity()  // 앱의 모든 액티비티를 종료
            System.exit(0)  // 시스템 종료 (선택적)
        } else {
            backPressedTime = tempTime
            Toast.makeText(applicationContext, "한 번 더 뒤로가기 하면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }


}