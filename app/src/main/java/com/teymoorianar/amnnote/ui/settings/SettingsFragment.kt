package com.teymoorianar.amnnote.ui.settings

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.teymoorianar.amnnote.R
import com.teymoorianar.amnnote.domain.preferences.ThemePreference
import com.teymoorianar.amnnote.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Displays user-configurable options such as the preferred color theme.
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        configureThemePreference()
    }

    /** Wires listeners and observers for the color theme preference item. */
    private fun configureThemePreference() {
        val key = getString(R.string.pref_key_theme_mode)
        val themePreference = findPreference<ListPreference>(key) ?: return
        themePreference.setSummaryProvider { preference ->
            val listPreference = preference as ListPreference
            listPreference.entry ?: getString(R.string.theme_mode_dark)
        }
        themePreference.setOnPreferenceChangeListener { _, newValue ->
            ThemePreference.fromValue(newValue as? String)
                .also { preference ->
                    themeViewModel.onThemeSelected(preference)
                }
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                themeViewModel.theme.collectLatest { preference ->
                    if (themePreference.value != preference.name) {
                        themePreference.value = preference.name
                    }
                }
            }
        }
    }
}
