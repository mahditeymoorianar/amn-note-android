package com.teymoorianar.amnnote.domain.repository

import com.teymoorianar.amnnote.domain.model.Note

interface NoteRepository {
    suspend fun getNotes(): List<Note>
    suspend fun getNote(id: Long): Note?
    suspend fun saveNote(note: Note, encrypt: Boolean): Long
    suspend fun deleteNote(noteId: Long)
    suspend fun reorderNotes(noteIds: List<Long>)
}
