package com.rank.me.message.models

import android.net.Uri

data class AttachmentSelection(
    val uri: Uri,
    val isPending: Boolean,
)
