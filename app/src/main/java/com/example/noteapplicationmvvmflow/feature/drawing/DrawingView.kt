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

    // Remove local path storage - now managed by ViewModel
    private var drawingPaths = listOf<DrawingPath>()
    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    private var currentBrushStyle = BrushStyle.PENCIL
    private var currentColor = Color.BLACK

    // Callbacks to communicate with Fragment/ViewModel
    private var onDrawListener: (() -> Unit)? = null
    private var onPathCompleted: ((DrawingPath) -> Unit)? = null

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

        // Redraw existing paths after size change
        redrawAllPaths()
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
                // Draw the path to canvas
                drawCanvas?.drawPath(currentPath, currentPaint)

                // Notify ViewModel about completed path
                val completedPath = DrawingPath(Path(currentPath), Paint(currentPaint))
                onPathCompleted?.invoke(completedPath)

                // Reset current path
                currentPath.reset()

                Log.d("DrawingView", "Path completed and sent to ViewModel")
                onDrawListener?.invoke()
            }
        }

        invalidate()
        return true
    }

    // Update paths from ViewModel
    fun updatePaths(paths: List<DrawingPath>) {
        drawingPaths = paths
        redrawAllPaths()
        invalidate()
    }

    private fun redrawAllPaths() {
        drawCanvas?.drawColor(Color.WHITE)
        for (drawingPath in drawingPaths) {
            drawCanvas?.drawPath(drawingPath.path, drawingPath.copyPaint())
        }
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

    fun setOnPathCompletedListener(listener: (DrawingPath) -> Unit) {
        onPathCompleted = listener
    }

    fun getCurrentColor(): Int = currentColor
    fun getCurrentBrushStyle(): BrushStyle = currentBrushStyle
}
