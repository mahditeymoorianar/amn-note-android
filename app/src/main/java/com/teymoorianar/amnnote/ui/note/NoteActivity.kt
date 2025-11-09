package com.teymoorianar.amnnote.ui.note

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teymoorianar.amnnote.R
import com.teymoorianar.amnnote.ui.theme.AmnNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.MaterialTheme as Material3Theme

@AndroidEntryPoint
class NoteActivity : ComponentActivity() {

    private val viewModel: NoteEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmnNoteTheme {
                val uiState by viewModel.state.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current
                val activity = context as? Activity

                LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
                    if (uiState.isSaved || uiState.isDeleted) {
                        activity?.setResult(RESULT_OK)
                        activity?.finish()
                    }
                }

                LaunchedEffect(uiState.errorMessage) {
                    val message = uiState.errorMessage
                    if (!message.isNullOrEmpty()) {
                        snackbarHostState.showSnackbar(message)
                        viewModel.dismissMessage()
                    }
                }

                NoteEditorScreen(
                    state = uiState,
                    snackbarHostState = snackbarHostState,
                    onTitleChange = viewModel::onTitleChange,
                    onContentChange = viewModel::onContentChange,
                    onSaveClick = { viewModel.saveNote() },
                    onDeleteClick = { viewModel.deleteNote() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_NOTE_ID: String = "extra_note_id"

        fun newIntent(context: Context, noteId: Long = 0L): Intent {
            return Intent(context, NoteActivity::class.java).apply {
                if (noteId != 0L) {
                    putExtra(EXTRA_NOTE_ID, noteId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorScreen(
    state: NoteEditorState,
    snackbarHostState: SnackbarHostState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val newNote =
        stringResource(R.string.note_editor_title_new)
    val noteTitle = if (state.noteId == 0L) newNote else state.title
    var enteringTitle by remember { mutableStateOf(noteTitle) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = state.title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text(
                                noteTitle
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        leadingIcon =
                            {
                                IconButton(onClick = {/* TODO */})
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowBackIosNew,
                                        contentDescription = "back",
                                        tint = Material3Theme.colorScheme.primary
                                    )
                                }
                            },
                        trailingIcon =
                            {
                                IconButton(
                                    onClick = onSaveClick,
                                    enabled = !state.isLoading,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "back",
                                        tint = Material3Theme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(30.dp)
                                    )
                                }
                            },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color(0xFFFFF8E1),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )

//                        lineLimits = TextFieldLineLimits.Default
//                        readOnly = false,
//                        textStyle = TODO(),
//                        labelPosition = TODO(),
//                        label = TODO(),
//                        prefix = TODO(),
//                        suffix = TODO(),
//                        supportingText = TODO(),
//                        isError = TODO(),
//                        inputTransformation = TODO(),
//                        outputTransformation = TODO(),
//                        keyboardOptions = TODO(),
//                        onKeyboardAction = TODO(),
//                        onTextLayout = TODO(),
//                        scrollState = TODO(),
//                        shape = TODO(),
//                        colors = TODO(),
//                        contentPadding = TODO(),
//                        interactionSource = TODO(),
                    )
//                    Text(
//                        text = if (state.noteId == 0L) {
//                            stringResource(id = R.string.note_editor_title_new)
//                        } else {
//                            stringResource(id = R.string.note_editor_title_edit)
//                        },
//                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            TextField(
                value = state.content,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxWidth(),
//                label = { Text(text = stringResource(id = R.string.note_content_label)) },
                placeholder = {Text("Write here...")},
                minLines = 6,
                maxLines = Int.MAX_VALUE,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (state.noteId != 0L) {
                    OutlinedButton(
                        onClick = onDeleteClick,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.delete))
                    }
                }
            }

            if (!state.isLoading && state.noteId == 0L) {
                Text(
                    text = stringResource(id = R.string.note_hint_text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

