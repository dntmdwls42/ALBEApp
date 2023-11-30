package kr.ac.wku.albeapp.HomeMenu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
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
import kr.ac.wku.albeapp.logins.LoginSession
import kr.ac.wku.albeapp.logins.UserData
import kr.ac.wku.albeapp.logins.UserStatus

class HomeMenu : AppCompatActivity() {
    // 실시간 파이어베이스 관련 세팅
    private lateinit var database: DatabaseReference

    // 파이어베이스 스토리지 관련 세팅
    private lateinit var storage: FirebaseStorage

    // 데이터바인딩 설정
    private lateinit var binding: ActivityHomeMenuBinding

    // 리사이클러 뷰 설정
    lateinit var recyclerView: RecyclerView

    // 사용자 전화번호와 검색 전화번호 변수 추가 ( 친구 추가 관련 )
    private var userPhoneNumber: String? = null
    private var searchPhoneNumber: String? = null

    //뒤로 가기 토스트 앱종료 기능 측정 변수
    private var backPressedTime: Long = 0
    private val FINISH_INTERVAL_TIME: Long = 2000 //(2초)


    // 세션 정보 받아오기(클래스를 통해 받아옴)
    private lateinit var loginSession: LoginSession

    // 현재 로그인한 사용자의 ID를 가져옵니다.
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // 사용자 데이터를 로드하는 함수
    fun loadUserData(userId: String? = null): LiveData<UserData> {
        val liveData = MutableLiveData<UserData>()

        // userId가 제공되면 해당 ID를 사용하고, 그렇지 않으면 userPhoneNumber를 사용합니다.
        val userKey = userId ?: userPhoneNumber!!

        database.child("users").child(userKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("userName").value as? String
                    val userID = snapshot.child("userID").value as? String
                    val userPW = snapshot.child("userPW").value as? String
                    val userState = snapshot.child("userState").value as? Int ?: 1
                    val friends = snapshot.child("Friends").value as? Map<String, Any>

                    val userData = UserData(userName, userID, userPW, userState, friends)
                    liveData.value = userData

                    val imageRef = storage.getReference().child("image/$userKey")
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        val userData = UserData(
                            userName,
                            userID,
                            null, // userPW는 보안상의 이유로 null로 설정합니다.
                            userState,
                            null // Friends는 이 함수에서 사용되지 않으므로 null로 설정합니다.
                        )
                        liveData.value = userData
                    }.addOnFailureListener {
                        val imageUrl = "https://via.placeholder.com/150"
                        val userData = UserData(
                            userName,
                            userID,
                            null, // userPW는 보안상의 이유로 null로 설정합니다.
                            userState,
                            null // Friends는 이 함수에서 사용되지 않으므로 null로 설정합니다.
                        )
                        liveData.value = userData
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("사용자 정보 받기 실패..: ${databaseError.toException()}")
                }
            })

        return liveData
    }

    // onCreate 여기에 있음
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_menu)

        // Firebase에서 데이터를 가져옵니다.
        database = FirebaseDatabase.getInstance().reference

        // 파이어베이스 스토리지에서 참조
        storage = FirebaseStorage.getInstance()

        // RecyclerView를 찾습니다.
        recyclerView = binding.homeFriendlistRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)


        // 로그인 세션 확인
        loginSession = LoginSession(this)

        Log.d("로그인 세션 확인", "로그인 세션 상태: ${loginSession.isLoggedIn}")
        Log.d("정보확인 1", "로그인 한 사용자 이름: ${loginSession.userName}")
        Log.d("정보확인 2", "로그인 한 ID 확인 : ${loginSession.phoneNumber}")

        userPhoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        // 어댑터를 먼저 생성하고 설정합니다.
        val adapter = FriendListAdapter(mutableListOf()).apply {
            setOnFriendLongClickListener(object : FriendListAdapter.OnFriendLongClickListener {
                override fun onFriendLongClick(friend: Friendlist.Friend) {
                    val friendListData = loadFriendsData()
                    friendListData.observe(this@HomeMenu, Observer { updatedFriendList ->
                        this@apply.friendList = updatedFriendList
                        this@apply.notifyDataSetChanged()
                    })
                }
            })
        }
        recyclerView.adapter = adapter

        // 사용자 데이터를 로드합니다.
        loadUserData(userId).observe(this, Observer { user ->
            // 사용자의 이름, 전화번호, 상태를 UI에 설정합니다.
            binding.homeUsername.text = user.userName
            binding.homeUserphonenumber.text = user.userID
            binding.homeUserstatusText.text = when (user.userState) {
                1 -> "활성 상태"
                else -> "비활성 상태"
            }
        })


        // 친구 목록 데이터를 불러옵니다.
        val friendListData = loadFriendsData()
        friendListData.observe(this, Observer { friendList ->
            // 데이터가 변경될 때마다 어댑터에 데이터를 업데이트합니다.
            adapter.friendList = friendList
            adapter.notifyDataSetChanged()
        })


        storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        // 이미지 경로 받음
        val imageRef = storageRef.child("image/$userPhoneNumber")

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            val profileImage: ImageView = binding.homeProfileimage
            Glide.with(this)
                .load(imageUrl)
                .into(profileImage)
        }.addOnFailureListener {
            Glide.with(this)
                .load(R.drawable.base_profile_image)
                .into(binding.homeProfileimage)
        }

        // 라이브러리 : 화면 아래로 잡아댕겼을때 새로고침
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            val friendListData = loadFriendsData()
            friendListData.observe(this@HomeMenu, Observer { updatedFriendList ->
                adapter.friendList = updatedFriendList
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            })
        }

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


        // 실시간 데이터베이스에서 로그인 한 사용자 받아오는 내용
        val userRef = database.child("users").child(userPhoneNumber!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                println("데이타 스냅샷: $dataSnapshot") // 제대로 나오는지 로그 찍는거
                val userName = dataSnapshot.child("userName").value as? String
                val userState = dataSnapshot.child("userState").value as? Int ?: 1
                val userStatus = UserStatus.fromStatus(userState)

                // 화면에 사용자 정보를 표시합니다.
                binding.homeUsername.text = userName
                binding.homeUserphonenumber.text = userPhoneNumber
                binding.homeUserstatusText.text = userStatus.description
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("로그인 한 사용자 정보 받기 실패: ${databaseError.toException()}")
            }
        })

