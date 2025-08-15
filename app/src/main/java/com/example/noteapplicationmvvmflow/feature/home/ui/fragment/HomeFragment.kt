package com.example.noteapplicationmvvmflow.feature.home.ui.fragment

import android.os.Bundle
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
import com.example.noteapplicationmvvmflow.R
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
                    description = it.description
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
            val action = HomeFragmentDirections.actionHomeFragmentToAddFragment()
            findNavController().navigate(action)
        }
    }
}