package com.mygdx.primelogistics.android.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("user")
    val user: User
)
