package com.tomatoketchup.ori.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE recipientId = :recipientId ORDER BY createdAt DESC")
    fun getForRecipient(recipientId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM messages WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("DELETE FROM messages WHERE createdAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
