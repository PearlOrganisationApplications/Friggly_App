package com.rank.me.ui.component.onboarding

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rank.me.R
import com.rank.me.REQUEST_CODE_SET_DEFAULT_DIALER
import com.rank.me.data.Resource
import com.rank.me.data.local.LocalData
import com.rank.me.databinding.ActivityOnboardingBinding
import com.rank.me.ui.base.BaseActivity
import com.rank.me.utils.LocaleUtil
import com.rank.me.utils.LocaleUtil.Companion.supportedLocales
import com.rank.me.utils.LocaleUtil.Companion.supportedLocalesFullName
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import kotlin.math.abs

class OnBoardingActivity : BaseActivity() {
    private lateinit var localRepository: LocalData
    private lateinit var binding: ActivityOnboardingBinding
    private val valueAnimator = ValueAnimator.ofObject(
        ArgbEvaluator(),
        Color.parseColor("#6680cbc4"),
        Color.parseColor("#669A80CB"),
        Color.parseColor("#66CB8080")
    )


    override fun observeViewModel() {

    }

    override fun initViewBinding() {
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localRepository = LocalData(this@OnBoardingActivity)
        binding.btnCreateAccount.setOnClickListener {
            launchSetDefaultDialerIntent()
        }

        binding.viewPager.adapter =
            OnboardingViewPagerAdapter(this@OnBoardingActivity, this@OnBoardingActivity)
        //set  page Indicator
        binding.pageIndicator.apply {
            setSliderWidth(50F)
            setSliderHeight(10F)
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setPageSize(binding.viewPager.adapter!!.itemCount)
            notifyDataChanged()
        }
        //change animation based on viewpager
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                //set page Indicator current page
                binding.pageIndicator.onPageSelected(position)
                //set animation current minmax Frame (DO NOT CHANGE MinAndMaxFrame)
                when (position) {
                    0 -> {
                        binding.imageOnboarding.setMinAndMaxFrame(1, 162)
                        binding.imageOnboarding.playAnimation()
                    }
                    1 -> {
                        binding.imageOnboarding.setMinAndMaxFrame(112, 203)
                        binding.imageOnboarding.playAnimation()
                    }
                    else -> {
                        binding.imageOnboarding.setMinAndMaxFrame(201, 380)
                        binding.imageOnboarding.playAnimation()
                    }
                }
                super.onPageSelected(position)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding.pageIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
                valueAnimator.currentPlayTime = (((position + positionOffset) * 1000000000).toLong())
            }
        })
        valueAnimator.duration = (3-1)*1000000000
        valueAnimator.addUpdateListener { animator ->
            binding.rootView.setBackgroundColor(animator.animatedValue as Int)
        }

        binding.viewPager.setPageTransformer { page, position ->
            if (position >= -1.0f || position <= 1.0f) {
                val height = page.height.toFloat()
                val scaleFactor = 0.85f.coerceAtLeast(1.0f - abs(position))
                val vertMargin = height * (1.0f - scaleFactor) / 2.0f
                val horzMargin = page.width.toFloat() * (1.0f - scaleFactor) / 2.0f
                page.pivotY = 0.5f * height
                if (position < 0.0f) {
                    page.translationX = horzMargin - vertMargin / 2.0f
                } else {
                    page.translationX = -horzMargin + vertMargin / 2.0f
                }
                page.scaleX = scaleFactor
                page.alpha = 0.2f + (scaleFactor - 0.85f) / 0.14999998f * 0.7f
            }
        }
        if (localRepository.getPreferredLocale() == "en") {
            binding.changeLangHindi.text = getString(R.string.hindi)
            binding.changeLangHindi.setOnClickListener {
                updateAppLocale("hi")
            }
        } else {
            binding.changeLangHindi.text = getString(R.string.english)
            binding.changeLangHindi.setOnClickListener {
                updateAppLocale("en")
            }
        }
        binding.changeLangOptions.setOnClickListener {
            showRadioConfirmationDialog()
        }
    }

    fun launchSetDefaultDialerIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                    RoleManager.ROLE_DIALER
                )
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            ).apply {
                try {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: ActivityNotFoundException) {
                    Log.e("TAG", "launchSetDefaultDialerIntent: ActivityNotFoundException : $e")
                } catch (e: Exception) {
                    Log.e("TAG", "launchSetDefaultDialerIntent: Exception : $e")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // we dont really care about the result, the app can work without being the default Dialer too
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            PermissionsDialog().show(supportFragmentManager, "permissions")
            Toast.makeText(this@OnBoardingActivity, "test", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRadioConfirmationDialog() {
        var selectedlocale = supportedLocales[selectedLocaleIndex]
        MaterialAlertDialogBuilder(this@OnBoardingActivity)
            .setTitle("Set Language")
            .setSingleChoiceItems(supportedLocalesFullName, selectedLocaleIndex) { dialog_, which ->
                selectedLocaleIndex = which
                selectedlocale = supportedLocales[which]
            }
            .setPositiveButton("Ok") { dialog, _ ->
                updateAppLocale(selectedlocale)
                Toast.makeText(this, "$selectedlocale Selected", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateAppLocale(locale: String) {
        val changedLocale: Resource<Boolean> = localRepository.setPreferredLocale(locale)
        if (changedLocale.data == true) {
            LocaleUtil.applyLocalizedContext(applicationContext, locale)
            startActivity(Intent(this, OnBoardingActivity::class.java))
            finish()
        } else {
            Toast.makeText(
                applicationContext,
                "Error : ${changedLocale.errorCode}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
