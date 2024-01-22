package kr.ac.wku.albeapp.logins

// 사용자 데이터를 담을 데이터 클래스 정의
data class UserData(
    var userName: String? = null,
    var userID: String? = null,
    var userPW: String? = null,
    // 필요한 정보가 더 있다면 추가하세요.
    var userState: Int? = null, // 유저 상태 정상 : 1 , 비활성 : 0 이외 : 2
    var Friends: Map<String, Any>? = null,// 새로운 노드 추가, Map 타입으로 변경
    var FCMToken: String? = null, // FCM 토큰 추가
    var email: String? = null // 2024.01 신규 로그인 방식 : 이메일 추가
)
