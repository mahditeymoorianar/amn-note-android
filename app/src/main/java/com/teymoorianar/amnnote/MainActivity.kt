package com.teymoorianar.amnnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as Material3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teymoorianar.amnnote.domain.model.Note
import com.teymoorianar.amnnote.ui.main.MainViewModel
import com.teymoorianar.amnnote.ui.note.NoteActivity
import com.teymoorianar.amnnote.ui.theme.AmnNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.material3.MaterialTheme as Material3Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmnNoteTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val notes by viewModel.notes.collectAsState()
                Screen(
                    notes = notes,
                    onAddNote = { startActivity(NoteActivity.newIntent(this)) },
                    onNoteClick = { id -> startActivity(NoteActivity.newIntent(this, id)) },
                    onMove = viewModel::moveNote,
                    onDragEnd = viewModel::onDragFinished
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    notes: List<Note>,
    onAddNote: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
) {
    val backgroundColor = Material3Theme.colorScheme.background

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = { CurvedBottomBar() },
        floatingActionButton = { CenterFab(onAddNote) },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        backgroundColor = backgroundColor,
        topBar = { MainTopAppBar() }
    ) { innerPadding ->
        NotesList(
            notes = notes,
            onNoteClick = onNoteClick,
            onMove = onMove,
            onDragEnd = onDragEnd,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to -> onMove(from.index, to.index) },
        onDragEnd = { _, _ -> onDragEnd() }
    )

    LazyColumn(
        modifier = modifier
            .reorderable(reorderableState)
            .detectReorderAfterLongPress(reorderableState),
        state = reorderableState.listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(notes, key = { _, note -> note.id }) { _, note ->
            ReorderableItem(reorderableState, key = note.id) { isDragging ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                    modifier = Modifier.alpha(if (isDragging) 0.6f else 1f)
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Material3Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Material3Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MainTopAppBar() {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Text(
                text = stringResource(R.string.app_name),
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* TODO: handle navigation click */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Material3Theme.colorScheme.onPrimary,
        elevation = 4.dp
    )
}

@Composable
fun CurvedBottomBar() {
    BottomAppBar(
        modifier = Modifier.navigationBarsPadding(),
        cutoutShape = CircleShape,
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Color.White,
        elevation = 28.dp
    ) {
        IconButton(onClick = { /* TODO: handle Home click */ }) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home"
            )
        }

        Spacer(modifier = Modifier.weight(0.8f))

        IconButton(onClick = { /* TODO: handle Settings click */ }) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

@Composable
fun CenterFab(onAddNote: () -> Unit) {
    FloatingActionButton(
        onClick = onAddNote,
        shape = CircleShape,
        backgroundColor = Material3Theme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Rounded.Create,
            contentDescription = "Add"
        )
    }
}

@Preview
@Composable
fun PreviewScreen() {
    val sampleNotes = listOf(
        Note(
            id = 1L,
            title = "Grocery list",
            content = "Milk, Bread, Eggs, Butter",
            isEncrypted = false,
            createdAt = 0L,
            updatedAt = 0L
        ),
        Note(
            id = 2L,
            title = "Meeting notes",
            content = "Discuss quarterly targets and hiring plan.",
            isEncrypted = false,
            createdAt = 0L,
            updatedAt = 0L
        )
    )
    AmnNoteTheme {
        Screen(
            notes = sampleNotes,
            onAddNote = {},
            onNoteClick = {},
            onMove = { _, _ -> },
            onDragEnd = {}
        )
    }
}
