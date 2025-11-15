package com.teymoorianar.amnnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teymoorianar.amnnote.ui.main.MainScreen
import com.teymoorianar.amnnote.ui.main.MainViewModel
import com.teymoorianar.amnnote.ui.note.NoteActivity
import com.teymoorianar.amnnote.ui.settings.SettingsActivity
import com.teymoorianar.amnnote.ui.theme.AmnNoteTheme
import com.teymoorianar.amnnote.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity that renders the list of notes and exposes navigation actions.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val notes by viewModel.notes.collectAsStateWithLifecycle()
            val themePreference by themeViewModel.theme.collectAsStateWithLifecycle()
            AmnNoteTheme(darkTheme = themePreference.isDarkMode) {
                MainScreen(
                    notes = notes,
                    onAddNote = { startActivity(NoteActivity.newIntent(this)) },
                    onNoteClick = { id -> startActivity(NoteActivity.newIntent(this, id)) },
                    onSettingsClick = { startActivity(SettingsActivity.newIntent(this)) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshNotes()
    }
}
