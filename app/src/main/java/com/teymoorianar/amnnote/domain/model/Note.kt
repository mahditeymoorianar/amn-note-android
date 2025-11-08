package com.teymoorianar.amnnote.domain.model

data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,           // always plaintext here
    val isEncrypted: Boolean,      // logical flag: this note is stored encrypted
    val createdAt: Long,
    val updatedAt: Long
)
