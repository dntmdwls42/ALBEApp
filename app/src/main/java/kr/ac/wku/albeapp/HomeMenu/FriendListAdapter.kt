package kr.ac.wku.albeapp.HomeMenu

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kr.ac.wku.albeapp.R
import kr.ac.wku.albeapp.logins.LoginSession
import kr.ac.wku.albeapp.logins.UserState


class FriendListAdapter(var friendList: List<Friendlist.Friend>) :
    RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    interface OnFriendLongClickListener {
        fun onFriendLongClick(friend: Friendlist.Friend)
    }

    private var onFriendLongClickListener: OnFriendLongClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.home_profileimage)
        val userName: TextView = itemView.findViewById(R.id.home_username)
        val userPhoneNumber: TextView = itemView.findViewById(R.id.home_userphonenumber)
        val userState: ImageView = itemView.findViewById(R.id.home_userstatus)
        val userStateText: TextView = itemView.findViewById(R.id.home_userstatus_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friendlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val friend = friendList[position]
        val phoneNumber = friend.userID
        val userState = UserState.fromDescription(friend.userState ?: UserState.NOTHING.description)
        Log.w("친구 어댑터","바인딩 데이타: $friend")
        Log.w("새 어댑터"," 상태 값 확인 : ${userState}")

        val imageRef = storage.getReference().child("image/$phoneNumber")

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView.context)
                .load(uri) // 친구의 이미지 URL을 사용합니다.
                .into(holder.profileImage)
        }.addOnFailureListener {
            // 이미지 로드에 실패했을 때 기본 이미지를 설정합니다.
            holder.profileImage.setImageResource(R.drawable.base_profile_image)
        }

        holder.userName.text = friend.userName
        // 번호가 없어도 일단 나오게 수정
        holder.userPhoneNumber.text = friend.userID ?: "번호 없음"
        holder.userState.setImageResource(userState.imageResId) // 여기에 수정된 코드 추가함
        holder.userStateText.text = userState.description // 사용자 상태를 텍스트로 표시합니다.

        holder.itemView.setOnLongClickListener {
            // 롱 클릭 시 친구 삭제 다이얼로그 표시
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("친구 삭제")
                .setMessage("${friend.userName}을(를) 친구 목록에서 삭제하시겠습니까?")
                .setPositiveButton("삭제") { dialog, _ ->
                    val userPhoneNumber = LoginSession(holder.itemView.context).phoneNumber
                    val friendPhoneNumber = friend.userID
                    if (friendPhoneNumber != null) {
                        if (userPhoneNumber != null) {
                            DeleteFriend(holder.itemView.context).deleteFriend(
                                userPhoneNumber,
                                friendPhoneNumber
                            )
                        }
                    }

                    dialog.dismiss()

                    // 친구 삭제 후 콜백 호출
                    onFriendLongClickListener?.onFriendLongClick(friend)

                }
                .setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
                .show()


            true
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    fun setOnFriendLongClickListener(listener: OnFriendLongClickListener) {
        onFriendLongClickListener = listener
    }
}