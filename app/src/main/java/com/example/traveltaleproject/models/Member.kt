package com.example.traveltaleproject.models

data class Member(
    val nickname: String?,
    val id: String?,
    val pw: String?,
    val email: String?,
    val phone: String?,
    val logintype: String?,
    val profileImage: String?
)
{
    constructor() : this(null, null, null, null, null,  null, null)
}
