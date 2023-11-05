package kr.ac.wku.albeapp.photos

import java.util.*

data class Photo(
    var description: String = "", // 사진 설명
    var imageUrl: String = "", // 사진이 저장된 경로
    var date: Date = Date(),
    var id: String? = null
)
