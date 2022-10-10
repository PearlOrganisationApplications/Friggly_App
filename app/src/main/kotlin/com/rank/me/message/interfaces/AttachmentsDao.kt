package com.rank.me.message.interfaces

import androidx.room.Dao
import androidx.room.Query
import com.rank.me.message.models.Attachment

@Dao
interface AttachmentsDao {
    @Query("SELECT * FROM attachments")
    fun getAll(): List<Attachment>
}
