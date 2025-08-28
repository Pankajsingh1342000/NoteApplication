package com.example.noteapplicationmvvmflow.data.model.drawing

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PathEffect

enum class BrushStyle(
    val strokeWidth: Float,
    val pathEffect: PathEffect? = null,
    val strokeCap: Paint.Cap = Paint.Cap.ROUND,
    val strokeJoin: Paint.Join = Paint.Join.ROUND
) {
    PENCIL(8f),
    BRUSH(15f),
    MARKER(25f, null, Paint.Cap.SQUARE),
    CALLIGRAPHY(35f, null, Paint.Cap.SQUARE, Paint.Join.MITER);

    fun applyToPaint(paint: Paint) {
        paint.strokeWidth = strokeWidth
        paint.strokeCap = strokeCap
        paint.pathEffect = pathEffect
        paint.strokeJoin = strokeJoin
    }
}