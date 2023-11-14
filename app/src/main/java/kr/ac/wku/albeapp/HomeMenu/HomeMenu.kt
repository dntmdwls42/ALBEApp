package kr.ac.wku.albeapp.HomeMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ac.wku.albeapp.R

class HomeMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_menu)

        val friendList: List<Friendlist.Friend> = listOf(
            Friendlist.Friend(R.drawable.ic_launcher_foreground, "예시1", "01012210001", 0),
            Friendlist.Friend(R.drawable.ic_launcher_background, "예시2", "01012210002", 1),
            Friendlist.Friend(R.drawable.ic_launcher_background, "예시3", "01012210003", 2),
            // 필요한 만큼 여기에 객체 추가 = 친구창에 친구목록이 자동으로 늘어남.
        )

        val adapter = FriendListAdapter(friendList)
        val recyclerView: RecyclerView = findViewById(R.id.home_friendlist_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}