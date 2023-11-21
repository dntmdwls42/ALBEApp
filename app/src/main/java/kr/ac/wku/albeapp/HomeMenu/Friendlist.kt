package kr.ac.wku.albeapp.HomeMenu

class Friendlist {
    data class Friend(
        var imageUrl: String = "", // 사진이 저장된 경로
        var userName: String? = null,   // 사용자 이름
        val userPhoneNumber: String? = null,  // 사용자 전화번호 = 이게 아이디
        val userStatus: Int? = null    // 사용자 상태
    )
}