package com.rank.me

import android.app.Application
import com.pearltools.commons.extensions.checkUseEnglish
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by Saurabh, 27th sept 2022
 */
@HiltAndroidApp
open class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
    }
}
