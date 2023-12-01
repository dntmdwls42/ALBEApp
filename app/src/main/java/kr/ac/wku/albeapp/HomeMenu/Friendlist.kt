package kr.ac.wku.albeapp.HomeMenu

class Friendlist {
    data class Friend(
        var imageUrl: String? = null, // 사진이 저장된 경로
        var userName: String? = null,   // 사용자 이름
        val userID: String? = null,  // 사용자 전화번호 = 이게 아이디
        val userState: String? = null    // 사용자 상태
    )
}