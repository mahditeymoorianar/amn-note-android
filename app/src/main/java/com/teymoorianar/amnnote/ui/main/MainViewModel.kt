package com.teymoorianar.amnnote.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teymoorianar.amnnote.domain.model.Note
import com.teymoorianar.amnnote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private var reorderDirty = false

    init {
        refreshNotes()
    }

    private fun refreshNotes() {
        viewModelScope.launch {
            _notes.value = noteRepository.getNotes()
        }
    }

    fun moveNote(fromIndex: Int, toIndex: Int) {
        _notes.update { current ->
            if (fromIndex !in current.indices || toIndex !in 0..current.size || fromIndex == toIndex) {
                return@update current
            }
            reorderDirty = true
            current.toMutableList().apply {
                val item = removeAt(fromIndex)
                val destination = toIndex.coerceIn(0, size)
                add(destination, item)
            }
        }
    }

    fun onDragFinished() {
        if (!reorderDirty) return
        reorderDirty = false
        val orderedIds = notes.value.map { it.id }
        if (orderedIds.isEmpty()) return
        viewModelScope.launch {
            try {
                noteRepository.reorderNotes(orderedIds)
            } finally {
                refreshNotes()
            }
        }
    }
}
