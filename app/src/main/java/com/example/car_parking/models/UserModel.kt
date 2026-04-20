package com.example.car_parking.models

data class UserModel(
    val fullName: String = "",
    val email: String = "",
    val phone: String? = null,
    val avatarUrl: String? = null,
    val userId: String = "",
    val memberSince: String? = null
)
