package com.example.noteapplicationmvvmflow.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val contentType: String?,
    val textContent: String?,
    val audioPath: String?,
    val imagePath: String?,
    val drawingData: String?,
    val todoItems: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
