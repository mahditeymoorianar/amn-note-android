package com.teymoorianar.amnnote.ui.note

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.text.input.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teymoorianar.amnnote.R
import com.teymoorianar.amnnote.ui.components.FormattedTextField
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

                LaunchedEffect(uiState.isSaved) {
                    if (uiState.isSaved)
                        viewModel.switchReadEditMode(
                            readingMode = true
                        )
                }
                LaunchedEffect(uiState.isDeleted) {
                    if (uiState.isDeleted) {
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
                    onTitleChange = { viewModel.onTitleChange(it);
                        viewModel.switchReadEditMode(readingMode = false)},
                    onContentChange = { viewModel.onContentChange(it);
                        viewModel.switchReadEditMode(readingMode = false)},
                    onSaveClick = { viewModel.saveNote() },
                    onDeleteClick = { viewModel.deleteNote() },
                    switchEditMode = {
                        readingMode ->
                            viewModel.switchReadEditMode(readingMode = readingMode)

                    },
                    onForceFinish = {
                        activity?.setResult(Activity.RESULT_OK)
                        activity?.finish()
                    }
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
    onDeleteClick: () -> Unit,
    switchEditMode: (Boolean) -> Unit,
    onForceFinish: () -> Unit
) {
    val scrollState = rememberScrollState()

    // controls showing the bottom sheet
    var showDiscardSheet by remember { mutableStateOf(false) }

    // 1) intercept back press HERE
    val handleBack: () -> Unit = {
        if (state.readingMode) {
            onForceFinish()
        } else {
            showDiscardSheet = true
        }
    }

    // Intercept system back press
    BackHandler(enabled = true) {
        handleBack()
    }

    // 2) sheet itself (Compose only)
    if (showDiscardSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDiscardSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Discard unsaved changes?",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "If you go back now, your changes will be lost.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDiscardSheet = false }) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            showDiscardSheet = false
                            onForceFinish()
                        }
                    ) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    val newNote =
        stringResource(R.string.note_editor_title_new)
    val noteTitle = if (state.noteId == 0L) newNote else state.title
    var enteringTitle by remember { mutableStateOf(noteTitle) }
    var contentField by remember { mutableStateOf(TextFieldValue(state.content)) }

    LaunchedEffect(state.content) {
        if (state.content != contentField.text) {
            contentField = TextFieldValue(
                text = state.content,
                selection = TextRange(state.content.length)
            )
        }
    }
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
                                noteTitle,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                val nowFocused = focusState.isFocused
                                if (nowFocused && state.readingMode) {
                                    switchEditMode(false) // <-- your custom function
                                }
                            },
                        enabled = true,
                        leadingIcon =
                            {
                                IconButton(onClick = handleBack)
                                {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                        contentDescription = "back",
                                        tint = Material3Theme.colorScheme.primary,
                                    )
                                }
                            },
                        trailingIcon =
                            {
                                if (state.readingMode)
                                    IconButton(
                                        onClick = {}
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "back",
                                            tint = Color.Transparent,
                                            modifier = Modifier
                                                .size(30.dp)
                                        )
                                    }
                                else IconButton(
                                    onClick = onSaveClick,
                                    enabled = !state.isLoading,
                                    modifier = Modifier.focusable(true)
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
                        ),
                        textStyle = TextStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp
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
        val imeVisible = isImeVisible()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                FormattedTextField(
                    value = contentField,
                    onValueChange = { newValue ->
                        contentField = newValue
                        onContentChange(newValue.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .onFocusChanged { focusState ->
                            val nowFocused = focusState.isFocused
                            if (nowFocused && state.readingMode) {
                                switchEditMode(false) // <-- your custom function
                            }
                        },
//                label = { Text(text = stringResource(id = R.string.note_content_label)) },
                    placeholder = { Text("Write here...") },
                    minLines = 40,
                    maxLines = Int.MAX_VALUE,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        fontSize = 20.sp
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
                if (!state.readingMode && !imeVisible) {
                    Spacer(Modifier.padding(bottom = 80.dp)) // ~panel height; tweak as needed
                }
            }
            // NEW: The floating, rounded, horizontally scrollable panel
            WritingToolsPanel(
                visible = !state.readingMode,
                tools = writingToolsDemo(),               // supply your tools here
                onToolClick = { tool ->
                    // TODO: perform your action (insert markdown, toggle style, etc.)
                    // Example: onContentChange(applyTool(state.content, tool))
                },
                bottomMargin = 8.dp,
                cornerRadius = 20.dp,
                elevated = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)// required for alignment inside Box
            )
        }
    }
}


