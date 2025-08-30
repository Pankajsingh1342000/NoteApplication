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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.noteapplicationmvvmflow.data.model.drawing.BrushStyle
import com.example.noteapplicationmvvmflow.databinding.FragmentDrawingBinding
import androidx.core.graphics.toColorInt
import androidx.navigation.fragment.findNavController
import com.example.noteapplicationmvvmflow.R
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DrawingFragment : Fragment() {

    private var _binding: FragmentDrawingBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawingView: DrawingView
    private var drawingFilePath: String? = null

    // ViewModel
    private val drawingViewModel: DrawingViewModel by viewModels()

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
        observeViewModel()
    }

    private fun setupDrawingView() {
        // Set up listeners
        drawingView.setOnDrawListener {
            // Optional: any immediate UI updates
        }

        // Listen for completed paths to send to ViewModel
        drawingView.setOnPathCompletedListener { drawingPath ->
            drawingViewModel.addDrawingPath(drawingPath)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe drawing paths
                launch {
                    drawingViewModel.drawingPaths.collect { paths ->
                        drawingView.updatePaths(paths)
                    }
                }

                // Observe undo/redo state
                launch {
                    drawingViewModel.canUndo.collect { canUndo ->
                        binding.btnUndo.isEnabled = canUndo
                        binding.btnUndo.alpha = if (canUndo) 1.0f else 0.5f
                    }
                }

                launch {
                    drawingViewModel.canRedo.collect { canRedo ->
                        binding.btnRedo.isEnabled = canRedo
                        binding.btnRedo.alpha = if (canRedo) 1.0f else 0.5f
                    }
                }

                // Observe current color
                launch {
                    drawingViewModel.currentColor.collect { color ->
                        setDrawingColor(color)
                    }
                }

                // Observe current brush style
                launch {
                    drawingViewModel.currentBrushStyle.collect { brushStyle ->
                        updateBrushIcon(brushStyle)
                        drawingView.setBrushStyle(brushStyle)
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnUndo.setOnClickListener {
            drawingViewModel.undo()
        }

        binding.btnRedo.setOnClickListener {
            drawingViewModel.redo()
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
                drawingViewModel.setColor(colors[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBrushStyleDialog() {
        val styles = BrushStyle.entries.toTypedArray()
        val styleNames = styles.map { getBrushStyleDisplayName(it) }.toTypedArray()
        val currentStyle = drawingViewModel.currentBrushStyle.value
        val currentIndex = styles.indexOf(currentStyle)

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Brush Style")
            .setSingleChoiceItems(styleNames, currentIndex) { dialog, which ->
                val selectedStyle = styles[which]
                drawingViewModel.setBrushStyle(selectedStyle)
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
                drawingViewModel.clearDrawing()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setDrawingColor(color: Int) {
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
        if (!drawingViewModel.hasContent()) {
            Toast.makeText(context, "Nothing to save", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = drawingView.exportDrawing()
        if (bitmap != null) {
            saveDrawingToFile(bitmap)
        } else {
            Toast.makeText(context, "Failed to export drawing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDrawingToFile(bitmap: Bitmap) {
        try {
            val file = createDrawingFile()
            drawingFilePath = file.absolutePath

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