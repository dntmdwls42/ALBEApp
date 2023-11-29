package kr.ac.wku.albeapp.logins

// 유저의 상태 정보 클래스
enum class UserStatus(val status: Int, val description: String) {
    ACTIVE(1, "활성"),
    INACTIVE(0, "비활성"),
    TEMP_INACTIVE(2, "일시적 비활성"),
    NOTHING(3,"알수없음");

    companion object {
        fun fromStatus(status: Int): UserStatus {
            return values().firstOrNull { it.status == status } ?: NOTHING
        }
    }
}