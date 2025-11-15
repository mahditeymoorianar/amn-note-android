package com.teymoorianar.amnnote.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teymoorianar.amnnote.domain.preferences.ThemePreference
import com.teymoorianar.amnnote.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes the persisted theme preference to UI layers and updates it when required.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    /** Current theme preference as a hot [StateFlow]. */
    val theme: StateFlow<ThemePreference> = themeRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemePreference.DARK
        )

    /** Persists a new [preference] when the user selects a different theme. */
    fun onThemeSelected(preference: ThemePreference) {
        viewModelScope.launch {
            themeRepository.setThemeMode(preference)
        }
    }
}
