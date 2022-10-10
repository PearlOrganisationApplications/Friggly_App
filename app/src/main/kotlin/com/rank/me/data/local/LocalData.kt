package com.rank.me.data.local

import android.content.Context
import android.content.SharedPreferences
import com.rank.*
import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginRequest
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.dto.login.RegisterRequest
import com.rank.me.*
import com.rank.me.utils.LocaleUtil
import javax.inject.Inject

/**
 * Created by Saurabh, 27th sept 2022
 */

class LocalData @Inject constructor(val context: Context) {
    fun doLogin(loginRequest: LoginRequest): Resource<LoginResponse> {
        if (loginRequest == LoginRequest("91", "1234567890")) {
            return Resource.Success(
                LoginResponse(
                    "", "", "",
                    "", "", "", "",
                    loginRequest.country_code, loginRequest.number, "123456", isNewUser = true
                )
            )
        } else {
            return Resource.Success(
                LoginResponse(
                    "123", "TestName", "TestLastName",
                    "TestStreet", "77", "248001", "Uttarakhand",
                    "India", "test@test.com", "123456", isNewUser = false
                )
            )
        }
//        return Resource.DataError(PASS_WORD_ERROR)
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

    fun isFavourite(id: String): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val cache = sharedPref.getStringSet(FAVOURITES_KEY, setOf<String>()) ?: setOf()
        return Resource.Success(cache.contains(id))
    }

    fun cacheFavourites(ids: Set<String>): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putStringSet(FAVOURITES_KEY, ids)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
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
}

