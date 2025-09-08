package com.example.noteapplicationmvvmflow.data.model.todo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    var text: String = "",
    var isCompleted: Boolean = false,
    var position: Int = 0
) : Parcelable
