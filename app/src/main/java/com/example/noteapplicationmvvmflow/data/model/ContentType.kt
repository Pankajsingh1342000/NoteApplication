package com.example.noteapplicationmvvmflow.data.model

import com.example.noteapplicationmvvmflow.R

enum class ContentType(val value: String, val displayName: String, val iconResId: Int) {
    TEXT("text", "Text", R.drawable.ic_text),
    AUDIO("audio", "Audio", R.drawable.ic_audio),
    IMAGE("image", "Image", R.drawable.ic_image),
    DRAWING("drawing", "Drawing", R.drawable.ic_drawing),
    TODO("todo", "Todo", R.drawable.ic_todo)
}