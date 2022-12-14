package com.rank.me.message.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rank.me.extensions.conversationsDB
import com.rank.me.extensions.markThreadMessagesRead
import com.rank.me.extensions.updateUnreadCountBadge
import com.rank.me.message.helpers.MARK_AS_READ
import com.rank.me.message.helpers.THREAD_ID
import com.rank.me.message.helpers.refreshMessages
import com.pearltools.commons.extensions.notificationManager
import com.pearltools.commons.helpers.ensureBackgroundThread

class MarkAsReadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MARK_AS_READ -> {
                val threadId = intent.getLongExtra(THREAD_ID, 0L)
                context.notificationManager.cancel(threadId.hashCode())
                ensureBackgroundThread {
                    context.markThreadMessagesRead(threadId)
                    context.conversationsDB.markRead(threadId)
                    context.updateUnreadCountBadge(context.conversationsDB.getUnreadConversations())
                    refreshMessages()
                }
            }
        }
    }
}
