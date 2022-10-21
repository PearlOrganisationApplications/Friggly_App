package com.rank.me.extensions

import android.text.TextUtils
import com.pearltools.commons.models.SimpleContact

fun ArrayList<SimpleContact>.getThreadTitle() = TextUtils.join(", ", map { it.name }.toTypedArray())
