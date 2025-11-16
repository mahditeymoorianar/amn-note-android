package com.teymoorianar.amnnote.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.teymoorianar.amnnote.domain.model.Note
import androidx.compose.material3.MaterialTheme
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * Displays the collection of notes inside a vertically scrolling list.
 */
@Composable
fun NotesList(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    onMoveNote: (Int, Int) -> Unit,
    onReorderFinished: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp)
) {
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to -> onMoveNote(from.index, to.index) },
        onDragEnd = { _, _ -> onReorderFinished() },
        onDragCancel = { onReorderFinished() }
    )
    val listState = reorderableState.listState
    LazyColumn(
        state = listState,
        modifier = modifier
            .reorderable(reorderableState)
            .detectReorderAfterLongPress(reorderableState),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(items = notes, key = { it.id }) { note ->
            ReorderableItem(reorderableState, key = note.id) { isDragging ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = if (isDragging) 0.95f else 1f
                            shadowElevation = if (isDragging) 16f else 0f
                        }
                        .clip(MaterialTheme.shapes.medium)
                )
            }
        }
    }
}
