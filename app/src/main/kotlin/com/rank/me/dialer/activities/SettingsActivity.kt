package com.rank.me.dialer.activities

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import com.rank.me.R
import com.rank.me.dialer.dialogs.ManageVisibleTabsDialog
import com.rank.me.extensions.config
import com.rank.me.ui.base.SimpleActivity
import com.simplemobiletools.commons.activities.ManageBlockedNumbersActivity
import com.simplemobiletools.commons.dialogs.ChangeDateTimeFormatDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.activity_call_settings.*
import java.util.*
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_settings)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(settings_toolbar, NavigationIcon.Arrow)

        setupPurchaseThankYou()
        setupCustomizeColors()
        setupUseEnglish()
        setupLanguage()
        setupManageBlockedNumbers()
        setupManageSpeedDial()
        setupChangeDateTimeFormat()
        setupFontSize()
        setupManageShownTabs()
        setupDefaultTab()
        setupDialPadOpen()
        setupGroupSubsequentCalls()
        setupStartNameWithSurname()
        setupDialpadVibrations()
        setupDialpadBeeps()
        setupShowCallConfirmation()
        setupDisableProximitySensor()
        setupDisableSwipeToAnswer()
        setupAlwaysShowFullscreen()
        updateTextColors(settings_holder)

        arrayOf(
            settings_color_customization_label,
            settings_general_settings_label,
            settings_startup_label,
            settings_calls_label
        ).forEach {
            it.setTextColor(getProperPrimaryColor())
        }

        arrayOf(
            settings_color_customization_holder,
            settings_general_settings_holder,
            settings_startup_holder,
            settings_calls_holder
        ).forEach {
            it.background.applyColorFilter(getProperBackgroundColor().getContrastColor())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupPurchaseThankYou() {
        settings_purchase_thank_you_holder.beGoneIf(isOrWasThankYouInstalled())

        // make sure the corners at ripple fit the stroke rounded corners
        if (settings_purchase_thank_you_holder.isGone()) {
            settings_use_english_holder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
            settings_manage_blocked_numbers_holder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        settings_purchase_thank_you_holder.setOnClickListener {
            launchPurchaseThankYouIntent()
        }
    }

    private fun setupCustomizeColors() {
        settings_customize_colors_label.text = getCustomizeColorsString()
        settings_customize_colors_holder.setOnClickListener {
            handleCustomizeColorsClick()
        }
    }

    private fun setupUseEnglish() {
        settings_use_english_holder.beVisibleIf((config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus())
        settings_use_english.isChecked = config.useEnglish
        settings_use_english_holder.setOnClickListener {
            settings_use_english.toggle()
            config.useEnglish = settings_use_english.isChecked
            exitProcess(0)
        }
    }

    private fun setupLanguage() {
        settings_language.text = Locale.getDefault().displayLanguage
        settings_language_holder.beVisibleIf(isTiramisuPlus())

        if (settings_use_english_holder.isGone() && settings_language_holder.isGone() && settings_purchase_thank_you_holder.isGone()) {
            settings_manage_blocked_numbers_holder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        settings_language_holder.setOnClickListener {
            launchChangeAppLanguageIntent()
        }
    }

    // support for device-wise blocking came on Android 7, rely only on that
    @TargetApi(Build.VERSION_CODES.N)
    private fun setupManageBlockedNumbers() {
        settings_manage_blocked_numbers_holder.beVisibleIf(isNougatPlus())

        if (settings_use_english_holder.isGone() && settings_purchase_thank_you_holder.isGone() && settings_manage_blocked_numbers_holder.isGone()) {
            settings_change_date_time_format_holder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        settings_manage_blocked_numbers_holder.setOnClickListener {
            Intent(this, ManageBlockedNumbersActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupManageSpeedDial() {
        settings_manage_speed_dial_holder.setOnClickListener {
            Intent(this, ManageSpeedDialActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setupChangeDateTimeFormat() {
        settings_change_date_time_format_holder.setOnClickListener {
            ChangeDateTimeFormatDialog(this) {}
        }
    }

    private fun setupFontSize() {
        settings_font_size.text = getFontSizeText()
        settings_font_size_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(FONT_SIZE_SMALL, getString(R.string.small)),
                RadioItem(FONT_SIZE_MEDIUM, getString(R.string.medium)),
                RadioItem(FONT_SIZE_LARGE, getString(R.string.large)),
                RadioItem(FONT_SIZE_EXTRA_LARGE, getString(R.string.extra_large))
            )

            RadioGroupDialog(this@SettingsActivity, items, config.fontSize) {
                config.fontSize = it as Int
                settings_font_size.text = getFontSizeText()
            }
        }
    }

    private fun setupManageShownTabs() {
        settings_manage_tabs_holder.setOnClickListener {
            ManageVisibleTabsDialog(this)
        }
    }

    private fun setupDefaultTab() {
        settings_default_tab.text = getDefaultTabText()
        settings_default_tab_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(TAB_CONTACTS, getString(R.string.contacts_tab)),
                RadioItem(TAB_FAVORITES, getString(R.string.favorites_tab)),
                RadioItem(TAB_CALL_HISTORY, getString(R.string.call_history_tab)),
                RadioItem(TAB_LAST_USED, getString(R.string.last_used_tab))
            )

            RadioGroupDialog(this@SettingsActivity, items, config.defaultTab) {
                config.defaultTab = it as Int
                settings_default_tab.text = getDefaultTabText()
            }
        }
    }

    private fun getDefaultTabText() = getString(
        when (baseConfig.defaultTab) {
            TAB_CONTACTS -> R.string.contacts_tab
            TAB_FAVORITES -> R.string.favorites_tab
            TAB_CALL_HISTORY -> R.string.call_history_tab
            else -> R.string.last_used_tab
        }
    )

    private fun setupDialPadOpen() {
        settings_open_dialpad_at_launch.isChecked = config.openDialPadAtLaunch
        settings_open_dialpad_at_launch_holder.setOnClickListener {
            settings_open_dialpad_at_launch.toggle()
            config.openDialPadAtLaunch = settings_open_dialpad_at_launch.isChecked
        }
    }

    private fun setupGroupSubsequentCalls() {
        settings_group_subsequent_calls.isChecked = config.groupSubsequentCalls
        settings_group_subsequent_calls_holder.setOnClickListener {
            settings_group_subsequent_calls.toggle()
            config.groupSubsequentCalls = settings_group_subsequent_calls.isChecked
        }
    }

    private fun setupStartNameWithSurname() {
        settings_start_name_with_surname.isChecked = config.startNameWithSurname
        settings_start_name_with_surname_holder.setOnClickListener {
            settings_start_name_with_surname.toggle()
            config.startNameWithSurname = settings_start_name_with_surname.isChecked
        }
    }

    private fun setupDialpadVibrations() {
        settings_dialpad_vibration.isChecked = config.dialpadVibration
        settings_dialpad_vibration_holder.setOnClickListener {
            settings_dialpad_vibration.toggle()
            config.dialpadVibration = settings_dialpad_vibration.isChecked
        }
    }

    private fun setupDialpadBeeps() {
        settings_dialpad_beeps.isChecked = config.dialpadBeeps
        settings_dialpad_beeps_holder.setOnClickListener {
            settings_dialpad_beeps.toggle()
            config.dialpadBeeps = settings_dialpad_beeps.isChecked
        }
    }

    private fun setupShowCallConfirmation() {
        settings_show_call_confirmation.isChecked = config.showCallConfirmation
        settings_show_call_confirmation_holder.setOnClickListener {
            settings_show_call_confirmation.toggle()
            config.showCallConfirmation = settings_show_call_confirmation.isChecked
        }
    }

    private fun setupDisableProximitySensor() {
        settings_disable_proximity_sensor.isChecked = config.disableProximitySensor
        settings_disable_proximity_sensor_holder.setOnClickListener {
            settings_disable_proximity_sensor.toggle()
            config.disableProximitySensor = settings_disable_proximity_sensor.isChecked
        }
    }

    private fun setupDisableSwipeToAnswer() {
        settings_disable_swipe_to_answer.isChecked = config.disableSwipeToAnswer
        settings_disable_swipe_to_answer_holder.setOnClickListener {
            settings_disable_swipe_to_answer.toggle()
            config.disableSwipeToAnswer = settings_disable_swipe_to_answer.isChecked
        }
    }

    private fun setupAlwaysShowFullscreen() {
        settings_always_show_fullscreen.isChecked = config.alwaysShowFullscreen
        settings_always_show_fullscreen_holder.setOnClickListener {
            settings_always_show_fullscreen.toggle()
            config.alwaysShowFullscreen = settings_always_show_fullscreen.isChecked
        }
    }
}
