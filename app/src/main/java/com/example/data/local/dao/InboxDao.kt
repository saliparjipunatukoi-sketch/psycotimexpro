package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.InboxMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxDao {
    @Query("SELECT * FROM inbox_messages WHERE coachId = :coachId ORDER BY timestamp DESC")
    fun getMessagesByCoach(coachId: Long): Flow<List<InboxMessageEntity>>

    @Query("SELECT COUNT(*) FROM inbox_messages WHERE coachId = :coachId AND isRead = 0")
    fun getUnreadCount(coachId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: InboxMessageEntity): Long

    @Query("UPDATE inbox_messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM inbox_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("SELECT * FROM inbox_messages WHERE coachId = :coachId AND title LIKE :titlePrefix LIMIT 1")
    suspend fun findMessageByTitlePrefix(coachId: Long, titlePrefix: String): InboxMessageEntity?
}
