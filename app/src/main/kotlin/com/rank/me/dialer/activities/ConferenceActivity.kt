package com.rank.me.dialer.activities

import android.os.Bundle
import com.rank.me.R
import com.rank.me.dialer.adapters.ConferenceCallsAdapter
import com.rank.me.dialer.helpers.CallManager
import com.rank.me.ui.base.SimpleActivity
import com.simplemobiletools.commons.helpers.NavigationIcon
import kotlinx.android.synthetic.main.activity_conference.*

class ConferenceActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conference)

        conference_calls_list.adapter = ConferenceCallsAdapter(this, conference_calls_list, ArrayList(CallManager.getConferenceCalls())) {}
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(conference_toolbar, NavigationIcon.Arrow)
    }
}
