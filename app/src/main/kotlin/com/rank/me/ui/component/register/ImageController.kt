package com.rank.me.ui.component.register

import android.net.Uri
import android.widget.ImageView
import coil.api.load
import coil.size.Scale

class ImageController(private val imgMain: ImageView) {

    fun setImgMain(path: Uri) {
        imgMain.load(path) {
            scale(Scale.FILL)
            scale(Scale.FIT)
        }
    }
}