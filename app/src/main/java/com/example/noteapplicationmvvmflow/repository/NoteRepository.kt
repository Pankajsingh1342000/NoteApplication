package com.example.noteapplicationmvvmflow.repository

import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.data.db.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NoteRepository @Inject constructor(
    private val dao: NoteDao
) {

    val getAllNotes : Flow<List<Note>> = dao.getAllNotes()

    suspend fun insert(note: Note) = dao.insert(note)
    suspend fun update(note: Note) = dao.update(note)
    suspend fun delete(note: Note) = dao.delete(note)
}