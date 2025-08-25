package com.example.noteapplicationmvvmflow.feature.home.ui.fragment

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

    // Permission launcher
    private val imagePermissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectImage()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            navigateToAddFragment(ContentType.IMAGE, uri.toString())
        } else {
            Log.d("PhotoPicker", "No media selected")
            shrinkFab()
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
            onDeleteClick = { note -> noteViewModel.delete(note)
                            },
            onNoteClick = { note, bgColor ->
                val action = HomeFragmentDirections.actionHomeFragmentToEditFragment(
                    id = note.id,
                    title = note.title,
                    textContent = note.textContent ?: "",
                    contentType = note.contentType ?: "text",
                    audioPath = note.audioPath ?: "",
                    imagePath = note.imagePath ?: "",
                    bgColor = bgColor
                )
                findNavController().navigate(action)
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
        binding.fabDrawing.setOnClickListener { navigateToAddFragment(ContentType.DRAWING, "") }
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
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                selectImage()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(context, "Image Permission is Required", Toast.LENGTH_SHORT).show()
            }
            else -> {
                imagePermissionRequestLauncher.launch(permission)
            }
        }
    }

    private fun selectImage() {
        pickImageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

}
