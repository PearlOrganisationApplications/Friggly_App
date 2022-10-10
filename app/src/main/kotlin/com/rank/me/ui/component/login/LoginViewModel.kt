package com.rank.me.ui.component.login

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rank.me.data.DataRepository
import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginRequest
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.ui.base.BaseViewModel
import com.rank.me.utils.SingleEvent
import com.rank.me.utils.wrapEspressoIdlingResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Saurabh, 27th sept 2022
 */
@HiltViewModel
class LoginViewModel @Inject constructor(private val dataRepository: DataRepository) :
    BaseViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val loginLiveDataPrivate = MutableLiveData<Resource<LoginResponse>>()
    val loginLiveData: LiveData<Resource<LoginResponse>> get() = loginLiveDataPrivate

    /** Error handling as UI **/

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val showSnackBarPrivate = MutableLiveData<SingleEvent<Any>>()
    val showSnackBar: LiveData<SingleEvent<Any>> get() = showSnackBarPrivate

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val showToastPrivate = MutableLiveData<SingleEvent<Any>>()
    val showToast: LiveData<SingleEvent<Any>> get() = showToastPrivate


    fun doLogin(userName: String, passWord: String) {
//        val isUsernameValid = isValidEmail(userName)
//        val isPassWordValid = passWord.trim().length > 4
//        if (isUsernameValid && !isPassWordValid) {
//            loginLiveDataPrivate.value = Resource.DataError(PASS_WORD_ERROR)
//        } else if (!isUsernameValid && isPassWordValid) {
//            loginLiveDataPrivate.value = Resource.DataError(USER_NAME_ERROR)
//        } else if (!isUsernameValid && !isPassWordValid) {
//            loginLiveDataPrivate.value = Resource.DataError(CHECK_YOUR_FIELDS)
//        } else {
        viewModelScope.launch {
            loginLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                dataRepository.doLogin(loginRequest = LoginRequest(userName, passWord)).collect {
                    loginLiveDataPrivate.value = it
                }
            }
//            }
        }
    }

    fun showToastMessage(errorCode: Int) {
        val error = errorManager.getError(errorCode)
        showToastPrivate.value = SingleEvent(error.description)
    }
}
