package com.rank.me.ui.component.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import com.rank.me.R
import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.error.SEARCH_ERROR
import com.rank.me.databinding.RegisterActivityBinding
import com.rank.me.ui.base.BaseActivity
import com.rank.me.ui.component.home.HomeActivity
import com.rank.me.utils.SingleEvent
import com.rank.me.utils.observe
import com.rank.me.utils.setupSnackbar
import com.rank.me.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Saurabh, 27th sept 2022
 */
@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    private val registerViewModel: RegisterViewModel by viewModels()
    private lateinit var binding: RegisterActivityBinding
    val loginResponse = intent?.getParcelableExtra<LoginResponse>("data")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.manualLogin.setOnClickListener { doManualRegister() }
        binding.googleLogin.setOnClickListener { doGoogleRegister() }
        binding.facebookLogin.setOnClickListener { doFacebookRegister() }
    }

    private fun doManualRegister() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, ManualRegisterFragment.newInstance())
            .commitAllowingStateLoss()
    }

    private fun doFacebookRegister() {

    }

    private fun doGoogleRegister() {

    }

    override fun initViewBinding() {
        binding = RegisterActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun observeViewModel() {
        // registration result
        observe(registerViewModel.loginLiveData, ::handleLoginResult)
        // UI
        observeSnackBarMessages(registerViewModel.showSnackBar)
        observeToast(registerViewModel.showToast)
    }

    fun startRegister() {
        Log.e("TAG", "startRegister: ")
        registerViewModel.doRegister(
            registerViewModel.manualFirstName.value.toString(),
            registerViewModel.manualLastName.value.toString(),
            registerViewModel.manualEmail.value.toString(),
            registerViewModel.manualAge.value.toString(),
            registerViewModel.manualGender.value.toString(),
            loginResponse
        )
    }

    private fun handleLoginResult(status: Resource<LoginResponse>) {
        Log.e("TAG", "handleLoginResult: ${status.data}")
        when (status) {
//            is Resource.Loading -> binding.loaderView.toVisible()
            is Resource.Success -> status.data?.let {
//                binding.loaderView.toGone()
                navigateToMainScreen()
            }
            is Resource.DataError -> {
//                binding.loaderView.toGone()
                status.errorCode?.let { registerViewModel.showToastMessage(it) }
            }
            else -> {
               let { registerViewModel.showToastMessage(SEARCH_ERROR)}
            }
        }
    }

    private fun navigateToMainScreen() {
        val nextScreenIntent = Intent(this, HomeActivity::class.java)
        startActivity(nextScreenIntent)
        finish()
    }

    private fun observeSnackBarMessages(event: LiveData<SingleEvent<Any>>) {
        binding.root.setupSnackbar(this, event, Snackbar.LENGTH_LONG)
    }

    private fun observeToast(event: LiveData<SingleEvent<Any>>) {
        binding.root.showToast(this, event, Snackbar.LENGTH_LONG)
    }
}
