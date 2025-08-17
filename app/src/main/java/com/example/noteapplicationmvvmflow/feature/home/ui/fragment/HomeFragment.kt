package com.example.noteapplicationmvvmflow.feature.home.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.rvNotes
        fab = binding.fabAdd

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = NoteAdapter(
            onDeleteClick = {
                noteViewModel.delete(it)
            },
            onNoteClick = {
                val action = HomeFragmentDirections.actionHomeFragmentToEditFragment(
                    id = it.id,
                    title = it.title,
                    textContent = it.textContent ?: "",
                    contentType = it.contentType ?: "text",
                    audioPath = it.audioPath ?: ""
                )
                findNavController().navigate(action)
            }
        )
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                noteViewModel.allNote.collect {
                    adapter.setNotes(it)
                }
            }
        }

        fab.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToContentTypeSelectionFragment()
            findNavController().navigate(action)
        }
    }

    // Add this method to handle audio player cleanup
    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "Fragment resumed - resetting all audio players")
        adapter.resetAllAudioPlayers()
    }

    // Update the existing lifecycle methods
    override fun onPause() {
        super.onPause()
        Log.d("HomeFragment", "Fragment paused - pausing all audio players")
        adapter.pauseAllAudioPlayers()
    }

    override fun onStop() {
        super.onStop()
        Log.d("HomeFragment", "Fragment stopped - releasing all audio players")
        adapter.releaseAllAudioPlayers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "Fragment destroyed - releasing all audio players")
        adapter.releaseAllAudioPlayers()
    }
}