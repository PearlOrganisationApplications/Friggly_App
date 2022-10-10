package com.rank.me.ui.base

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.rank.me.data.local.LocalData
import com.rank.me.utils.LocaleUtil

/**
 * Created by Saurabh, 27th sept 2022
 */


abstract class BaseActivity : AppCompatActivity() {

    var selectedLocaleIndex: Int = 0
    private lateinit var oldPrefLocaleCode: String

    abstract fun observeViewModel()
    protected abstract fun initViewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewBinding()
        observeViewModel()
        resetTitle()
    }

    override fun attachBaseContext(newBase: Context) {
        oldPrefLocaleCode = LocalData(newBase).getPreferredLocale()
        applyOverrideConfiguration(LocaleUtil.getLocalizedConfiguration(oldPrefLocaleCode))
        super.attachBaseContext(newBase)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * updates the toolbar text locale if it set from the android:label property of Manifest
     */
    private fun resetTitle() {
        try {
            val label = packageManager.getActivityInfo(
                componentName,
                PackageManager.GET_META_DATA
            ).labelRes
            if (label != 0) {
                setTitle(label)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("TAG", "resetTitle: $e")
        }
    }
}
