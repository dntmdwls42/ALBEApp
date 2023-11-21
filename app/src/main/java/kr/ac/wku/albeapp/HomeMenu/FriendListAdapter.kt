package kr.ac.wku.albeapp.HomeMenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.ac.wku.albeapp.R

class FriendListAdapter(private val friendList: List<Friendlist.Friend>) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.home_profileimage)
        val userName: TextView = itemView.findViewById(R.id.home_username)
        val userPhoneNumber: TextView = itemView.findViewById(R.id.home_userphonenumber)
        val userStatus: ImageView = itemView.findViewById(R.id.home_userstatus)
        val userStatusText: TextView = itemView.findViewById(R.id.home_userstatus_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friendlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friendList[position]

        Glide.with(holder.itemView)  // Context
            .load(friend.imageUrl)  // 로드할 이미지 URL
            .into(holder.profileImage)  // 이미지를 표시할 ImageView

        holder.userName.text = friend.userName
        holder.userPhoneNumber.text = friend.userPhoneNumber
        when (friend.userStatus) {
            0 -> {
                holder.userStatus.setImageResource(R.drawable.check)  // 활성 상태일 때의 이미지
                holder.userStatusText.text = "활성"
            }
            1 -> {
                holder.userStatus.setImageResource(R.drawable.noinfo)  // 비활성 상태일 때의 이미지
                holder.userStatusText.text = "비활성"
            }
            else -> {
                holder.userStatus.setImageResource(R.drawable.away)  // AWAY 상태일 때의 이미지
                holder.userStatusText.text = "일시적 비활성"
            }
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}