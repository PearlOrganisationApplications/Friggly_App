package com.rank.me.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.rank.me.R
import com.rank.me.dialer.dialogs.SelectSIMDialog
import com.rank.me.ui.base.SimpleActivity
import com.pearltools.commons.activities.BaseSimpleActivity
import com.pearltools.commons.extensions.*
import com.pearltools.commons.helpers.*
import com.pearltools.commons.models.SimpleContact

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

fun SimpleActivity.startCallIntent(recipient: String) {
    if (isDefaultDialer()) {
        getHandleToUse(null, recipient) { handle ->
            launchCallIntent(recipient, handle)
        }
    } else {
        launchCallIntent(recipient, null)
    }
}

fun SimpleActivity.launchCreateNewContactIntent() {
    Intent().apply {
        action = Intent.ACTION_INSERT
        data = ContactsContract.Contacts.CONTENT_URI
        launchActivityIntent(this)
    }
}

fun BaseSimpleActivity.callContactWithSim(recipient: String, useMainSIM: Boolean) {
    handlePermission(PERMISSION_READ_PHONE_STATE) {
        val wantedSimIndex = if (useMainSIM) 0 else 1
        val handle = getAvailableSIMCardLabels().sortedBy { it.id }.getOrNull(wantedSimIndex)?.handle
        launchCallIntent(recipient, handle)
    }
}

// handle private contacts differently, only Simple Contacts Pro can open them
fun Activity.startContactDetailsIntent(contact: SimpleContact) {
    val simpleContacts = "com.pearltools.contacts.pro"
    val simpleContactsDebug = "com.pearltools.contacts.pro.debug"
    if (contact.rawId > 1000000 && contact.contactId > 1000000 && contact.rawId == contact.contactId &&
        (isPackageInstalled(simpleContacts) || isPackageInstalled(simpleContactsDebug))
    ) {
        Intent().apply {
            action = Intent.ACTION_VIEW
            putExtra(CONTACT_ID, contact.rawId)
            putExtra(IS_PRIVATE, true)
            `package` = if (isPackageInstalled(simpleContacts)) simpleContacts else simpleContactsDebug
            setDataAndType(ContactsContract.Contacts.CONTENT_LOOKUP_URI, "vnd.android.cursor.dir/person")
            launchActivityIntent(this)
        }
    } else {
        ensureBackgroundThread {
            val lookupKey = SimpleContactsHelper(this).getContactLookupKey((contact).rawId.toString())
            val publicUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
            runOnUiThread {
                launchViewContactIntent(publicUri)
            }
        }
    }
}

// used at devices with multiple SIM cards
@SuppressLint("MissingPermission")
fun SimpleActivity.getHandleToUse(intent: Intent?, phoneNumber: String, callback: (handle: PhoneAccountHandle?) -> Unit) {
    handlePermission(PERMISSION_READ_PHONE_STATE) { it ->
        if (it) {
            val defaultHandle = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)
            when {
                intent?.hasExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE) == true -> callback(intent.getParcelableExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE)!!)
                config.getCustomSIM(phoneNumber)?.isNotEmpty() == true -> {
                    val storedLabel = Uri.decode(config.getCustomSIM(phoneNumber))
                    val availableSIMs = getAvailableSIMCardLabels()
                    val firstOrNull = availableSIMs.firstOrNull { it.label == storedLabel }?.handle ?: availableSIMs.first().handle
                    callback(firstOrNull)
                }
                defaultHandle != null -> callback(defaultHandle)
                else -> {
                    SelectSIMDialog(this, phoneNumber) { handle ->
                        callback(handle)
                    }
                }
            }
        }
    }
}

