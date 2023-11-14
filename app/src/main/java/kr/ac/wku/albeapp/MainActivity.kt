package kr.ac.wku.albeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.ac.wku.albeapp.databinding.ActivityMainBinding
import kr.ac.wku.albeapp.setting.SettingActivity
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kr.ac.wku.albeapp.HomeMenu.HomeMenu
import kr.ac.wku.albeapp.logins.LoginPageActivity
import kr.ac.wku.albeapp.logins.UserSignUp
import kr.ac.wku.albeapp.photos.Photo
import kr.ac.wku.albeapp.photos.PhotoActivity
import kr.ac.wku.albeapp.photos.PhotoAdapter

// 초기 로그인 화면 액티비티
class MainActivity : AppCompatActivity(), PhotoAdapter.OnItemClickListener {

    // 리사이클러 뷰 , 파이어베이스 세팅
    lateinit var email: TextView
    lateinit var auth: FirebaseAuth
    lateinit var listRv: RecyclerView
    lateinit var photoAdapter: PhotoAdapter
    lateinit var photoList: ArrayList<Photo>
    lateinit var firestore: FirebaseFirestore

    // "메인 페이지" 데이터 바인딩 세팅 1
    lateinit var binding: ActivityMainBinding

    // 파이어베이스 리얼타임 데이터베이스 테스트 용 변수
    private val db = Firebase.database
    private val myRef = db.getReference("안녕 파이어베이스")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // "메인 페이지" 데이터 바인딩 세팅 2
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // 전화번호를 가져옴
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        // 사진 업로드 세팅 2
        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.email_tv)
        email.text = auth.currentUser?.email

        firestore = FirebaseFirestore.getInstance()

        
        listRv = findViewById(R.id.list_rv)

        photoList = ArrayList()
        photoAdapter = PhotoAdapter(this, photoList)

        listRv.layoutManager = GridLayoutManager(this, 3)
        listRv.adapter = photoAdapter

        photoAdapter.onItemClickListener = this

        firestore.collection("photo")
            .addSnapshotListener { querySnapshot, FirebaseFIrestoreException ->
                if (querySnapshot != null) {
                    for (dc in querySnapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            var photo = dc.document.toObject(Photo::class.java)
                            photo.id = dc.document.id
                            photoList.add(photo)
                        }
                    }
                    photoAdapter.notifyDataSetChanged()
                }
            }

        

        binding.fromSetting.setOnClickListener {
            // 환경 설정 화면으로 이동하는 이벤트

            // 화면 이동 :  intent
            // imageupload 레이아웃이 도착지로 설정함.

            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("phoneNumber", phoneNumber) // 전화번호를 Intent에 추가

            // 환경 설정 레이아웃으로 이동
            startActivity(intent)

        }
        
        binding.fromSignup.setOnClickListener { 
            // 회원 가입 화면으로 이동하는 이벤트
            var myIntent = Intent(this, UserSignUp::class.java)

            // 회원가입 화면 레이아웃으로 이동
            startActivity(myIntent)
        }

        binding.fromlogin.setOnClickListener {
            // 로그인 화면으로 이동하는 이벤트
            var myIntent = Intent(this, LoginPageActivity::class.java)

            // 로그인 화면 레이아웃으로 이동
            startActivity(myIntent)
        }

        
        binding.textUpload.setOnClickListener {
            // 데이터 쓰기 버튼 했을때 파이어베이스에 쓰이는지
            writeValue("센서값 : 12")
            writeValue("다르게 한번 써보기")
        }

        binding.frommain.setOnClickListener {
            // 메인 화면으로 이동하는 이벤트
            var myIntent = Intent(this, HomeMenu::class.java)

            // 메인 화면 레이아웃으로 이동
            startActivity(myIntent)
        }

    }

    // 실시간 데이터 베이스 , 데이터 쓰기
    private fun writeValue(data: String) {
        myRef.setValue(data)
    }

    // 리사이클러 뷰에서 아이템 = 사진을 눌렀을때 생기는 이벤트
    override fun onItemClick(photo: Photo) {
        var intent = Intent(this, PhotoActivity::class.java)
        intent.putExtra("id", photo.id)
        startActivity(intent)
    }

}