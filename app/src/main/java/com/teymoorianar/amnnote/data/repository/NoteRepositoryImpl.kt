package com.teymoorianar.amnnote.data.repository

import com.teymoorianar.amnnote.data.crypto.CryptoManager
import com.teymoorianar.amnnote.data.local.db.dao.NoteDao
import com.teymoorianar.amnnote.data.local.db.entity.NoteEntity
import com.teymoorianar.amnnote.domain.model.Note
import com.teymoorianar.amnnote.domain.repository.NoteRepository
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val cryptoManager: CryptoManager
) : NoteRepository {

    override suspend fun getNotes(): List<Note> =
        dao.getAll().map { it.toDomain(cryptoManager) }

    override suspend fun getNote(id: Long): Note? =
        dao.getById(id)?.toDomain(cryptoManager)

    override suspend fun saveNote(note: Note, encrypt: Boolean): Long {
        val now = System.currentTimeMillis()
        val cipherBytes = if (encrypt) {
            cryptoManager.encrypt(note.content)
        } else {
            // still go through encryption path for consistency → or store raw.
            cryptoManager.encrypt(note.content)
        }

        val entity = NoteEntity(
            id = note.id,
            title = note.title,
            contentCipher = cipherBytes,
            isEncrypted = encrypt,
            createdAt = if (note.id == 0L) now else note.createdAt,
            updatedAt = now
        )
        return if (note.id == 0L) dao.insert(entity) else {
            dao.update(entity)
            note.id
        }
    }

    override suspend fun deleteNote(noteId: Long) {
        dao.getById(noteId)?.let { dao.delete(it) }
    }

    private fun NoteEntity.toDomain(cryptoManager: CryptoManager): Note {
        val contentPlain = cryptoManager.decrypt(contentCipher)
        return Note(
            id = id,
            title = title,
            content = contentPlain,
            isEncrypted = isEncrypted,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
