package com.example.traveltaleproject.user

data class Member(
    val nickname: String?,
    val id: String?,
    val pw: String?,
    val email: String?,
    val phone: String?,
    val logintype: String?
)
{
    constructor() : this(null, null, null, null, null,  null)
}
