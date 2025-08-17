package com.example.noteapplicationmvvmflow.feature.content_selection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.data.model.ContentType
import com.example.noteapplicationmvvmflow.databinding.FragmentContentTypeSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ContentTypeSelectionFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentContentTypeSelectionBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContentTypeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnText.setOnClickListener {
            navigateToAddFragment(ContentType.TEXT)
        }

        binding.btnAudio.setOnClickListener {
            val action = ContentTypeSelectionFragmentDirections.actionContentTypeSelectionFragmentToAudioFragment()
            findNavController().navigate(action)
        }

        binding.btnImage.setOnClickListener {
            navigateToAddFragment(ContentType.IMAGE)
        }

        binding.btnDrawing.setOnClickListener {
            navigateToAddFragment(ContentType.DRAWING)
        }

        binding.btnTodo.setOnClickListener {
            navigateToAddFragment(ContentType.TODO)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

    }

    private fun navigateToAddFragment(contentType: ContentType) {
        val action = ContentTypeSelectionFragmentDirections.actionContentTypeSelectionFragmentToAddFragment(
            contentType = contentType.value
        )
        findNavController().navigate(action)
        dismiss()
    }
}