//        // 로그인한 사용자의 정보를 가져옵니다.
//        loadUserData(userId).observe(this, Observer { user ->
//            // 사용자의 이름, 전화번호, 상태를 UI에 설정합니다.
//            binding.homeUsername.text = user.userName
//            binding.homeUserphonenumber.text = user.userPhoneNumber
//            binding.homeUserstatusText.text = when (user.userStatus) {
//                1 -> "활성 상태"
//                else -> "비활성 상태"
//            }
//
//            // 사용자의 프로필 이미지를 가져옵니다.
//            Glide.with(this)
//                .load(user.userImageUrl)
//                .into(binding.homeProfileimage)
//        })

        // 로그인한 사용자의 친구 목록을 가져옵니다.
        loadFriendsData(userId).observe(this, Observer { friends ->
            // 친구 목록을 UI에 설정합니다.
            // 여기에 친구 목록을 표시하는 코드를 추가하세요.
        })


    }


    // 파이어베이스 실시간 데이터베이스에서 친구목록을 가져오는 역할
    fun loadFriendsData(userId: String? = null): MutableLiveData<List<Friend>> {
        val liveData = MutableLiveData<List<Friendlist.Friend>>()
        val friendList = mutableListOf<Friendlist.Friend>()

        // 프로그레스 바를 보이게 합니다.
        binding.progressBar.visibility = View.VISIBLE

        val userKey = userId ?: userPhoneNumber!!

        database.child("users").child(userKey).child("Friends")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val totalFriends = dataSnapshot.childrenCount
                    var loadedFriends = 0

                    for (friendSnapshot in dataSnapshot.children) {
                        val friendPhoneNumber = friendSnapshot.key
                        val isFriend = friendSnapshot.value as? Boolean

                        if (isFriend == true) {
                            database.child("users").child(friendPhoneNumber!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        // 친구 목록 로딩이 끝나면 프로그레스 바를 숨깁니다.
                                        binding.progressBar.visibility = View.GONE

                                        val userName = snapshot.child("userName").value as? String
                                        val userID = snapshot.child("userID").value as? String

                                        // 유저상태(userState) 가 정상적으로 나오는지 테스트
                                        val userStatusNode = snapshot.child("userState")
                                        if (!userStatusNode.exists()) {
                                            Log.d("홈메뉴", "유저 상태확인해보자 $friendPhoneNumber")
                                        }

                                        var userStatus =
                                            snapshot.child("userState").value as? Int ?: 1

                                        val imageRef =
                                            storage.getReference().child("image/$friendPhoneNumber")
                                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                                            val imageUrl = uri.toString()

                                            val friend = Friendlist.Friend(
                                                imageUrl,
                                                userName,
                                                userID,
                                                userStatus
                                            )
                                            friendList.add(friend)

                                            loadedFriends++
                                            if (loadedFriends == totalFriends.toInt()) {
                                                liveData.value = friendList
                                            }
                                        }.addOnFailureListener {
                                            val imageUrl = "https://via.placeholder.com/150"
                                            val friend = Friendlist.Friend(
                                                imageUrl,
                                                userName,
                                                userID,
                                                userStatus
                                            )
                                            friendList.add(friend)

                                            loadedFriends++
                                            if (loadedFriends == totalFriends.toInt()) {
                                                liveData.value = friendList
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // 친구 목록 로딩이 실패하면 프로그레스 바를 숨깁니다.
                                        binding.progressBar.visibility = View.GONE

                                        println("Friend 정보 받기 실패..: ${error.toException()}")
                                    }
                                })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("다른 사용자 정보 받기 실패..: ${databaseError.toException()}")
                }
            })
        return liveData
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

        val status = UserStatus.fromStatus(userStatus ?: 1).description
        // 검색한 사용자의 이름과 상태를 문자열로 만들어 메시지로 설정
        val message =
            "이름: $userName\n" +
                    "상태: $status\n" +
                    "이미지 URL: ${imageUrl ?: "이미지 없음"}"

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
