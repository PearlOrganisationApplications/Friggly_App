package com.rank.me.message.helpers

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.helpers.BaseConfig
import com.rank.me.message.models.Conversation
import com.rank.me.ui.component.home.call.helpers.*
import com.simplemobiletools.dialer.models.SpeedDial

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    fun saveUseSIMIdAtNumber(number: String, SIMId: Int) {
        prefs.edit().putInt(USE_SIM_ID_PREFIX + number, SIMId).apply()
    }

    fun getUseSIMIdAtNumber(number: String) = prefs.getInt(USE_SIM_ID_PREFIX + number, 0)

    var showCharacterCounter: Boolean
        get() = prefs.getBoolean(SHOW_CHARACTER_COUNTER, false)
        set(showCharacterCounter) = prefs.edit().putBoolean(SHOW_CHARACTER_COUNTER, showCharacterCounter).apply()

    var useSimpleCharacters: Boolean
        get() = prefs.getBoolean(USE_SIMPLE_CHARACTERS, false)
        set(useSimpleCharacters) = prefs.edit().putBoolean(USE_SIMPLE_CHARACTERS, useSimpleCharacters).apply()

    var enableDeliveryReports: Boolean
        get() = prefs.getBoolean(ENABLE_DELIVERY_REPORTS, false)
        set(enableDeliveryReports) = prefs.edit().putBoolean(ENABLE_DELIVERY_REPORTS, enableDeliveryReports).apply()

    var sendLongMessageMMS: Boolean
        get() = prefs.getBoolean(SEND_LONG_MESSAGE_MMS, false)
        set(sendLongMessageMMS) = prefs.edit().putBoolean(SEND_LONG_MESSAGE_MMS, sendLongMessageMMS).apply()

    var sendGroupMessageMMS: Boolean
        get() = prefs.getBoolean(SEND_GROUP_MESSAGE_MMS, false)
        set(sendGroupMessageMMS) = prefs.edit().putBoolean(SEND_GROUP_MESSAGE_MMS, sendGroupMessageMMS).apply()

    var lockScreenVisibilitySetting: Int
        get() = prefs.getInt(LOCK_SCREEN_VISIBILITY, LOCK_SCREEN_SENDER_MESSAGE)
        set(lockScreenVisibilitySetting) = prefs.edit().putInt(LOCK_SCREEN_VISIBILITY, lockScreenVisibilitySetting).apply()

    var mmsFileSizeLimit: Long
        get() = prefs.getLong(MMS_FILE_SIZE_LIMIT, FILE_SIZE_600_KB)
        set(mmsFileSizeLimit) = prefs.edit().putLong(MMS_FILE_SIZE_LIMIT, mmsFileSizeLimit).apply()

    var pinnedConversations: Set<String>
        get() = prefs.getStringSet(PINNED_CONVERSATIONS, HashSet<String>())!!
        set(pinnedConversations) = prefs.edit().putStringSet(PINNED_CONVERSATIONS, pinnedConversations).apply()

    fun addPinnedConversationByThreadId(threadId: Long) {
        pinnedConversations = pinnedConversations.plus(threadId.toString())
    }

    fun addPinnedConversations(conversations: List<Conversation>) {
        pinnedConversations = pinnedConversations.plus(conversations.map { it.threadId.toString() })
    }

    fun removePinnedConversationByThreadId(threadId: Long) {
        pinnedConversations = pinnedConversations.minus(threadId.toString())
    }

    fun removePinnedConversations(conversations: List<Conversation>) {
        pinnedConversations = pinnedConversations.minus(conversations.map { it.threadId.toString() })
    }

    var lastExportPath: String
        get() = prefs.getString(LAST_EXPORT_PATH, "")!!
        set(lastExportPath) = prefs.edit().putString(LAST_EXPORT_PATH, lastExportPath).apply()

    var exportSms: Boolean
        get() = prefs.getBoolean(EXPORT_SMS, true)
        set(exportSms) = prefs.edit().putBoolean(EXPORT_SMS, exportSms).apply()

    var exportMms: Boolean
        get() = prefs.getBoolean(EXPORT_MMS, true)
        set(exportMms) = prefs.edit().putBoolean(EXPORT_MMS, exportMms).apply()

    var importSms: Boolean
        get() = prefs.getBoolean(IMPORT_SMS, true)
        set(importSms) = prefs.edit().putBoolean(IMPORT_SMS, importSms).apply()

    var importMms: Boolean
        get() = prefs.getBoolean(IMPORT_MMS, true)
        set(importMms) = prefs.edit().putBoolean(IMPORT_MMS, importMms).apply()

    var wasDbCleared: Boolean
        get() = prefs.getBoolean(WAS_DB_CLEARED, false)
        set(wasDbCleared) = prefs.edit().putBoolean(WAS_DB_CLEARED, wasDbCleared).apply()

    /////////////CALL CONFIG///////////////

    var speedDial: String
        get() = prefs.getString(SPEED_DIAL, "")!!
        set(speedDial) = prefs.edit().putString(SPEED_DIAL, speedDial).apply()

    fun getSpeedDialValues(): ArrayList<SpeedDial> {
        val speedDialType = object : TypeToken<List<SpeedDial>>() {}.type
        val speedDialValues = Gson().fromJson<ArrayList<SpeedDial>>(speedDial, speedDialType) ?: ArrayList(1)

        for (i in 1..9) {
            val speedDial = SpeedDial(i, "", "")
            if (speedDialValues.firstOrNull { it.id == i } == null) {
                speedDialValues.add(speedDial)
            }
        }

        return speedDialValues
    }

    fun saveCustomSIM(number: String, SIMlabel: String) {
        prefs.edit().putString(REMEMBER_SIM_PREFIX + number, Uri.encode(SIMlabel)).apply()
    }

    fun getCustomSIM(number: String) = prefs.getString(REMEMBER_SIM_PREFIX + number, "")

    fun removeCustomSIM(number: String) {
        prefs.edit().remove(REMEMBER_SIM_PREFIX + number).apply()
    }

    var groupSubsequentCalls: Boolean
        get() = prefs.getBoolean(GROUP_SUBSEQUENT_CALLS, true)
        set(groupSubsequentCalls) = prefs.edit().putBoolean(GROUP_SUBSEQUENT_CALLS, groupSubsequentCalls).apply()

    var openDialPadAtLaunch: Boolean
        get() = prefs.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH, false)
        set(openDialPad) = prefs.edit().putBoolean(OPEN_DIAL_PAD_AT_LAUNCH, openDialPad).apply()

    var disableProximitySensor: Boolean
        get() = prefs.getBoolean(DISABLE_PROXIMITY_SENSOR, false)
        set(disableProximitySensor) = prefs.edit().putBoolean(DISABLE_PROXIMITY_SENSOR, disableProximitySensor).apply()

    var disableSwipeToAnswer: Boolean
        get() = prefs.getBoolean(DISABLE_SWIPE_TO_ANSWER, false)
        set(disableSwipeToAnswer) = prefs.edit().putBoolean(DISABLE_SWIPE_TO_ANSWER, disableSwipeToAnswer).apply()

    var showTabs: Int
        get() = prefs.getInt(SHOW_TABS, ALL_TABS_MASK)
        set(showTabs) = prefs.edit().putInt(SHOW_TABS, showTabs).apply()

    var favoritesContactsOrder: String
        get() = prefs.getString(FAVORITES_CONTACTS_ORDER, "")!!
        set(order) = prefs.edit().putString(FAVORITES_CONTACTS_ORDER, order).apply()

    var isCustomOrderSelected: Boolean
        get() = prefs.getBoolean(FAVORITES_CUSTOM_ORDER_SELECTED, false)
        set(selected) = prefs.edit().putBoolean(FAVORITES_CUSTOM_ORDER_SELECTED, selected).apply()

    var wasOverlaySnackbarConfirmed: Boolean
        get() = prefs.getBoolean(WAS_OVERLAY_SNACKBAR_CONFIRMED, false)
        set(wasOverlaySnackbarConfirmed) = prefs.edit().putBoolean(WAS_OVERLAY_SNACKBAR_CONFIRMED, wasOverlaySnackbarConfirmed).apply()

    var dialpadVibration: Boolean
        get() = prefs.getBoolean(DIALPAD_VIBRATION, true)
        set(dialpadVibration) = prefs.edit().putBoolean(DIALPAD_VIBRATION, dialpadVibration).apply()

    var dialpadBeeps: Boolean
        get() = prefs.getBoolean(DIALPAD_BEEPS, true)
        set(dialpadBeeps) = prefs.edit().putBoolean(DIALPAD_BEEPS, dialpadBeeps).apply()

    var alwaysShowFullscreen: Boolean
        get() = prefs.getBoolean(ALWAYS_SHOW_FULLSCREEN, false)
        set(alwaysShowFullscreen) = prefs.edit().putBoolean(ALWAYS_SHOW_FULLSCREEN, alwaysShowFullscreen).apply()
    /////////////CALL CONFIG///////////////
}
