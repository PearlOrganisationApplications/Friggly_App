package com.rank.me.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.rank.me.*
import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginRequest
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.dto.login.RegisterRequest
import com.rank.me.utils.LocaleUtil
import javax.inject.Inject


/**
 * Created by Saurabh, 27th sept 2022
 */

class LocalData @Inject constructor(val context: Context) {
    fun doLogin(loginRequest: LoginRequest): Resource<LoginResponse> {
        val resp = LoginResponse(
            "123", "TestName", "TestLastName",
            "TestStreet", "77", "248001", "Uttarakhand",
            "India", "test@test.com", "123456", isNewUser = false
        )
        this.saveUser(resp)
        return if (loginRequest == LoginRequest("91", "1234567890")) {
            Resource.Success(resp.apply { isNewUser = true })
        } else {
            Resource.Success(resp.apply { isNewUser = false })
        }
    }

    fun doRegister(registerRequest: RegisterRequest): Resource<LoginResponse> {
        return if (registerRequest == RegisterRequest("91", "1111100000", "test@test.com","1","Male", LoginResponse("", "", "", "", "", "", "", "91", "1234567890", "123456", isNewUser = true))
        ) {
            Resource.Success(
                LoginResponse(
                    "123", "TestName", "TestLastName",
                    "TestStreet", "77", "248001", "Uttarakhand",
                    "India", "test@test.com"
                )
            )
        } else {
            Resource.Success(
                LoginResponse(
                    "123", "TestName", "TestLastName",
                    "TestStreet", "77", "248001", "Uttarakhand",
                    "India", "test@test.com"
                )
            )
//            Resource.DataError(PASS_WORD_ERROR)
        }
    }

    fun getCachedFavourites(): Resource<Set<String>> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        return Resource.Success(sharedPref.getStringSet(FAVOURITES_KEY, setOf()) ?: setOf())
    }

    fun cacheFavourites(ids: Set<String>): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putStringSet(FAVOURITES_KEY, ids)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
    }

    fun isFavourite(id: String): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val cache = sharedPref.getStringSet(FAVOURITES_KEY, setOf<String>()) ?: setOf()
        return Resource.Success(cache.contains(id))
    }

    fun removeFromFavourites(id: String): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        var set = sharedPref.getStringSet(FAVOURITES_KEY, mutableSetOf<String>())?.toMutableSet()
            ?: mutableSetOf()
        if (set.contains(id)) {
            set.remove(id)
        }
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
        editor.commit()
        editor.putStringSet(FAVOURITES_KEY, set)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
    }

    fun getPreferredLocale(): String {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        return sharedPref.getString(PREFERRED_LOCALE_KEY, LocaleUtil.OPTION_PHONE_LANGUAGE)!!
    }

    fun setPreferredLocale(localeCode: String): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(PREFERRED_LOCALE_KEY, localeCode)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
    }

    fun isFirstTime(): Boolean {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        return sharedPref.getBoolean(IS_FIRST_TIME, true)
    }

    fun setFirstTime(isFirstTime: Boolean): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(IS_FIRST_TIME, isFirstTime)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
    }

    fun isLogin(): Boolean {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        return sharedPref.getBoolean(IS_LOGIN, false)
    }

    fun setIsLogin(isFirstTime: Boolean): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(IS_LOGIN, isFirstTime)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
    }

    fun saveUser(user: LoginResponse) {
        val editor: SharedPreferences.Editor  = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0).edit()
        editor.putString(USER_INFO, Gson().toJson(user)).apply()
    }

    fun getUser() : Resource<LoginResponse> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        return Resource.Success(Gson().fromJson(sharedPref.getString(USER_INFO, ""), LoginResponse :: class.java))
    }
}

