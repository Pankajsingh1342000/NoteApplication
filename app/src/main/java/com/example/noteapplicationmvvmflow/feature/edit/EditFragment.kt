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
import com.example.noteapplicationmvvmflow.data.model.ContentType
import com.example.noteapplicationmvvmflow.databinding.FragmentEditBinding
import com.example.noteapplicationmvvmflow.feature.audio.AudioPlayerView
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val args: EditFragmentArgs by navArgs()
    private var audioDeleted = false

    // Audio player for audio notes
    private var audioPlayerView: AudioPlayerView? = null

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

                // Setup audio player
                setupAudioPlayer()
            }
            "image" -> {
                binding.etDescription.hint = "Image will be displayed here..."
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE

                // TODO: Setup image display
            }
            "drawing" -> {
                binding.etDescription.hint = "Drawing will be displayed here..."
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE

                // TODO: Setup drawing display
            }
            "todo" -> {
                binding.etDescription.hint = "Todo list will be displayed here..."
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
                binding.etDescription.visibility = View.VISIBLE
                audioPlayerView?.visibility = View.GONE

                // TODO: Setup todo list display
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

        // Create new audio player
        audioPlayerView = AudioPlayerView(requireContext())
        binding.audioPlayerContainer.addView(audioPlayerView)
        binding.audioPlayerContainer.visibility = View.VISIBLE

        audioPlayerView?.onAudioDeleted = {
            onAudioDeleted()
        }

        // Set audio path from Safe Args
        if (args.audioPath.isNotEmpty()) {
            audioPlayerView?.setAudioPath(args.audioPath)
        }
    }

    private fun onAudioDeleted() {
        audioDeleted = true

        binding.audioPlayerContainer.visibility = View.GONE
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
        val note = createUpdatedNote()
        if (shouldUpdateNote(note)) {
            updateNote(note)
        }
        findNavController().popBackStack()
    }

    private fun createUpdatedNote(): Note {
        return Note(
            id = args.id,
            title = binding.etTitle.text.toString().trim(),
            contentType = if (audioDeleted) "text" else args.contentType,
            textContent = binding.etDescription.text.toString().trim(),
            audioPath = if (audioDeleted) null else args.audioPath,
            imagePath = null, // TODO: Add image path handling
            drawingData = null, // TODO: Add drawing data handling
            todoItems = null, // TODO: Add todo items handling
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun shouldUpdateNote(note: Note): Boolean {
        return when {
            audioDeleted -> note.title.isNotEmpty() || (note.textContent?.isNotEmpty() == true)
            args.contentType == "text" -> note.title.isNotEmpty() || (note.textContent?.isNotEmpty() == true)
            args.contentType =="audio" -> note.title.isNotEmpty()
            else -> note.title.isNotEmpty()
        }
    }

    private fun updateNote(note: Note) {
        noteViewModel.update(note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up audio player
        audioPlayerView?.release()
        audioPlayerView = null
    }
}