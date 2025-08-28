package com.example.noteapplicationmvvmflow.feature.drawing

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.noteapplicationmvvmflow.data.model.drawing.BrushStyle
import com.example.noteapplicationmvvmflow.databinding.FragmentDrawingBinding
import androidx.core.graphics.toColorInt
import androidx.navigation.fragment.findNavController
import com.example.noteapplicationmvvmflow.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DrawingFragment : Fragment() {

    private var _binding: FragmentDrawingBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawingView: DrawingView
    private var currentColor = Color.BLACK
    private var drawingFilePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDrawingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawingView = binding.drawingView
        setupDrawingView()
        setupButtons()
        handleBackPress()

        // Initialize button states
        updateUndoRedoButtons()
        setDrawingColor(drawingView.getCurrentColor())
        updateBrushIcon(drawingView.getCurrentBrushStyle())
    }

    private fun setupDrawingView() {
        drawingView.setOnDrawListener {
            updateUndoRedoButtons()
        }
    }

    private fun setupButtons() {
        binding.btnUndo.setOnClickListener {
            if (drawingView.undo()) {
                updateUndoRedoButtons()
            }
        }

        binding.btnRedo.setOnClickListener {
            if (drawingView.redo()) {
                updateUndoRedoButtons()
            }
        }

        binding.btnBrushColor.setOnClickListener {
            showColorPicker()
        }

        binding.btnBrushStyle.setOnClickListener {
            showBrushStyleDialog()
        }

        binding.btnClear.setOnClickListener {
            showClearConfirmation()
        }

        binding.btnSave.setOnClickListener {
            saveDrawing()
        }
    }

    private fun updateUndoRedoButtons() {
        val canUndo = drawingView.canUndo()
        val canRedo = drawingView.canRedo()

        binding.btnUndo.isEnabled = canUndo
        binding.btnUndo.alpha = if (canUndo) 1.0f else 0.5f

        binding.btnRedo.isEnabled = canRedo
        binding.btnRedo.alpha = if (canRedo) 1.0f else 0.5f

        Log.d("DrawingFragment", "Undo: $canUndo, Redo: $canRedo")
    }

    private fun showColorPicker() {
        val colors = arrayOf(
            Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY,
            "#FF9800".toColorInt(), "#9C27B0".toColorInt(),
            "#4CAF50".toColorInt(), "#FF5722".toColorInt()
        )

        val colorNames = arrayOf(
            "Black", "Red", "Green", "Blue",
            "Yellow", "Cyan", "Magenta", "Gray",
            "Orange", "Purple", "Light Green", "Deep Orange"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Color")
            .setItems(colorNames) { _, which ->
                setDrawingColor(colors[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBrushStyleDialog() {
        val styles = BrushStyle.entries.toTypedArray()
        val styleNames = styles.map { getBrushStyleDisplayName(it) }.toTypedArray()

        // Get current brush style to show which one is selected
        val currentStyle = drawingView.getCurrentBrushStyle()
        val currentIndex = styles.indexOf(currentStyle)

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Brush Style")
            .setSingleChoiceItems(styleNames, currentIndex) { dialog, which ->
                val selectedStyle = styles[which]
                drawingView.setBrushStyle(selectedStyle)
                updateBrushIcon(selectedStyle)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showClearConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Drawing")
            .setMessage("Are you sure you want to clear the entire drawing?")
            .setPositiveButton("Clear") { _, _ ->
                drawingView.clearDrawing()
                updateUndoRedoButtons()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setDrawingColor(color: Int) {
        currentColor = color
        drawingView.setBrushColor(color)

        // Update color button background to show current color
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(4, Color.GRAY)
        }
        binding.btnBrushColor.background = drawable
    }

    private fun updateBrushIcon(brushStyle: BrushStyle) {
        val iconRes = when (brushStyle) {
            BrushStyle.PENCIL -> R.drawable.ic_pencil
            BrushStyle.BRUSH -> R.drawable.ic_brush
            BrushStyle.MARKER -> R.drawable.ic_marker
            BrushStyle.CALLIGRAPHY -> R.drawable.ic_calligraphy
        }

        binding.btnBrushStyle.setImageResource(iconRes)
    }

    private fun getBrushStyleDisplayName(brushStyle: BrushStyle): String {
        return when (brushStyle) {
            BrushStyle.PENCIL -> "Pencil"
            BrushStyle.BRUSH -> "Brush"
            BrushStyle.MARKER -> "Marker"
            BrushStyle.CALLIGRAPHY -> "Calligraphy"
        }
    }

    private fun saveDrawing() {
        val bitmap = drawingView.exportDrawing()
        if (bitmap != null) {
            saveDrawingToFile(bitmap)
        } else {
            Toast.makeText(context, "Nothing to save", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDrawingToFile(bitmap: Bitmap) {
        try {
            val file = createDrawingFile()
            drawingFilePath = file.absolutePath

            // Ensure parent directory exists
            file.parentFile?.mkdirs()
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            Log.d("DrawingFragment", "Drawing saved: ${file.absolutePath}")
            navigateToAddFragment()

        } catch (e: Exception) {
            Log.e("DrawingFragment", "Failed to save drawing", e)
            Toast.makeText(context, "Failed to save drawing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createDrawingFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "DRAWING_$timeStamp.png"
        val storageDir = requireContext().getExternalFilesDir(null)
        return File(storageDir, fileName)
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }

    private fun navigateToAddFragment() {
        val action = DrawingFragmentDirections.actionDrawingFragmentToAddFragment(
            contentType = "drawing",
            drawingPath = drawingFilePath!!
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}