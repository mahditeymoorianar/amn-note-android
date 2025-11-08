package com.teymoorianar.amnnote.di

import android.content.Context
import androidx.room.Room
import com.teymoorianar.amnnote.data.local.db.NoteDatabase
import com.teymoorianar.amnnote.data.local.db.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NoteDatabase = Room.databaseBuilder(
        context,
        NoteDatabase::class.java,
        "notes.db"
    ).fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideNoteDao(database: NoteDatabase): NoteDao = database.noteDao()
}
