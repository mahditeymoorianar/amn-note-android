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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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
import com.teymoorianar.amnnote.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@AndroidEntryPoint
class NoteActivity : ComponentActivity() {

    private val viewModel: NoteEditorViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by themeViewModel.theme.collectAsStateWithLifecycle()
            AmnNoteTheme(darkTheme = themePreference.isDarkMode) {
                val uiState by viewModel.state.collectAsStateWithLifecycle()
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                        val processedValue = handleListAutoFormatting(
                            oldValue = contentField,
                            newValue = newValue
                        )
                        contentField = processedValue
                        onContentChange(processedValue.text)
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
                tools = writingTools(state.noteId != 0L),               // supply your tools here
                onToolClick = { tool ->
                    if (state.isLoading) return@WritingToolsPanel
                    when (tool.id) {
                        "delete" -> if (state.noteId != 0L) {
                            showDeleteConfirm = true
                        }

                        else -> {
                            val updatedValue = applyToolAction(tool.id, contentField)
                            if (updatedValue != contentField) {
                                contentField = updatedValue
                                onContentChange(updatedValue.text)
                            }
                        }
                    }
                },
                bottomMargin = 8.dp,
                cornerRadius = 20.dp,
                elevated = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)// required for alignment inside Box
            )

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text(stringResource(id = R.string.delete_note_title)) },
                    text = { Text(stringResource(id = R.string.delete_note_message)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirm = false
                                onDeleteClick()
                            },
                            enabled = !state.isLoading
                        ) {
                            Text(text = stringResource(id = R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    }
                )
            }
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

private val bulletLineRegex = Regex("^([\\t ]*)- (.*)$")

private fun handleListAutoFormatting(
    oldValue: TextFieldValue,
    newValue: TextFieldValue
): TextFieldValue {
    val newSelection = newValue.selection
    if (!newSelection.collapsed) return newValue

    if (newValue.text.length != oldValue.text.length + 1) return newValue

    val cursor = newSelection.start
    if (cursor == 0 || cursor > newValue.text.length) return newValue

    if (newValue.text[cursor - 1] != '\n') return newValue

    val newlineIndex = cursor - 1
    val lineStart = newValue.text.lastIndexOf('\n', newlineIndex - 1).let { index ->
        if (index == -1) 0 else index + 1
    }
    if (lineStart > newlineIndex) return newValue

    val lineContent = newValue.text.substring(lineStart, newlineIndex)
    val match = bulletLineRegex.matchEntire(lineContent) ?: return newValue

    val indent = match.groupValues[1]
    val afterDash = match.groupValues[2]

    return if (afterDash.isBlank()) {
        val builder = StringBuilder(newValue.text)
        builder.delete(lineStart, newlineIndex)
        val updatedText = builder.toString()
        val newCursor = cursor - (newlineIndex - lineStart)
        newValue.copy(text = updatedText, selection = TextRange(newCursor))
    } else {
        val insertion = indent + "- "
        val builder = StringBuilder(newValue.text)
        builder.insert(cursor, insertion)
        val updatedText = builder.toString()
        val newCursor = cursor + insertion.length
        newValue.copy(text = updatedText, selection = TextRange(newCursor))
    }
}

private fun applyToolAction(toolId: String, value: TextFieldValue): TextFieldValue {
    return when (toolId) {
        "ltr" -> insertDirective(value, "\\ltr")
        "rtl" -> insertDirective(value, "\\rtl")
        "bold" -> surroundSelectionWith(value, "**")
        "italic" -> surroundSelectionWith(value, "*")
        "bullet" -> applyBulletToggle(value)
        "indent-inc" -> applyIndentChange(value, increase = true)
        "indent-dec" -> applyIndentChange(value, increase = false)
        "H1" -> applyHeadingLevel(value, 1)
        "H2" -> applyHeadingLevel(value, 2)
        "H3" -> applyHeadingLevel(value, 3)
        "H4" -> applyHeadingLevel(value, 4)
        "H5" -> applyHeadingLevel(value, 5)
        else -> value
    }
}

private fun insertDirective(value: TextFieldValue, directive: String): TextFieldValue {
    val selection = value.selection
    val start = selection.start.coerceIn(0, value.text.length)
    val end = selection.end.coerceIn(0, value.text.length)
    val builder = StringBuilder(value.text)
    builder.replace(start, end, directive)
    val newCursor = start + directive.length
    val updatedText = builder.toString()
    return value.copy(text = updatedText, selection = TextRange(newCursor))
}

