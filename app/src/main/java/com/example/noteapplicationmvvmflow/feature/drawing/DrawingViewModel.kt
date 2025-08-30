package com.example.noteapplicationmvvmflow.feature.drawing

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapplicationmvvmflow.data.model.drawing.BrushStyle
import com.example.noteapplicationmvvmflow.data.model.drawing.DrawingPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor() : ViewModel() {

    // Drawing state
    private val _drawingPaths = MutableStateFlow<List<DrawingPath>>(emptyList())
    val drawingPaths: StateFlow<List<DrawingPath>> = _drawingPaths.asStateFlow()

    private val _undoPaths = MutableStateFlow<List<DrawingPath>>(emptyList())

    // Current drawing settings
    private val _currentColor = MutableStateFlow(Color.BLACK)
    val currentColor: StateFlow<Int> = _currentColor.asStateFlow()

    private val _currentBrushStyle = MutableStateFlow(BrushStyle.PENCIL)
    val currentBrushStyle: StateFlow<BrushStyle> = _currentBrushStyle.asStateFlow()

    // UI state
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Drawing operations
    fun addDrawingPath(drawingPath: DrawingPath) {
        viewModelScope.launch {
            val currentPaths = _drawingPaths.value.toMutableList()
            currentPaths.add(drawingPath)
            _drawingPaths.value = currentPaths

            // Clear redo stack when new drawing is added
            _undoPaths.value = emptyList()

            updateUndoRedoState()
        }
    }

    fun undo(): Boolean {
        val currentPaths = _drawingPaths.value.toMutableList()
        val currentUndoPaths = _undoPaths.value.toMutableList()

        return if (currentPaths.isNotEmpty()) {
            val lastPath = currentPaths.removeAt(currentPaths.size - 1)
            currentUndoPaths.add(lastPath)

            _drawingPaths.value = currentPaths
            _undoPaths.value = currentUndoPaths

            updateUndoRedoState()
            true
        } else {
            false
        }
    }

    fun redo(): Boolean {
        val currentPaths = _drawingPaths.value.toMutableList()
        val currentUndoPaths = _undoPaths.value.toMutableList()

        return if (currentUndoPaths.isNotEmpty()) {
            val pathToRedo = currentUndoPaths.removeAt(currentUndoPaths.size - 1)
            currentPaths.add(pathToRedo)

            _drawingPaths.value = currentPaths
            _undoPaths.value = currentUndoPaths

            updateUndoRedoState()
            true
        } else {
            false
        }
    }

    fun clearDrawing() {
        viewModelScope.launch {
            _drawingPaths.value = emptyList()
            _undoPaths.value = emptyList()
            updateUndoRedoState()
        }
    }

    fun setColor(color: Int) {
        _currentColor.value = color
    }

    fun setBrushStyle(style: BrushStyle) {
        _currentBrushStyle.value = style
    }

    private fun updateUndoRedoState() {
        _canUndo.value = _drawingPaths.value.isNotEmpty()
        _canRedo.value = _undoPaths.value.isNotEmpty()
    }


    // Check if there's any content to save
    fun hasContent(): Boolean = _drawingPaths.value.isNotEmpty()
}
