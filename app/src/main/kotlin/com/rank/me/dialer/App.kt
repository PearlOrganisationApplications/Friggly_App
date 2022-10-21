package com.rank.me.dialer


import android.app.Application
import com.pearltools.commons.extensions.checkUseEnglish

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
    }
}
