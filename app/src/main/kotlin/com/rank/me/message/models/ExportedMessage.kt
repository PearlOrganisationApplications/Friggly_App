package com.rank.me.message.models

import com.google.gson.annotations.SerializedName

data class ExportedMessage(
    @SerializedName("sms")
    val sms: List<SmsBackup>?,
    @SerializedName("mms")
    val mms: List<MmsBackup>?,
)
