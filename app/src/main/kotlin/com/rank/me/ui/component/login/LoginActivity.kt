package com.rank.me.ui.component.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import com.google.android.material.snackbar.Snackbar
import com.hbb20.countrypicker.dialog.launchCountryPickerDialog
import com.rank.me.R
import com.rank.me.data.Resource
import com.rank.me.data.dto.login.LoginResponse
import com.rank.me.data.local.LocalData
import com.rank.me.databinding.LoginActivityBinding
import com.rank.me.ui.base.BaseActivity
import com.rank.me.ui.component.home.HomeActivity
import com.rank.me.ui.component.register.RegisterActivity
import com.rank.me.utils.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Saurabh, 27th sept 2022
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity() {


    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: LoginActivityBinding
    private var selectedCountry: String = "91"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.login.setOnClickListener { showPrivacy() }
        binding.username.setOnClickListener {
            launchCountryPickerDialog() { selectedCountry ->
                this.selectedCountry = selectedCountry?.phoneCode.toString()
                binding.username.setText("+${selectedCountry?.phoneCode} (${selectedCountry?.englishName})")
            }
        }
    }

    private fun showPrivacy() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, ConsentFragment.newInstance())
            .commitAllowingStateLoss()
    }

    override fun initViewBinding() {
        binding = LoginActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun observeViewModel() {
        observe(loginViewModel.loginLiveData, ::handleLoginResult)
        observeSnackBarMessages(loginViewModel.showSnackBar)
        observeToast(loginViewModel.showToast)
    }

    fun doLogin() {
        Log.e(
            "doLogin",
            "doLogin: \"$selectedCountry + ${
                binding.password.text.trim()
            }\"",
        )
        loginViewModel.doLogin(
            selectedCountry,
            binding.password.text.trim().toString()
        )
    }

    private fun handleLoginResult(status: Resource<LoginResponse>) {
        when (status) {
            is Resource.Loading -> binding.loaderView.toVisible()
            is Resource.Success -> status.data?.let {
                if (it.isNewUser == true) {
                    binding.loaderView.toGone()
                    navigateToMainScreen()
                } else {
                    binding.loaderView.toGone()
                    navigateToRegisterScreen(it)
                }
            }
            is Resource.DataError -> {
                binding.loaderView.toGone()
                status.errorCode?.let { loginViewModel.showToastMessage(it) }
            }
        }
    }

    private fun navigateToMainScreen() {
        LocalData(this@LoginActivity).setIsLogin(true)
        val nextScreenIntent = Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(nextScreenIntent)
        finish()
    }

    private fun navigateToRegisterScreen(loginResponse: LoginResponse) {
        val nextScreenIntent = Intent(this, RegisterActivity::class.java)
        nextScreenIntent.putExtra("data", loginResponse)
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
