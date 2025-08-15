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
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.databinding.FragmentAddBinding
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private val noteViewModel: NoteViewModel by viewModels()

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
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val note = Note(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString()
                )
                if(note.title.isNotEmpty() || note.description.isNotEmpty()){
                    saveNote(note)
                }
                findNavController().popBackStack()

            }
        })
    }

    private fun saveNote(note: Note) {
        noteViewModel.insert(note)
    }
}