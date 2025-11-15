package com.teymoorianar.amnnote.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teymoorianar.amnnote.ui.theme.AmnNoteTheme
import com.teymoorianar.amnnote.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the Compose-powered settings UI so users can configure app-wide options.
 */
@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by themeViewModel.theme.collectAsStateWithLifecycle()
            AmnNoteTheme(darkTheme = themePreference.isDarkMode) {
                SettingsScreen(
                    themePreference = themePreference,
                    onThemeSelected = themeViewModel::onThemeSelected,
                    onBackClick = { finish() }
                )
            }
        }
    }

    companion object {
        /** Creates an [Intent] that opens the settings screen. */
        fun newIntent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
