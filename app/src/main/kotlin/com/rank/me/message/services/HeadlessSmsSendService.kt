package com.rank.me.message.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import com.klinker.android.send_message.Transaction
import com.rank.me.message.extensions.getSendMessageSettings
import com.rank.me.message.receivers.SmsStatusDeliveredReceiver
import com.rank.me.message.receivers.SmsStatusSentReceiver

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent == null) {
                return START_NOT_STICKY
            }

            val number = Uri.decode(intent.dataString!!.removePrefix("sms:").removePrefix("smsto:").removePrefix("mms").removePrefix("mmsto:").trim())
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val settings = getSendMessageSettings()
            val transaction = Transaction(this, settings)
            val message = com.klinker.android.send_message.Message(text, number)

            val smsSentIntent = Intent(this, SmsStatusSentReceiver::class.java)
            val deliveredIntent = Intent(this, SmsStatusDeliveredReceiver::class.java)

            transaction.setExplicitBroadcastForSentSms(smsSentIntent)
            transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

            transaction.sendNewMessage(message)
        } catch (ignored: Exception) {
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
