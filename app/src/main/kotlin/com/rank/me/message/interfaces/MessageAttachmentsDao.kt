package com.rank.me.message.interfaces

import androidx.room.Dao
import androidx.room.Query
import com.rank.me.message.models.MessageAttachment

@Dao
interface MessageAttachmentsDao {
    @Query("SELECT * FROM message_attachments")
    fun getAll(): List<MessageAttachment>
}
