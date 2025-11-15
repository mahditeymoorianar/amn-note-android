package com.teymoorianar.amnnote.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teymoorianar.amnnote.domain.model.Note
import com.teymoorianar.amnnote.ui.main.components.CenterFab
import com.teymoorianar.amnnote.ui.main.components.CurvedBottomBar
import com.teymoorianar.amnnote.ui.main.components.MainTopAppBar
import com.teymoorianar.amnnote.ui.main.components.NotesList
import com.teymoorianar.amnnote.ui.theme.AmnNoteTheme
import androidx.compose.material3.MaterialTheme as Material3Theme

/**
 * High level composable for the main screen that wires together scaffolding and content.
 */
@Composable
fun MainScreen(
    notes: List<Note>,
    onAddNote: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Material3Theme.colorScheme.background
    val contentColor = Material3Theme.colorScheme.onBackground
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        bottomBar = { CurvedBottomBar(onSettingsClick = onSettingsClick) },
        floatingActionButton = { CenterFab(onAddNote) },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        topBar = { MainTopAppBar() }
    ) { innerPadding ->
        NotesList(
            notes = notes,
            onNoteClick = onNoteClick,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    val sampleNotes = listOf(
        Note(id = 1L, title = "Grocery list", content = "Milk, Bread, Eggs", isEncrypted = false, createdAt = 0L, updatedAt = 0L),
        Note(id = 2L, title = "Meeting notes", content = "Discuss quarterly targets.", isEncrypted = false, createdAt = 0L, updatedAt = 0L)
    )
    AmnNoteTheme {
        MainScreen(
            notes = sampleNotes,
            onAddNote = {},
            onNoteClick = {},
            onSettingsClick = {}
        )
    }
}
