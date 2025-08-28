package com.example.noteapplicationmvvmflow.data.model.drawing

import android.graphics.Paint
import android.graphics.Path

data class DrawingPath(
    val path: Path,
    val paint: Paint
) {
    fun copyPaint(): Paint {
        return Paint(paint)
    }
}