data class ToolItem(
    val id: String,
    val iconRes: Int,
    val label: String,
    val color: Color? = null
)

@Composable
fun WritingToolsPanel(
    visible: Boolean,
    tools: List<ToolItem>,
    onToolClick: (ToolItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomMargin: Dp = 8.dp,
    cornerRadius: Dp = 20.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevated: Boolean = true,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(180)) + fadeIn(tween(150)),
        exit = slideOutVertically(targetOffsetY = { it / 3 }, animationSpec = tween(160)) + fadeOut(tween(120)),
        modifier = modifier
            .padding(bottom = bottomMargin)
            .imePadding()                // jump above IME when it shows
            .navigationBarsPadding()     // hover above gesture bar when IME hidden
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        val shape = RoundedCornerShape(cornerRadius)

        val rowContent: @Composable () -> Unit = {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
                items(tools, key = { it.id }) { tool ->
                    ToolChip(item = tool, onClick = { onToolClick(tool) }, contentColor = contentColor)
                }
            }
        }

        if (elevated) {
            Card(
                shape = shape,
                colors = CardDefaults.cardColors(containerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) { rowContent() }
        } else {
            Surface(shape = shape, color = containerColor, contentColor = contentColor, modifier = Modifier.fillMaxWidth()) {
                rowContent()
            }
        }
    }
}

@Composable
private fun ToolChip(
    item: ToolItem,
    onClick: () -> Unit,
    contentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = contentColor.copy(alpha = 0.00f),
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
        tonalElevation = 0.dp
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(start = 2.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
        ) {
            IconButton(onClick = onClick, modifier = Modifier) {
                Icon(painter = painterResource(item.iconRes),
                    contentDescription = item.label,
                    tint = item.color?: Color(0xFFFFFFFF)
                )
            }
            Text(text = item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * Helper you can use to know whether the IME is visible.
 * (No extra dependencies needed; uses insets height in px.)
 */
@Composable
fun isImeVisible(): Boolean {
    val bottomPx = WindowInsets.ime.getBottom(LocalDensity.current)
    return bottomPx > 0
}

private fun writingToolsDemo(): List<ToolItem> = listOf(
    ToolItem("ltr", R.drawable.rounded_format_textdirection_l_to_r_24, ""),
    ToolItem("rtl", R.drawable.rounded_format_textdirection_r_to_l_24, ""),
    ToolItem("bold", R.drawable.rounded_format_bold_24,        ""),
    ToolItem("italic", R.drawable.rounded_format_italic_24,      ""),
    ToolItem("heading",        R.drawable.tag_24px,          ""),
    ToolItem("bullet",    R.drawable.rounded_format_list_bulleted_24, ""),
    ToolItem("H1",    R.drawable.format_h1_24px, ""),
    ToolItem("H2",    R.drawable.format_h2_24px, ""),
    ToolItem("H3",    R.drawable.format_h3_24px, ""),
    ToolItem("H4",    R.drawable.format_h4_24px, ""),
    ToolItem("H5",    R.drawable.format_h5_24px, ""),
    ToolItem("delete",    R.drawable.delete_24px, "", color = Color(0xFfCf0000)),
//    ToolItem("code",      R.drawable.rounded_checkbook_24,        ""), TODO
//    ToolItem("quote",     R.drawable.rounded_checkbook_24,       ""), TODO
)
