package kr.ac.wku.albeapp.photos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kr.ac.wku.albeapp.R
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    lateinit var imageIv: ImageView
    lateinit var textEt: EditText
    lateinit var uploadBtn: Button

    val IMAGE_PICK = 1111

    var selectImage: Uri? = null

    lateinit var storage: FirebaseStorage
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // 회원가입 액티비티로부터 전화번호 값 넘어옴
        val userPhoneNumber = intent.getStringExtra("phoneNumber")

        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        imageIv = findViewById(R.id.image_iv)
        textEt = findViewById(R.id.text_et)
        uploadBtn = findViewById(R.id.upload_btn)

        imageIv.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK) //선택하면 무언가를 띄움. 묵시적 호출
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK)
        }
        uploadBtn.setOnClickListener {
            if (selectImage != null) {
                var fileName =
                    SimpleDateFormat("yyyyMMddHHmmss").format(Date()) // 파일명이 겹치면 안되기 떄문에 시년월일분초 지정
                storage.getReference().child("image").child(userPhoneNumber!!)
                    .putFile(selectImage!!)  // 어디에 업로드할지 지정
                    .addOnSuccessListener { taskSnapshot -> // 업로드 정보를 담는다
                        taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { it ->
                            var imageUrl = it.toString()
                            var photo = Photo(textEt.text.toString(), imageUrl)
                            firestore.collection("photo")
                                .document().set(photo)
                                .addOnSuccessListener {
                                    finish()
                                }
                        }
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectImage = data?.data
            imageIv.setImageURI(selectImage)
        }
    }
}