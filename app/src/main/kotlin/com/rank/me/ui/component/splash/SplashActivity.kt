package com.rank.me.ui.component.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.rank.me.SPLASH_DELAY
import com.rank.me.data.local.LocalData
import com.rank.me.databinding.SplashLayoutBinding
import com.rank.me.ui.base.BaseActivity
import com.rank.me.ui.component.home.HomeActivity
import com.rank.me.ui.component.login.LoginActivity
import com.rank.me.ui.component.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Saurabh, 27 sept 2022
 */
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: SplashLayoutBinding

    override fun initViewBinding() {
        binding = SplashLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        if (LocalData(this@SplashActivity).isFirstTime()) {
            navigateToOnBoardingScreen()
        } else {
            navigateToMainScreen()
        }
    }

    override fun observeViewModel() {

    }

    private fun navigateToMainScreen() {
        var nextScreenIntent = Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (!LocalData(this@SplashActivity).isLogin())
            nextScreenIntent = Intent(this, LoginActivity::class.java)
        Handler().postDelayed({
            startActivity(nextScreenIntent)
            finish()
        }, SPLASH_DELAY.toLong())
    }

    private fun navigateToOnBoardingScreen() {
        Handler().postDelayed({
            val nextScreenIntent = Intent(this, OnBoardingActivity::class.java)
            startActivity(nextScreenIntent)
            finish()
        }, SPLASH_DELAY.toLong())
    }
}
