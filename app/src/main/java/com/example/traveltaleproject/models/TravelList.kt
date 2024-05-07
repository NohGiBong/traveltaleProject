package com.example.traveltaleproject.models

data class TravelList(
    val travelListId: String = "",
    val title: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val address: String = "",
    var travelImage: String = ""
) {
    // 매개변수가 없는 생성자 추가
    constructor() : this("", "", "", "")
}