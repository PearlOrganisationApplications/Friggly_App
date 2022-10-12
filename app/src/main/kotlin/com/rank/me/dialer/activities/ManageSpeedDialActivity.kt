package com.rank.me.dialer.activities

import android.os.Bundle
import com.google.gson.Gson
import com.rank.me.R
import com.rank.me.dialer.adapters.SpeedDialAdapter
import com.rank.me.dialer.dialogs.SelectContactDialog
import com.rank.me.dialer.extensions.config
import com.rank.me.dialer.interfaces.RemoveSpeedDialListener
import com.rank.me.dialer.models.SpeedDial
import com.rank.me.ui.base.SimpleActivity
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.SimpleContactsHelper
import com.simplemobiletools.commons.models.SimpleContact
import kotlinx.android.synthetic.main.activity_manage_speed_dial.*

class ManageSpeedDialActivity : SimpleActivity(), RemoveSpeedDialListener {
    private var allContacts = ArrayList<SimpleContact>()
    private var speedDialValues = ArrayList<SpeedDial>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_speed_dial)

        speedDialValues = config.getSpeedDialValues()
        updateAdapter()
        SimpleContactsHelper(this).getAvailableContacts(false) { contacts ->
            allContacts = contacts
        }

        updateTextColors(manage_speed_dial_scrollview)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(manage_speed_dial_toolbar, NavigationIcon.Arrow)
    }

    override fun onStop() {
        super.onStop()
        config.speedDial = Gson().toJson(speedDialValues)
    }

    private fun updateAdapter() {
        SpeedDialAdapter(this, speedDialValues, this, speed_dial_list) {
            val clickedContact = it as SpeedDial
            if (allContacts.isEmpty()) {
                return@SpeedDialAdapter
            }

            SelectContactDialog(this, allContacts) { selectedContact ->
                speedDialValues.first { it.id == clickedContact.id }.apply {
                    displayName = selectedContact.name
                    number = selectedContact.phoneNumbers.first().normalizedNumber
                }
                updateAdapter()
            }
        }.apply {
            speed_dial_list.adapter = this
        }
    }

    override fun removeSpeedDial(ids: ArrayList<Int>) {
        ids.forEach {
            val dialId = it
            speedDialValues.first { it.id == dialId }.apply {
                displayName = ""
                number = ""
            }
        }
        updateAdapter()
    }
}
