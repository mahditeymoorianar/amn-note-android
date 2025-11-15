package com.teymoorianar.amnnote.di

import com.teymoorianar.amnnote.data.repository.NoteRepositoryImpl
import com.teymoorianar.amnnote.data.repository.ThemeRepositoryImpl
import com.teymoorianar.amnnote.domain.repository.NoteRepository
import com.teymoorianar.amnnote.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: ThemeRepositoryImpl
    ): ThemeRepository
}
