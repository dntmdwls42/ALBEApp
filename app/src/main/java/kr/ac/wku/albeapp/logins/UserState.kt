package kr.ac.wku.albeapp.logins

import kr.ac.wku.albeapp.R

// 유저의 상태 정보 클래스
enum class UserState(val status: Int, val description: String,val imageResId: Int) {
    ACTIVE(1, "활성", R.drawable.check),
    INACTIVE(0, "비활성", R.drawable.noinfo),
    TEMP_INACTIVE(2, "일시적 비활성", R.drawable.away),
    NOTHING(3,"알수없음", R.drawable.nothing);

    companion object {
        fun fromStatus(status: Int): UserState {
            return values().firstOrNull { it.status == status } ?: NOTHING
        }

        fun fromDescription(description: String): UserState {
            return values().firstOrNull { it.description == description } ?: NOTHING
        }
    }
}