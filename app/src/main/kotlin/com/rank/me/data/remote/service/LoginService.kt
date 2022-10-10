package com.rank.me.data.remote.service

import com.rank.me.data.dto.login.LoginResponse
import retrofit2.Response
import retrofit2.http.POST

/**
 * Created by Saurabh, 27th sept 2022
 */

interface LoginService {
    @POST("login.php")
    suspend fun doLogin(): Response<LoginResponse>
}
