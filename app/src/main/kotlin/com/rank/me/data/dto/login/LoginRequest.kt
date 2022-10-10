package com.rank.me.data.dto.login

import com.squareup.moshi.Json

/**
 * Created by Saurabh, 27th sept 2022
 */
data class LoginRequest(
    @Json(name = "country_code")
    val country_code: String,
    @Json(name = "number")
    val number: String
)

data class RegisterRequest(val firstName: String, val lastName: String, val email: String,val age: String, val gender: String, val loginResponse: LoginResponse? = null)
