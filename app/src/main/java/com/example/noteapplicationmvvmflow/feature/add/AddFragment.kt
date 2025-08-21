package com.example.noteapplicationmvvmflow.feature.add

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.data.helper.NoteHelper
import com.example.noteapplicationmvvmflow.databinding.FragmentAddBinding
import com.example.noteapplicationmvvmflow.feature.audio.AudioPlayerView
import com.example.noteapplicationmvvmflow.feature.image.ImagePreview
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val args: AddFragmentArgs by navArgs()
    private var audioPlayerView: AudioPlayerView? = null
    private var imagePreviewView: ImagePreview? = null
    private var audioDeleted = false
    private var imageDeleted = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUIForContentType()
        handleBackPress()
    }

    private fun updateUIForContentType() {
        when (args.contentType) {
            "text" -> {
                binding.etDescription.hint = "Enter your note content here..."
                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 3
                binding.etDescription.maxLines = 10
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE
            }
            "audio" -> {
                binding.etDescription.hint = "Enter Description"
                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE

                // Setup audio player
                setupAudioPlayer()
            }
            "image" -> {
                binding.etDescription.hint = "Enter Description"
                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE

                setupImagePreview()
            }
            "drawing" -> {
                binding.etDescription.hint = "Drawing canvas will be added here"
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE
            }
            "todo" -> {
                binding.etDescription.hint = "Todo list will be added here"
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE
            }
        }
    }

    private fun setupAudioPlayer() {
        Log.d("AddFragment", "Setting up audio player")
        Log.d("AddFragment", "args.audioPath: ${args.audioPath}")
        Log.d("AddFragment", "arguments audioPath: ${arguments?.getString("audioPath")}")

        // Clear any existing audio player
        binding.audioPlayerContainer.removeAllViews()

        // Create new audio player
        audioPlayerView = AudioPlayerView(requireContext())
        binding.audioPlayerContainer.addView(audioPlayerView)
        binding.audioPlayerContainer.visibility = View.VISIBLE

        audioPlayerView?.onAudioDeleted = {
            onAudioDeleted()
        }

        if (args.audioPath.isNotEmpty()) {
            Log.d("AddFragment", "Setting audio path from args: ${args.audioPath}")
            audioPlayerView?.setAudioPath(args.audioPath)
        } else {

            val audioPath = arguments?.getString("audioPath")
            if (!audioPath.isNullOrEmpty()) {
                Log.d("AddFragment", "Setting audio path from arguments: $audioPath")
                audioPlayerView?.setAudioPath(audioPath)
            } else {
                Log.e("AddFragment", "No audio path found!")
            }
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
        } else {
            val imagePath = arguments?.getString("imagePath")
            if (!imagePath.isNullOrEmpty()) {
                imagePreviewView?.setImage(imagePath)
            } else {
                Log.e("AddFragment", "No Image path found!")
            }
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
                saveNoteAndNavigateBack()
            }
        })
    }

    private fun saveNoteAndNavigateBack() {
        val note = createNoteFromInput()
        if (shouldSaveNote(note)) {
            saveNote(note)
        }else {
            audioPlayerView?.deleteAudio()
            imagePreviewView?.deleteImage()
        }
        findNavController().popBackStack()
    }

    private fun createNoteFromInput(): Note {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etDescription.text.toString().trim()

        return when {
            audioDeleted -> {
                NoteHelper.createTextNote(title, content)
            }
            imageDeleted -> {
                NoteHelper.createTextNote(title, content)
            }
            args.contentType == "text" -> NoteHelper.createTextNote(title, content)
            args.contentType == "audio" -> {
                val audioPath = if (args.audioPath.isNotEmpty()) args.audioPath else arguments?.getString("audioPath") ?: ""
                NoteHelper.createAudioNote(title, audioPath, content)
            }
            args.contentType == "image" -> {
                val imagePath = if (args.imagePath.isNotEmpty()) args.imagePath else arguments?.getString("imagePath") ?: ""
                NoteHelper.createImageNote(title, imagePath, content)
            }
            args.contentType == "drawing" -> NoteHelper.createDrawingNote(title, "")
            args.contentType == "todo" -> NoteHelper.createTodoNote(title, "")
            else -> NoteHelper.createTextNote(title, content)
        }
    }

    private fun shouldSaveNote(note: Note): Boolean {
        return when {
            audioDeleted -> note.title.isNotEmpty() || (note.textContent?.isNotEmpty() == true)
            imageDeleted -> note.title.isNotEmpty() || (note.textContent?.isNotEmpty() == true)
            args.contentType == "text" -> note.title.isNotEmpty() || (note.textContent?.isNotEmpty() == true)
            args.contentType == "audio" -> note.title.isNotEmpty()
            args.contentType == "image" -> note.title.isNotEmpty()
            else -> note.title.isNotEmpty()
        }
    }

    private fun saveNote(note: Note) {
        noteViewModel.insert(note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayerView?.release()
        audioPlayerView = null
    }
}