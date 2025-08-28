package com.example.noteapplicationmvvmflow.feature.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.noteapplicationmvvmflow.data.model.drawing.BrushStyle
import com.example.noteapplicationmvvmflow.data.model.drawing.DrawingPath

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private var currentPath = Path()
    private var currentPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val drawingPaths = mutableListOf<DrawingPath>()
    private val undoPaths = mutableListOf<DrawingPath>()

    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    private var currentBrushStyle = BrushStyle.PENCIL
    private var currentColor = Color.BLACK
    private var onDrawListener: (() -> Unit)? = null

    init {
        setupPaint()
    }

    private fun setupPaint() {
        currentBrushStyle.applyToPaint(currentPaint)
        currentPaint.color = currentColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
        drawCanvas?.drawColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvasBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }
        canvas.drawPath(currentPath, currentPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
            }

            MotionEvent.ACTION_UP -> {
                drawCanvas?.drawPath(currentPath, currentPaint)
                drawingPaths.add(DrawingPath(Path(currentPath), Paint(currentPaint)))
                undoPaths.clear()
                currentPath.reset()

                Log.d("DrawingView", "Drawing added. Total paths: ${drawingPaths.size}")
                onDrawListener?.invoke()
            }
        }

        invalidate()
        return true
    }

    fun undo(): Boolean {
        return if (drawingPaths.isNotEmpty()) {
            val lastPath = drawingPaths.removeAt(drawingPaths.size - 1)
            undoPaths.add(lastPath)
            redrawCanvas()
            invalidate()
            Log.d("DrawingView", "Undo: Paths=${drawingPaths.size}, Undo stack=${undoPaths.size}")
            true
        } else {
            false
        }
    }

    fun redo(): Boolean {
        return if (undoPaths.isNotEmpty()) {
            val pathToRedo = undoPaths.removeAt(undoPaths.size - 1)
            drawingPaths.add(pathToRedo)
            redrawCanvas()
            invalidate()
            Log.d("DrawingView", "Redo: Paths=${drawingPaths.size}, Undo stack=${undoPaths.size}")
            true
        } else {
            false
        }
    }

    private fun redrawCanvas() {
        drawCanvas?.drawColor(Color.WHITE)
        for (drawingPath in drawingPaths) {
            drawCanvas?.drawPath(drawingPath.path, drawingPath.copyPaint())
        }
    }

    fun canUndo(): Boolean = drawingPaths.isNotEmpty()
    fun canRedo(): Boolean = undoPaths.isNotEmpty()

    fun clearDrawing() {
        drawingPaths.clear()
        undoPaths.clear()
        currentPath.reset()
        drawCanvas?.drawColor(Color.WHITE)
        invalidate()
        Log.d("DrawingView", "Drawing cleared")
    }

    fun setBrushColor(color: Int) {
        currentColor = color
        currentPaint.color = color
    }

    fun setBrushStyle(style: BrushStyle) {
        currentBrushStyle = style
        style.applyToPaint(currentPaint)
        currentPaint.color = currentColor
    }

    fun exportDrawing(): Bitmap? {
        return canvasBitmap?.copy(canvasBitmap!!.config!!, false)
    }

    fun setOnDrawListener(listener: () -> Unit) {
        onDrawListener = listener
    }

    fun getCurrentColor(): Int = currentColor
    fun getCurrentBrushStyle(): BrushStyle = currentBrushStyle
}