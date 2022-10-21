package com.pearltools.commons.extensions

import android.content.Context
import com.pearltools.commons.models.FileDirItem

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}