private fun surroundSelectionWith(value: TextFieldValue, marker: String): TextFieldValue {
    val selection = value.selection
    val text = value.text
    val start = selection.start.coerceIn(0, text.length)
    val end = selection.end.coerceIn(0, text.length)

    if (start == end) {
        val insertion = marker + marker
        val builder = StringBuilder(text)
        builder.insert(start, insertion)
        val cursor = start + marker.length
        val updatedText = builder.toString()
        return value.copy(text = updatedText, selection = TextRange(cursor, cursor))
    }

    val before = text.substring(0, start)
    val middle = text.substring(start, end)
    val after = text.substring(end)
    val hasWrappingPrefix = before.endsWith(marker)
    val hasWrappingSuffix = after.startsWith(marker)

    return if (hasWrappingPrefix && hasWrappingSuffix) {
        val newBefore = before.dropLast(marker.length)
        val newAfter = after.drop(marker.length)
        val updatedText = newBefore + middle + newAfter
        val newStart = start - marker.length
        val newEnd = end - marker.length
        value.copy(text = updatedText, selection = TextRange(newStart, newEnd))
    } else {
        val updatedText = before + marker + middle + marker + after
        val newStart = start + marker.length
        val newEnd = end + marker.length
        value.copy(text = updatedText, selection = TextRange(newStart, newEnd))
    }
}

private data class LineRange(val start: Int, val endExclusive: Int)

private data class LineChange(
    val newLine: String,
    val oldPrefixLength: Int,
    val newPrefixLength: Int
)

private fun applyHeadingLevel(value: TextFieldValue, level: Int): TextFieldValue {
    val prefix = "#".repeat(level) + " "
    return applyLineChanges(value) { oldLine ->
        var index = 0
        while (index < oldLine.length && (oldLine[index] == ' ' || oldLine[index] == '\t')) {
            index++
        }
        var hashesIndex = index
        while (hashesIndex < oldLine.length && oldLine[hashesIndex] == '#') {
            hashesIndex++
        }
        var spaceIndex = hashesIndex
        while (spaceIndex < oldLine.length && oldLine[spaceIndex] == ' ') {
            spaceIndex++
        }
        val contentStart = spaceIndex
        val content = oldLine.substring(contentStart)
        val newLine = prefix + content
        val oldPrefixLength = contentStart
        val newPrefixLength = prefix.length
        if (newLine == oldLine && oldPrefixLength == newPrefixLength) {
            null
        } else {
            LineChange(newLine, oldPrefixLength, newPrefixLength)
        }
    }
}

private fun applyBulletToggle(value: TextFieldValue): TextFieldValue {
    return applyLineChanges(value) { oldLine ->
        var index = 0
        while (index < oldLine.length && (oldLine[index] == ' ' || oldLine[index] == '\t')) {
            index++
        }
        val indent = index
        val trimmed = oldLine.substring(indent)
        return@applyLineChanges if (trimmed.startsWith("- ")) {
            val content = trimmed.removePrefix("- ")
            val newLine = oldLine.substring(0, indent) + content
            LineChange(newLine, indent + 2, indent)
        } else {
            val newLine = oldLine.substring(0, indent) + "- " + trimmed
            LineChange(newLine, indent, indent + 2)
        }
    }
}

private const val INDENT_STEP = 2

private fun applyIndentChange(value: TextFieldValue, increase: Boolean): TextFieldValue {
    return applyLineChanges(value) { oldLine ->
        var index = 0
        while (index < oldLine.length && (oldLine[index] == ' ' || oldLine[index] == '\t')) {
            index++
        }
        val indent = index
        val trimmed = oldLine.substring(indent)
        if (!trimmed.startsWith("- ")) {
            return@applyLineChanges null
        }
        return@applyLineChanges if (increase) {
            val newLine = " ".repeat(INDENT_STEP) + oldLine
            LineChange(newLine, indent + 2, indent + INDENT_STEP + 2)
        } else {
            if (indent == 0) {
                null
            } else {
                val removal = INDENT_STEP.coerceAtMost(indent)
                val newIndent = indent - removal
                val newLine = oldLine.substring(removal)
                LineChange(newLine, indent + 2, newIndent + 2)
            }
        }
    }
}

