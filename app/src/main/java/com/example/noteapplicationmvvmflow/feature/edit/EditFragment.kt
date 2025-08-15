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

        binding.etTitle.setText(args.title)
        binding.etDescription.setText(args.description)
        handleBackPress()

    }

    private fun handleBackPress() {
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
    }

    private fun updateNote(note: Note) {
        if (note.title.isNotBlank() && note.description.isNotBlank()) {
            noteViewModel.update(note.copy(title = note.title, description = note.description))
        }
    }

}