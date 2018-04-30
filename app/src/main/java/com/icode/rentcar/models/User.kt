package com.icode.rentcar.models

import com.icode.rentcar.USER_TYPE_CLIENT

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val type: Int = USER_TYPE_CLIENT
)
