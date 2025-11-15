package com.teymoorianar.amnnote.data.repository

import com.teymoorianar.amnnote.data.local.ThemePreferencesDataSource
import com.teymoorianar.amnnote.domain.preferences.ThemePreference
import com.teymoorianar.amnnote.domain.repository.ThemeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Stores and exposes the selected theme preference using [ThemePreferencesDataSource].
 */
@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val dataSource: ThemePreferencesDataSource
) : ThemeRepository {

    override val themeMode: Flow<ThemePreference> = dataSource.themeMode

    override suspend fun setThemeMode(mode: ThemePreference) {
        dataSource.setThemeMode(mode)
    }
}
