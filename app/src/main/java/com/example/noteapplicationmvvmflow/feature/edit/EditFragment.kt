package com.example.noteapplicationmvvmflow.feature.edit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.data.helper.NoteHelper
import com.example.noteapplicationmvvmflow.databinding.FragmentEditBinding
import com.example.noteapplicationmvvmflow.feature.audio.AudioPlayerView
import com.example.noteapplicationmvvmflow.feature.image.ImagePreview
import com.example.noteapplicationmvvmflow.feature.todo.ui.TodoListView
import com.example.noteapplicationmvvmflow.feature.todo.util.TodoItemConverter
import com.example.noteapplicationmvvmflow.feature.todo.viewmodel.TodoViewModel
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val todoViewModel: TodoViewModel by viewModels() // Add TodoViewModel
    private val args: EditFragmentArgs by navArgs()

    private var audioDeleted = false
    private var imageDeleted = false
    private var audioPlayerView: AudioPlayerView? = null
    private var imagePreviewView: ImagePreview? = null
    private var todoListView: TodoListView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateFields()
        updateUIForContentType()
        handleBackPress()

        if (args.contentType == "todo") {
            observeTodoViewModel()
            setupTodoTitleWatcher()
            loadExistingTodoItems()
        }
    }

    private fun observeTodoViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    todoViewModel.todoItems.collect { items ->
                        todoListView?.setTodoItems(items)
                    }
                }

                // Observe title changes from ViewModel (optional)
                launch {
                    todoViewModel.title.collect { title ->
                        if (binding.etTitle.text.toString() != title) {
                            binding.etTitle.setText(title)
                            binding.etTitle.setSelection(title.length)
                        }
                    }
                }
            }
        }
    }

    private fun setupTodoTitleWatcher() {
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                todoViewModel.setTitle(s.toString())
            }
        })
    }

    private fun loadExistingTodoItems() {
        // Load existing TODO items into ViewModel
        val existingTodoItems = TodoItemConverter.fromJson(args.todoItems)
        if (existingTodoItems.isNotEmpty()) {
            todoViewModel.setTodoItems(existingTodoItems)
        } else {
            // Add initial item if no existing items
            todoViewModel.initializeIfEmpty()
        }

        // Set existing title
        todoViewModel.setTitle(args.title)
    }

    private fun populateFields() {
        binding.etTitle.setText(args.title)
        binding.etDescription.setText(args.textContent)
        binding.root.setBackgroundColor(args.bgColor)
    }

    private fun updateUIForContentType() {
        val params = binding.layoutTitle.layoutParams as ConstraintLayout.LayoutParams

        when (args.contentType) {
            "text" -> {
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.topToBottom = ConstraintLayout.LayoutParams.UNSET
                binding.layoutTitle.layoutParams = params

                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 3
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE
                binding.imagePreviewContainer.visibility = View.GONE
                binding.todoContainer.visibility = View.GONE
            }

            "audio" -> {
                binding.audioPlayerContainer.visibility = View.VISIBLE
                params.topToTop = ConstraintLayout.LayoutParams.UNSET
                params.topToBottom = binding.audioPlayerContainer.id
                binding.layoutTitle.layoutParams = params

                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 1
                binding.etDescription.visibility = View.VISIBLE
                binding.imagePreviewContainer.visibility = View.GONE
                binding.todoContainer.visibility = View.GONE

                setupAudioPlayer()
            }

            "image" -> {
                binding.imagePreviewContainer.visibility = View.VISIBLE
                params.topToTop = ConstraintLayout.LayoutParams.UNSET
                params.topToBottom = binding.imagePreviewContainer.id
                binding.layoutTitle.layoutParams = params

                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 1
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE
                binding.todoContainer.visibility = View.GONE

                setupImagePreview(args.imagePath)
            }

            "drawing" -> {
                binding.imagePreviewContainer.visibility = View.VISIBLE
                params.topToTop = ConstraintLayout.LayoutParams.UNSET
                params.topToBottom = binding.imagePreviewContainer.id
                binding.layoutTitle.layoutParams = params

                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 1
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE
                binding.todoContainer.visibility = View.GONE

                setupImagePreview(args.drawingPath)
            }

            "todo" -> {
                binding.todoContainer.visibility = View.VISIBLE
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.bottomToTop = binding.todoContainer.id
                binding.layoutTitle.layoutParams = params

                binding.etDescription.visibility = View.GONE
                binding.audioPlayerContainer.visibility = View.GONE
                binding.imagePreviewContainer.visibility = View.GONE

                setupTodoList()
            }

            else -> {
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.topToBottom = ConstraintLayout.LayoutParams.UNSET
                binding.layoutTitle.layoutParams = params

                binding.etDescription.isEnabled = true
                binding.etDescription.minLines = 3
                binding.etDescription.visibility = View.VISIBLE
                binding.audioPlayerContainer.visibility = View.GONE
                binding.imagePreviewContainer.visibility = View.GONE
                binding.todoContainer.visibility = View.GONE
            }
        }
    }

    private fun setupAudioPlayer() {
        binding.audioPlayerContainer.removeAllViews()
        audioPlayerView = AudioPlayerView(requireContext())
        binding.audioPlayerContainer.addView(audioPlayerView)

        audioPlayerView?.onAudioDeleted = {
            onAudioDeleted()
        }

        if (args.audioPath.isNotEmpty()) {
            audioPlayerView?.setAudioPath(args.audioPath)
        }
    }

    private fun setupImagePreview(path: String?) {
        binding.imagePreviewContainer.removeAllViews()
        imagePreviewView = ImagePreview(requireContext())
        binding.imagePreviewContainer.addView(imagePreviewView)

        imagePreviewView?.onImageDeleted = {
            onImageDeleted()
        }

        if (path != null) {
            imagePreviewView?.setImage(path)
        }
    }

    private fun setupTodoList() {
        binding.todoContainer.removeAllViews()

        todoListView = TodoListView(requireContext())
        binding.todoContainer.addView(todoListView)

        // Connect TodoListView callbacks to TodoViewModel
        todoListView?.onAddTodoItem = {
            todoViewModel.addTodoItem()
        }

        todoListView?.onItemTextChanged = { itemId, newText ->
            todoViewModel.updateTodoItemText(itemId, newText)
        }

        todoListView?.onItemToggled = { itemId ->
            todoViewModel.toggleTodoItemCompletion(itemId)
        }

        todoListView?.onItemDeleted = { itemId ->
            todoViewModel.deleteTodoItem(itemId)
        }

        todoListView?.onItemMoved = { fromPos, toPos ->
            todoViewModel.moveTodoItem(fromPos, toPos)
        }
    }

    private fun onAudioDeleted() {
        audioDeleted = true
        binding.audioPlayerContainer.visibility = View.GONE
        binding.etDescription.isEnabled = true
        binding.etDescription.minLines = 3
        binding.etDescription.visibility = View.VISIBLE
    }

    private fun onImageDeleted() {
        imageDeleted = true
        binding.imagePreviewContainer.visibility = View.GONE
        binding.etDescription.isEnabled = true
        binding.etDescription.minLines = 3
        binding.etDescription.visibility = View.VISIBLE
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
            drawingPath = args.drawingPath,
            todoItems = args.todoItems,
            createdAt = 0L,
            updatedAt = 0L
        )

        if (hasNoteChanged(newNote, oldNote)) {
            newNote.updatedAt = System.currentTimeMillis()
            noteViewModel.update(newNote)
        }

        findNavController().popBackStack()
    }

    private fun hasNoteChanged(newNote: Note, oldNote: Note): Boolean {
        return newNote.title != oldNote.title ||
                newNote.textContent != oldNote.textContent ||
                newNote.contentType != oldNote.contentType ||
                newNote.audioPath != oldNote.audioPath ||
                newNote.imagePath != oldNote.imagePath ||
                newNote.drawingPath != oldNote.drawingPath ||
                newNote.todoItems != oldNote.todoItems
    }

    private fun createUpdatedNote(): Note {
        return when {
            audioDeleted || imageDeleted -> {
                // Convert to text note if media was deleted
                Note(
                    id = args.id,
                    title = binding.etTitle.text.toString().trim(),
                    contentType = "text",
                    textContent = binding.etDescription.text.toString().trim(),
                    audioPath = null,
                    imagePath = null,
                    drawingPath = null,
                    todoItems = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            args.contentType == "todo" -> {

                val todoItems = todoViewModel.todoItems.value
                NoteHelper.createTodoNote(
                    title = binding.etTitle.text.toString().trim(),
                    todoItems = todoItems
                ).copy(
                    id = args.id,
                    createdAt = System.currentTimeMillis()
                )
            }

            else -> {
                // Handle other content types
                Note(
                    id = args.id,
                    title = binding.etTitle.text.toString().trim(),
                    contentType = args.contentType,
                    textContent = binding.etDescription.text.toString().trim(),
                    audioPath = audioPlayerView?.getAudioPath() ?: args.audioPath,
                    imagePath = imagePreviewView?.getImagePath() ?: args.imagePath,
                    drawingPath = imagePreviewView?.getImagePath() ?: args.drawingPath,
                    todoItems = args.todoItems,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayerView?.release()
        audioPlayerView = null
        imagePreviewView = null
        todoListView = null
    }
}