package com.pearltools.commons.views

import android.content.Context
import android.util.AttributeSet
import androidx.biometric.auth.AuthPromptHost
import androidx.constraintlayout.widget.ConstraintLayout
import com.pearltools.commons.extensions.*
import com.pearltools.commons.helpers.DARK_GREY
import com.pearltools.commons.interfaces.HashListener
import com.pearltools.commons.interfaces.SecurityTab
import kotlinx.android.synthetic.main.tab_biometric_id.view.*

class BiometricIdTab(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), SecurityTab {
    private lateinit var hashListener: HashListener
    private lateinit var biometricPromptHost: AuthPromptHost

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(biometric_lock_holder)
        val textColor = if (context.isWhiteTheme()) {
            DARK_GREY
        } else {
            context.getProperPrimaryColor().getContrastColor()
        }

        open_biometric_dialog.setTextColor(textColor)
        open_biometric_dialog.setOnClickListener {
            biometricPromptHost.activity?.showBiometricPrompt(successCallback = hashListener::receivedHash)
        }
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.biometricPromptHost = biometricPromptHost
        hashListener = listener
        if (showBiometricAuthentication) {
            open_biometric_dialog.performClick()
        }
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}
