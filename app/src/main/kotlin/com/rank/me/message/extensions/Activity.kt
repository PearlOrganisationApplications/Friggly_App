package com.rank.me.message.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import com.rank.me.R

fun Activity.dialNumber(phoneNumber: String, callback: (() -> Unit)? = null) {
    hideKeyboard()
    Intent(Intent.ACTION_DIAL).apply {
        data = Uri.fromParts("tel", phoneNumber, null)

        try {
            startActivity(this)
            callback?.invoke()
        } catch (e: ActivityNotFoundException) {
            toast(R.string.no_app_found)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }
}
