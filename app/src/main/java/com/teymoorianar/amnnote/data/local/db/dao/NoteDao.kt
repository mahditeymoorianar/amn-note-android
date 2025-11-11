package com.teymoorianar.amnnote.data.local.db.dao

import androidx.room.*
import com.teymoorianar.amnnote.data.local.db.entity.NoteEntity

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAll(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("UPDATE notes SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateUpdatedAt(id: Long, updatedAt: Long)
}
