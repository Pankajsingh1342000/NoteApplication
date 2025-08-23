package com.example.noteapplicationmvvmflow.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
): ViewModel() {
    private val _allNotes = MutableStateFlow<List<Note>>(emptyList())
    val allNote: StateFlow<List<Note>> = _allNotes

    init {
        viewModelScope.launch {
            repository.getAllNotes.collect {
                _allNotes.value = it
            }
        }
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        if (note.contentType == "audio"){
            deleteAudio(note.audioPath)
        }
        repository.delete(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    private suspend fun deleteAudio(audioPath: String?) {
        withContext(Dispatchers.IO) {
            try {
                audioPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e("Audio Player View", "Error deleting file", e)
            }
        }
    }


}