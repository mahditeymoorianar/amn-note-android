package com.teymoorianar.amnnote.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teymoorianar.amnnote.domain.preferences.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val THEME_PREFERENCES_NAME = "theme_preferences"
private val Context.themeDataStore by preferencesDataStore(name = THEME_PREFERENCES_NAME)

/**
 * Handles reading and writing theme preferences using Jetpack DataStore.
 */
@Singleton
class ThemePreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_mode")

    /** Emits the currently saved theme as a [ThemePreference]. */
    val themeMode: Flow<ThemePreference> = context.themeDataStore.data
        .map { preferences -> ThemePreference.fromValue(preferences[themeKey]) }

    /** Persists the provided [preference] value. */
    suspend fun setThemeMode(preference: ThemePreference) {
        context.themeDataStore.edit { prefs ->
            prefs[themeKey] = preference.name
        }
    }
}
