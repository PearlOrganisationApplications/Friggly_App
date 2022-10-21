package com.rank.me.ui.base

import com.rank.me.R
import com.pearltools.commons.activities.BaseSimpleActivity

open class SimpleActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher,
    )

    override fun getAppLauncherName() = getString(R.string.app_name)
}
