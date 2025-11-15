package com.teymoorianar.amnnote.domain.repository

import com.teymoorianar.amnnote.domain.preferences.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for persisting and observing the selected color theme.
 */
interface ThemeRepository {
    /** Emits updates whenever the user selects a different color theme. */
    val themeMode: Flow<ThemePreference>

    /** Persists the newly selected color [mode]. */
    suspend fun setThemeMode(mode: ThemePreference)
}
