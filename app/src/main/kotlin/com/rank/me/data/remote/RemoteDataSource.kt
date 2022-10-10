package com.rank.me.data.remote

import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginRequest
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.dto.recipes.Recipes

/**
 * Created by Saurabh, 27th sept 2022
 */

internal interface RemoteDataSource {
    suspend fun requestRecipes(): Resource<Recipes>
    suspend fun doLogin(loginRequest: LoginRequest): Resource<LoginResponse>
}
