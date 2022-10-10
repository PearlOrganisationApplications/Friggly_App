package com.rank.me.data

import com.rank.me.data.dto.login.LoginRequest
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.dto.login.RegisterRequest
import com.rank.me.data.dto.recipes.Recipes
import kotlinx.coroutines.flow.Flow

/**
 * Created by Saurabh, 27th sept 2022
 */

interface DataRepositorySource {
    suspend fun requestRecipes(): Flow<Resource<Recipes>>
    suspend fun doLogin(loginRequest: LoginRequest): Flow<Resource<LoginResponse>>
    suspend fun doRegister(loginRequest: RegisterRequest): Flow<Resource<LoginResponse>>
    suspend fun addToFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun removeFromFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun isFavourite(id: String): Flow<Resource<Boolean>>
}