private fun applyLineChanges(
    value: TextFieldValue,
    transform: (String) -> LineChange?
): TextFieldValue {
    val text = value.text
    val ranges = selectedLineRanges(text, value.selection)
    if (ranges.isEmpty()) return value
    val builder = StringBuilder(text)
    var selectionStart = value.selection.start
    var selectionEnd = value.selection.end
    var delta = 0
    var changed = false
    for (range in ranges) {
        val startInBuilder = range.start + delta
        val endInBuilder = range.endExclusive + delta
        val oldLine = builder.substring(startInBuilder, endInBuilder)
        val change = transform(oldLine) ?: continue
        changed = true
        builder.replace(startInBuilder, endInBuilder, change.newLine)
        val lineDelta = change.newLine.length - oldLine.length
        selectionStart = adjustSelectionIndex(
            selectionStart,
            startInBuilder,
            endInBuilder,
            oldLine,
            change,
            lineDelta
        )
        selectionEnd = adjustSelectionIndex(
            selectionEnd,
            startInBuilder,
            endInBuilder,
            oldLine,
            change,
            lineDelta
        )
        delta += lineDelta
    }
    if (!changed) return value
    val updatedText = builder.toString()
    val newSelectionStart = selectionStart.coerceIn(0, updatedText.length)
    val newSelectionEnd = selectionEnd.coerceIn(0, updatedText.length)
    return value.copy(text = updatedText, selection = TextRange(newSelectionStart, newSelectionEnd))
}

private fun adjustSelectionIndex(
    index: Int,
    lineStart: Int,
    lineEnd: Int,
    oldLine: String,
    change: LineChange,
    lineDelta: Int
): Int {
    if (index < lineStart) return index
    if (index >= lineEnd) return index + lineDelta
    val offset = index - lineStart
    val prefixLength = change.oldPrefixLength.coerceAtMost(oldLine.length)
    val contentOffset = (offset - prefixLength).coerceAtLeast(0)
    val newIndex = lineStart + change.newPrefixLength + contentOffset
    val maxIndex = lineStart + change.newLine.length
    return newIndex.coerceAtMost(maxIndex)
}

private fun selectedLineRanges(text: String, selection: TextRange): List<LineRange> {
    if (text.isEmpty()) {
        return listOf(LineRange(0, 0))
    }
    val length = text.length
    val start = selection.start.coerceIn(0, length)
    val end = selection.end.coerceIn(0, length)
    val minPos = minOf(start, end)
    val maxPos = maxOf(start, end)
    var lineStart = if (minPos == 0) 0 else {
        val prevNewline = text.lastIndexOf('\n', minPos - 1)
        if (prevNewline == -1) 0 else prevNewline + 1
    }
    val ranges = mutableListOf<LineRange>()
    while (true) {
        val newlineIndex = text.indexOf('\n', lineStart)
        val lineEnd = if (newlineIndex == -1) length else newlineIndex
        ranges += LineRange(lineStart, lineEnd)
        if (lineEnd >= maxPos || lineEnd == length) {
            break
        }
        lineStart = lineEnd + 1
        if (lineStart > length) {
            ranges += LineRange(length, length)
            break
        }
    }
    return ranges
}

private fun writingTools(canDelete: Boolean): List<ToolItem> {
    val items = mutableListOf(
        ToolItem("ltr", R.drawable.rounded_format_textdirection_l_to_r_24, ""),
        ToolItem("rtl", R.drawable.rounded_format_textdirection_r_to_l_24, ""),
        ToolItem("bold", R.drawable.rounded_format_bold_24, ""),
        ToolItem("italic", R.drawable.rounded_format_italic_24, ""),
        ToolItem("heading", R.drawable.tag_24px, ""),
        ToolItem("bullet", R.drawable.rounded_format_list_bulleted_24, ""),
        ToolItem("indent-inc", R.drawable.format_indent_increase_24px, ""),
        ToolItem("indent-dec", R.drawable.format_indent_decrease_24px, ""),
        ToolItem("H1", R.drawable.format_h1_24px, ""),
        ToolItem("H2", R.drawable.format_h2_24px, ""),
        ToolItem("H3", R.drawable.format_h3_24px, ""),
        ToolItem("H4", R.drawable.format_h4_24px, ""),
        ToolItem("H5", R.drawable.format_h5_24px, ""),
    )
    if (canDelete) {
        items += ToolItem("delete", R.drawable.delete_24px, "", color = Color(0xFfCf0000))
    }
    //    ToolItem("code",      R.drawable.rounded_checkbook_24,        ""), TODO
    //    ToolItem("quote",     R.drawable.rounded_checkbook_24,       ""), TODO
    return items
}
