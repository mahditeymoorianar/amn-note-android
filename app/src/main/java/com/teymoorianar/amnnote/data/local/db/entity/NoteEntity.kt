package com.teymoorianar.amnnote.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val contentCipher: ByteArray,     // encrypted bytes
    val isEncrypted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
