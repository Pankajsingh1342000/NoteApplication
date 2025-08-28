package com.example.noteapplicationmvvmflow.data.helper

import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.data.model.ContentType

object NoteHelper {

    fun createTextNote(title: String, textContent: String): Note {
        return Note(
            title = title,
            contentType = ContentType.TEXT.value,
            textContent = textContent,
            audioPath = null,
            imagePath = null,
            drawingPath = null,
            todoItems = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun createAudioNote(title: String, audioPath: String, textContent: String? = null): Note {
        return Note(
            title = title,
            contentType = ContentType.AUDIO.value,
            textContent = textContent,
            audioPath = audioPath,
            imagePath = null,
            drawingPath = null,
            todoItems = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun createImageNote(title: String, imagePath: String,textContent: String? = null): Note {
        return Note(
            title = title,
            contentType = ContentType.IMAGE.value,
            textContent = textContent,
            audioPath = null,
            imagePath = imagePath,
            drawingPath = null,
            todoItems = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun createDrawingNote(title: String, drawingPath: String,textContent: String? = null): Note {
        return Note(
            title = title,
            contentType = ContentType.DRAWING.value,
            textContent = textContent,
            audioPath = null,
            imagePath = null,
            drawingPath = drawingPath,
            todoItems = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun createTodoNote(title: String, todoItems: String,textContent: String? = null): Note {
        return Note(
            title = title,
            contentType = ContentType.TODO.value,
            textContent = null,
            audioPath = null,
            imagePath = null,
            drawingPath = null,
            todoItems = todoItems,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}