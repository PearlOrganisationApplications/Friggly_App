package com.rank.me.ui.component.register

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rank.me.data.DataRepository
import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.dto.login.RegisterRequest
import com.rank.me.data.error.EMAIL_ERROR
import com.rank.me.data.error.FIRST_NAME_ERROR
import com.rank.me.data.error.LAST_NAME_ERROR
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
class RegisterViewModel @Inject constructor(private val dataRepository: DataRepository) :
    BaseViewModel() {

    // Data fields for Registration
    private val _firstName = MutableLiveData<String>()
    val manualFirstName: LiveData<String> = _firstName

    private val _lastName = MutableLiveData<String>()
    val manualLastName: LiveData<String> = _lastName

    private val _email = MutableLiveData<String>()
    val manualEmail: LiveData<String> = _email

    private val _age = MutableLiveData<String>()
    val manualAge: LiveData<String> = _age

    private val _gender = MutableLiveData<String>()
    val manualGender: LiveData<String> = _gender
    // Data fields for Registration

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


    fun doRegister(firstName: String, lastName: String, email: String, age: String, gender: String, loginResponse: LoginResponse? = null) {
        val isFirstnameValid = firstName.trim().isNotEmpty()
        val isLastnameValid = lastName.trim().isNotEmpty()
        val isemailValid = email.trim().isNotEmpty()
        if (isFirstnameValid && !isemailValid && isLastnameValid ) {
            loginLiveDataPrivate.value = Resource.DataError(EMAIL_ERROR)
        } else if (!isFirstnameValid && isemailValid && isLastnameValid ) {
            loginLiveDataPrivate.value = Resource.DataError(FIRST_NAME_ERROR)
        } else if (isFirstnameValid && !isLastnameValid && isemailValid) {
            loginLiveDataPrivate.value = Resource.DataError(LAST_NAME_ERROR)
        } else {
            viewModelScope.launch {
                loginLiveDataPrivate.value = Resource.Loading()
                wrapEspressoIdlingResource {
                    dataRepository.doRegister(registerRequest = RegisterRequest(firstName, lastName, email, age, gender, loginResponse))
                        .collect {
                            loginLiveDataPrivate.value = it
                        }
                }
            }
        }
    }

    fun setRegisterData(
        firstName: String,
        lastName: String,
        email: String,
        age: String,
        gender: String
    ) {
        _firstName.value = firstName
        _lastName.value = lastName
        _email.value = email
        _age.value = age
        _gender.value = gender
    }

    fun showToastMessage(errorCode: Int) {
        val error = errorManager.getError(errorCode)
        showToastPrivate.value = SingleEvent(error.description)
    }
}
