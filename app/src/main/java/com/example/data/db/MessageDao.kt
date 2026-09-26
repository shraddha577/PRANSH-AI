package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
  @Query("SELECT * FROM messages ORDER BY timestamp ASC")
  fun getAllMessages(): Flow<List<MessageEntity>>

  @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentMessages(limit: Int = 50): Flow<List<MessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageEntity): Long

  @Query("DELETE FROM messages")
  suspend fun clearAll()

  @Query("SELECT COUNT(*) FROM messages")
  suspend fun getMessageCount(): Int
}
