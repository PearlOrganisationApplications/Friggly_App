package com.rank.me.dialer.dialogs

import com.rank.me.R
import com.rank.me.dialer.helpers.ALL_TABS_MASK
import com.rank.me.extensions.config
import com.pearltools.commons.activities.BaseSimpleActivity
import com.pearltools.commons.extensions.getAlertDialogBuilder
import com.pearltools.commons.extensions.setupDialogStuff
import com.pearltools.commons.helpers.TAB_CALL_HISTORY
import com.pearltools.commons.helpers.TAB_CONTACTS
import com.pearltools.commons.helpers.TAB_FAVORITES
import com.pearltools.commons.views.MyAppCompatCheckbox

class ManageVisibleTabsDialog(val activity: BaseSimpleActivity) {
    private var view = activity.layoutInflater.inflate(R.layout.dialog_manage_visible_tabs, null)
    private val tabs = LinkedHashMap<Int, Int>()

    init {
        tabs.apply {
            put(TAB_CONTACTS, R.id.manage_visible_tabs_contacts)
            put(TAB_FAVORITES, R.id.manage_visible_tabs_favorites)
            put(TAB_CALL_HISTORY, R.id.manage_visible_tabs_call_history)
        }

        val showTabs = activity.config.showTabs
        for ((key, value) in tabs) {
            view.findViewById<MyAppCompatCheckbox>(value).isChecked = showTabs and key != 0
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this)
            }
    }

    private fun dialogConfirmed() {
        var result = 0
        for ((key, value) in tabs) {
            if (view.findViewById<MyAppCompatCheckbox>(value).isChecked) {
                result += key
            }
        }

        if (result == 0) {
            result = ALL_TABS_MASK
        }

        activity.config.showTabs = result
    }
}
