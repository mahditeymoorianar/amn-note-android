package com.teymoorianar.amnnote.ui.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teymoorianar.amnnote.domain.model.Note
import com.teymoorianar.amnnote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialNoteId: Long = savedStateHandle.get<Long>(NoteActivity.EXTRA_NOTE_ID) ?: 0L

    private val _state = MutableStateFlow(
        NoteEditorState(
            noteId = initialNoteId
        )
    )
    val state: StateFlow<NoteEditorState> = _state

    init {
        if (initialNoteId != 0L) {
            loadNote(initialNoteId)
        }
    }

    fun onTitleChange(title: String) {
        _state.update {
            it.copy(title = title, isSaved = false, isDeleted = false)
        }
    }

    fun onContentChange(content: String) {
        _state.update {
            it.copy(content = content, isSaved = false, isDeleted = false)
        }
    }

    private fun loadNote(noteId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val note = repository.getNote(noteId)
                if (note != null) {
                    _state.update {
                        it.copy(
                            noteId = note.id,
                            title = note.title,
                            content = note.content,
                            isEncrypted = note.isEncrypted,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Note not found"
                        )
                    }
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Unable to load note"
                    )
                }
            }
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            val current = _state.value
            if (current.title.isBlank() && current.content.isBlank()) {
                _state.update { it.copy(errorMessage = "Cannot save empty note") }
                return@launch
            }
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val now = System.currentTimeMillis()
                val note = Note(
                    id = current.noteId,
                    title = current.title,
                    content = current.content,
                    isEncrypted = current.isEncrypted,
                    createdAt = if (current.noteId == 0L && current.createdAt == 0L) now else current.createdAt,
                    updatedAt = now
                )
                val savedId = repository.saveNote(note, current.isEncrypted)
                _state.update {
                    it.copy(
                        noteId = savedId,
                        createdAt = note.createdAt,
                        updatedAt = now,
                        isLoading = false,
                        isSaved = true
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Unable to save note"
                    )
                }
            }
        }
    }

    fun deleteNote() {
        val noteId = _state.value.noteId
        if (noteId == 0L) {
            _state.update { it.copy(errorMessage = "Nothing to delete") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.deleteNote(noteId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isDeleted = true
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Unable to delete note"
                    )
                }
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(errorMessage = null) }
    }
}

data class NoteEditorState(
    val noteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val isEncrypted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)
