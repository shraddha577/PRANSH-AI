package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val text: String,
  val sender: String, // "USER", "MYRAAA", "SYSTEM"
  val timestamp: Long = System.currentTimeMillis(),
  val actionType: String? = null, // "OPEN_APP", "WHATSAPP", "DIAL", "SEARCH", "DIAGNOSTIC", "SYSTEM"
  val actionTarget: String? = null,
  val status: String = "SUCCESS"
)
