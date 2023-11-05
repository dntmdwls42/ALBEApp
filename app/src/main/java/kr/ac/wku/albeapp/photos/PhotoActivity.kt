package kr.ac.wku.albeapp.photos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kr.ac.wku.albeapp.R

class PhotoActivity : AppCompatActivity() {

    lateinit var imageIv: ImageView
    lateinit var descriptionTv: TextView

    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        var id = intent.getStringExtra("id")

        firestore = FirebaseFirestore.getInstance()
        imageIv = findViewById(R.id.image_iv)
        descriptionTv = findViewById(R.id.description_tv)

        if (id != null) {
            firestore.collection("photo").document(id).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var photo = task.result?.toObject(Photo::class.java)
                    Glide.with(this).load(photo?.imageUrl).into(imageIv)
                    descriptionTv.text = photo?.description
                }
            }
        }
    }
}