package com.example.noteapplicationmvvmflow.feature.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.data.helper.NoteHelper
import com.example.noteapplicationmvvmflow.databinding.FragmentAddBinding
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val args: AddFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPress()
        updateUIForContentType()
    }

    private fun updateUIForContentType() {
        when (args.contentType) {
            "text" -> {
                binding.etDescription.hint = "Enter your note content here..."
                binding.etDescription.isEnabled = true
            }
            "audio" -> {
                binding.etDescription.hint = "Audio recording will be added here"
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
            }
            "image" -> {
                binding.etDescription.hint = "Image will be added here"
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
            }
            "drawing" -> {
                binding.etDescription.hint = "Drawing canvas will be added here"
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
            }
            "todo" -> {
                binding.etDescription.hint = "Todo list will be added here"
                binding.etDescription.isEnabled = false
                binding.etDescription.minLines = 1
                binding.etDescription.maxLines = 1
            }
        }
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
        }
        findNavController().popBackStack()
    }

    private fun createNoteFromInput(): Note {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etDescription.text.toString().trim()

        return when (args.contentType) {
            "text" -> NoteHelper.createTextNote(title, content)
            "audio" -> NoteHelper.createAudioNote(title, "")
            "image" -> NoteHelper.createImageNote(title, "")
            "drawing" -> NoteHelper.createDrawingNote(title, "")
            "todo" -> NoteHelper.createTodoNote(title, "")
            else -> NoteHelper.createTextNote(title, content)
        }
    }

    private fun shouldSaveNote(note: Note): Boolean {
        return when (args.contentType) {
            "text" -> note.title.isNotEmpty() || (note.textContent?.isNotEmpty() == true)
            else -> note.title.isNotEmpty()
        }
    }

    private fun saveNote(note: Note) {
        noteViewModel.insert(note)
    }
}