package com.rank.me.data.remote

import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginRequest
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.dto.recipes.Recipes
import com.rank.me.data.dto.recipes.RecipesItem
import com.rank.me.data.error.NETWORK_ERROR
import com.rank.me.data.error.NO_INTERNET_CONNECTION
import com.rank.me.data.remote.service.LoginService
import com.rank.me.data.remote.service.RecipesService
import com.rank.me.utils.NetworkConnectivity
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject


/**
 * Created by Saurabh, 27th sept 2022
 */

class RemoteData @Inject
constructor(
    private val serviceGenerator: ServiceGenerator,
    private val networkConnectivity: NetworkConnectivity
) : RemoteDataSource {
    override suspend fun requestRecipes(): Resource<Recipes> {
        val recipesService = serviceGenerator.createService(RecipesService::class.java)
        return when (val response = processCall(recipesService::fetchRecipes)) {
            is List<*> -> {
                Resource.Success(data = Recipes(response as ArrayList<RecipesItem>))
            }
            else -> {
                Resource.DataError(errorCode = response as Int)
            }
        }
    }

    override suspend fun doLogin(loginRequest: LoginRequest): Resource<LoginResponse> {
        val loginService = serviceGenerator.createService(LoginService::class.java)
        return when (val response = processCall(loginService::doLogin)) {
            is LoginResponse -> {
                Resource.Success(data = response)
            }
            else -> {
                Resource.DataError(errorCode = response as Int)
            }
        }
    }

    private suspend fun processCall(responseCall: suspend () -> Response<*>): Any? {
        if (!networkConnectivity.isConnected()) {
            return NO_INTERNET_CONNECTION
        }
        return try {
            val response = responseCall.invoke()
            val responseCode = response.code()
            if (response.isSuccessful) {
                response.body()
            } else {
                responseCode
            }
        } catch (e: IOException) {
            NETWORK_ERROR
        }
    }
}
