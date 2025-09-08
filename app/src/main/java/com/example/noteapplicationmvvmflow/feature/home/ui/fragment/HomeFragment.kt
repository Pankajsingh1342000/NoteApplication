package com.example.noteapplicationmvvmflow.feature.home.ui.fragment

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.data.model.ContentType
import com.example.noteapplicationmvvmflow.databinding.FragmentHomeBinding
import com.example.noteapplicationmvvmflow.feature.home.adapter.NoteAdapter
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private var isExpanded = false

    // Animations
    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_fab)
    }

    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_fab)
    }

    // Permission launcher - Fixed to handle single permission
    private val imagePermissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("Permission", "Image permission granted: $isGranted")
        if (isGranted) {
            selectImage()
        } else {
            showPermissionDeniedDialog()
        }
    }

    // Modern photo picker
    private val pickImageLauncher = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            val imagePath = copyImageToAppStorage(uri)
            navigateToAddFragment(ContentType.IMAGE, imagePath.toString())
        } else {
            Log.d("PhotoPicker", "No media selected, trying fallback")
            selectImageFallback()
        }
    }

    // Fallback image picker for older devices or if photo picker fails
    private val legacyImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                Log.d("LegacyPicker", "Selected URI: $uri")
                val imagePath = copyImageToAppStorage(uri)
                navigateToAddFragment(ContentType.IMAGE, imagePath.toString())
            } else {
                Log.d("LegacyPicker", "No URI in result")
                Toast.makeText(context, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("LegacyPicker", "Result cancelled or failed")
        }
        shrinkFab()
    }

    private fun copyImageToAppStorage(uri: Uri): String? {

        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null

            // Create app folder + file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMAGE_$timeStamp.jpg"
            val storageDir = requireContext().getExternalFilesDir(null)
            val file = File(storageDir, fileName)

            // Ensure directory exists
            file.parentFile?.mkdirs()

            // Copy input stream → file
            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("ImageCopy", "Image saved to: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ImageCopy", "Failed to copy image", e)
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeNotes()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        recyclerView = binding.rvNotes
        recyclerView.setHasFixedSize(false)
        val lm = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        }
        recyclerView.layoutManager = lm
        adapter = NoteAdapter(
            onDeleteClick = { note -> noteViewModel.delete(note) },
            onNoteClick = { note, bgColor ->
                val action = HomeFragmentDirections.actionHomeFragmentToEditFragment(
                    id = note.id,
                    title = note.title,
                    textContent = note.textContent ?: "",
                    contentType = note.contentType ?: "text",
                    audioPath = note.audioPath ?: "",
                    imagePath = note.imagePath ?: "",
                    drawingPath = note.drawingPath ?: "",
                    todoItems = note.todoItems ?: "",
                    bgColor = bgColor
                )
                try {
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Navigation failed", e)
                }
//                findNavController().navigate(action)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun observeNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                noteViewModel.allNote.collect { notes ->
                    adapter.setNotes(notes)
                }
            }
        }
    }

    private fun setupClickListeners() {
        fab = binding.fabAdd
        binding.transparentBg.setOnClickListener { shrinkFab() }
        fab.setOnClickListener {
            if (isExpanded) shrinkFab() else expandFab()
        }

        binding.fabText.setOnClickListener { navigateToAddFragment(ContentType.TEXT, "") }
        binding.fabAudio.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAudioFragment())
            shrinkFab()
        }

        binding.fabImage.setOnClickListener { checkImagePermission() }
        binding.fabDrawing.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToDrawingFragment())
            shrinkFab()
        }

        binding.fabList.setOnClickListener { navigateToAddFragment(ContentType.TODO, "") }
    }

    private fun shrinkFab() {
        fadeOutTransparentBg()
        listOf(
            binding.fabText,
            binding.fabList,
            binding.fabAudio,
            binding.fabImage,
            binding.fabDrawing
        ).forEach { it.startAnimation(toBottomFabAnim) }
        isExpanded = false
    }

    private fun expandFab() {
        fadeInTransparentBg()
        listOf(
            binding.fabText,
            binding.fabList,
            binding.fabAudio,
            binding.fabImage,
            binding.fabDrawing
        ).forEach { it.startAnimation(fromBottomFabAnim) }
        isExpanded = true
    }

    private fun fadeInTransparentBg() {
        binding.transparentBg.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
        }
    }

    private fun fadeOutTransparentBg() {
        binding.transparentBg.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.transparentBg.visibility = View.GONE
                }
            })
    }

    private fun navigateToAddFragment(contentType: ContentType, imagePath: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToAddFragment(
            contentType = contentType.value,
            imagePath = imagePath
        )
        findNavController().navigate(action)
        shrinkFab()
    }

    private fun checkImagePermission() {
        Log.d("Permission", "Checking permission for Android API: ${Build.VERSION.SDK_INT}")

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        Log.d("Permission", "Required permission: $permission")

        val permissionStatus = ContextCompat.checkSelfPermission(requireContext(), permission)
        Log.d("Permission", "Permission status: $permissionStatus (GRANTED=${PackageManager.PERMISSION_GRANTED})")

        when {
            permissionStatus == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Permission", "Permission already granted")
                selectImage()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Log.d("Permission", "Should show rationale")
                showPermissionRationale(permission)
            }
            else -> {
                Log.d("Permission", "Requesting permission: $permission")
                imagePermissionRequestLauncher.launch(permission)
            }
        }
    }

    private fun showPermissionRationale(permission: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app needs permission to access your photos to add them to your notes.")
            .setPositiveButton("Grant Permission") { _, _ ->
                imagePermissionRequestLauncher.launch(permission)
            }
            .setNegativeButton("Cancel") { _, _ ->
                shrinkFab()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("To select images, please grant the photo permission in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                shrinkFab()
            }
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("Permission", "Failed to open app settings", e)
            Toast.makeText(context, "Please grant permission in Settings > Apps", Toast.LENGTH_LONG).show()
        }
        shrinkFab()
    }

    private fun selectImage() {
        try {
            Log.d("ImageSelection", "Attempting to select image")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use photo picker for API 33+ (including API 36)
                pickImageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            } else {
                // Use legacy method for older versions
                selectImageFallback()
            }
        } catch (e: Exception) {
            Log.e("ImageSelection", "Photo picker failed, using fallback", e)
            selectImageFallback()
        }
    }

    private fun selectImageFallback() {
        try {
            Log.d("ImageSelection", "Using fallback image picker")
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            legacyImagePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("ImageSelection", "Fallback picker also failed", e)
            Toast.makeText(context, "Unable to open image picker", Toast.LENGTH_SHORT).show()
            shrinkFab()
        }
    }
}
