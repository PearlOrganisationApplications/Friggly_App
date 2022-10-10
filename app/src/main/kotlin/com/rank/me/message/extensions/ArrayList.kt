package com.rank.me.message.extensions

import android.text.TextUtils
import com.simplemobiletools.commons.models.SimpleContact

fun ArrayList<SimpleContact>.getThreadTitle() = TextUtils.join(", ", map { it.name }.toTypedArray())
