package com.example.noteapplicationmvvmflow.feature.edit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.databinding.FragmentEditBinding
import com.example.noteapplicationmvvmflow.feature.audio.AudioPlayerView
import com.example.noteapplicationmvvmflow.feature.image.ImagePreview
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val args: EditFragmentArgs by navArgs()
    private var audioDeleted = false
    private var imageDeleted = false
    private var audioPlayerView: AudioPlayerView? = null
    private var imagePreviewView: ImagePreview? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateFields()
        updateUIForContentType()
        handleBackPress()
    }

    private fun populateFields() {
        binding.etTitle.setText(args.title)
        binding.etDescription.setText(args.textContent)
    }

    private fun updateUIForContentType() {
        when (args.contentType) {
            "text" -> {
                binding.etDescription.hint = "Enter your note content here..."
                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 3
                binding.etDescription.maxLines = 10
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE
            }
            "audio" -> {
                binding.etDescription.hint = "Description"
                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                imagePreviewView?.visibility = View.GONE

                setupAudioPlayer()
            }
            "image" -> {
                binding.etDescription.hint = "Description"
                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE

                setupImagePreview()

            }
            "drawing" -> {
                binding.etDescription.hint = "Drawing will be displayed here..."
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE

            }
            "todo" -> {
                binding.etDescription.hint = "Todo list will be displayed here..."
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE

            }
            else -> {
                binding.etDescription.hint = "Enter your note content here..."
                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 3
                binding.etDescription.maxLines = 10
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE
            }
        }
    }

    private fun setupAudioPlayer() {
        binding.audioPlayerContainer.removeAllViews()

        audioPlayerView = AudioPlayerView(requireContext())
        binding.audioPlayerContainer.addView(audioPlayerView)
        binding.audioPlayerContainer.visibility = View.VISIBLE

        audioPlayerView?.onAudioDeleted = {
            onAudioDeleted()
        }

        if (args.audioPath.isNotEmpty()) {
            audioPlayerView?.setAudioPath(args.audioPath)
        }
    }

    private fun setupImagePreview() {
        binding.imagePreviewContainer.removeAllViews()
        imagePreviewView = ImagePreview(requireContext())
        binding.imagePreviewContainer.addView(imagePreviewView)
        binding.imagePreviewContainer.visibility = View.VISIBLE

        imagePreviewView?.onImageDeleted = {
            onImageDeleted()
        }

        if (args.imagePath.isNotEmpty()) {
            imagePreviewView?.setImage(args.imagePath)
        }
    }

    private fun onAudioDeleted() {
        audioDeleted = true

        binding.audioPlayerContainer.visibility = View.GONE
        binding.etDescription.isEnabled = true
        binding.etDescription.minLines = 3
        binding.etDescription.maxLines = 10
    }

    private fun onImageDeleted() {
        imageDeleted = true

        binding.imagePreviewContainer.visibility = View.GONE
        binding.etDescription.isEnabled = true
        binding.etDescription.minLines = 3
        binding.etDescription.maxLines = 10
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                updateNoteAndNavigateBack()
            }
        })
    }

    private fun updateNoteAndNavigateBack() {
        val newNote = createUpdatedNote()

        val oldNote = Note(
            id = args.id,
            title = args.title,
            contentType = args.contentType,
            textContent = args.textContent,
            audioPath = args.audioPath,
            imagePath = args.imagePath,
            drawingData = null,
            todoItems = null,
            createdAt = 0L,
            updatedAt = 0L
        )

        if (newNote != oldNote) {
            newNote.updatedAt = System.currentTimeMillis()
            noteViewModel.update(newNote)
        }

        findNavController().popBackStack()
    }

    private fun createUpdatedNote(): Note {
        return Note(
            id = args.id,
            title = binding.etTitle.text.toString().trim(),
            contentType = if (audioDeleted || imageDeleted) "text" else args.contentType,
            textContent = binding.etDescription.text.toString().trim(),
            audioPath = if (audioDeleted) null else audioPlayerView?.getAudioPath() ?: args.audioPath,
            imagePath = if (imageDeleted) null else imagePreviewView?.getImagePath() ?: args.imagePath,
            drawingData = null, // TODO: Add drawing data handling
            todoItems = null, // TODO: Add todo items handling
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayerView?.release()
        audioPlayerView = null
    }
}