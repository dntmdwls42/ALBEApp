package kr.ac.wku.albeapp.HomeMenu

class Friendlist {
    data class Friend(
        val profileImage: Int,  // 프로필 이미지 URL
        val userName: String,   // 사용자 이름
        val userPhoneNumber: String,  // 사용자 전화번호
        val userStatus: Int    // 사용자 상태
    )
}