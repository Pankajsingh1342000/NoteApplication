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
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.databinding.FragmentEditBinding
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val args: EditFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateFields()
        handleBackPress()

    }

    private fun populateFields() {
        binding.etTitle.setText(args.title)
        binding.etDescription.setText(args.textContent)
        binding.etDescription.hint = "Enter your note content here..."
    }

/*    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val note = Note(
                    id = args.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString()
                )
                updateNote(note)
                findNavController().popBackStack()

            }
        })
    }*/

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
            contentType = binding.etDescription.toString().trim(),
            textContent = binding.etDescription.text.toString().trim(),
            audioPath = null,
            imagePath = null,
            drawingData = null,
            todoItems = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun shouldUpdateNote(note: Note): Boolean {
        return note.title.isNotEmpty() || (note.textContent?.isNotEmpty() == true)
    }

    private fun updateNote(note: Note) {
        if (note.title.isNotBlank() && note.textContent?.isNotBlank() == true) {
            noteViewModel.update(note.copy(title = note.title, textContent = note.textContent))
        }
    }

}