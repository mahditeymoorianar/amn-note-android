package com.teymoorianar.amnnote.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teymoorianar.amnnote.domain.model.Note

/**
 * Displays the collection of notes inside a vertically scrolling list.
 */
@Composable
fun NotesList(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp)
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(items = notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note.id) }
            )
        }
    }
}
