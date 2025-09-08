package com.example.noteapplicationmvvmflow.feature.add

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.data.helper.NoteHelper
import com.example.noteapplicationmvvmflow.databinding.FragmentAddBinding
import com.example.noteapplicationmvvmflow.feature.audio.AudioPlayerView
import com.example.noteapplicationmvvmflow.feature.image.ImagePreview
import com.example.noteapplicationmvvmflow.feature.todo.ui.TodoListView
import com.example.noteapplicationmvvmflow.feature.todo.viewmodel.TodoViewModel
import com.example.noteapplicationmvvmflow.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val todoViewModel: TodoViewModel by viewModels() // Add TodoViewModel
    private val args: AddFragmentArgs by navArgs()

    private var audioPlayerView: AudioPlayerView? = null
    private var imagePreviewView: ImagePreview? = null
    private var todoListView: TodoListView? = null

    private var audioDeleted = false
    private var imageDeleted = false

    private val pastelColors = listOf(
        "#AEC6CF".toColorInt(), "#FFB7B2".toColorInt(), "#FFDAB9".toColorInt(),
        "#E6E6FA".toColorInt(), "#B5EAD7".toColorInt(), "#C7CEEA".toColorInt(),
        "#FFFACD".toColorInt(), "#FDCFE8".toColorInt(), "#D5AAFF".toColorInt(),
        "#A0E7E5".toColorInt()
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUIForContentType()
        handleBackPress()

        val randomColor = pastelColors.random()
        binding.root.setBackgroundColor(randomColor)

        // Only observe TodoViewModel for TODO content type
        if (args.contentType == "todo") {
            observeTodoViewModel()
            setupTodoTitleWatcher()
        }
    }

    private fun observeTodoViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe todo items
                launch {
                    todoViewModel.todoItems.collect { items ->
                        todoListView?.setTodoItems(items)
                    }
                }

                // Observe title changes from ViewModel (optional)
                launch {
                    todoViewModel.title.collect { title ->
                        if (binding.etTitle.text.toString() != title) {
                            // Avoid infinite loop by checking if text is different
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
        }
    }

    private fun setupAudioPlayer() {
        Log.d("AddFragment", "Setting up audio player")
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

        // Initialize with one empty item if ViewModel is empty
        todoViewModel.initializeIfEmpty()
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
                saveNoteAndNavigateBack()
            }
        })
    }

    private fun saveNoteAndNavigateBack() {
        val note = createNoteFromInput()
        if (shouldSaveNote(note)) {
            saveNote(note)
        } else {
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
                val audioPath = if (args.audioPath.isNotEmpty()) args.audioPath else ""
                NoteHelper.createAudioNote(title, audioPath, content)
            }
            args.contentType == "image" -> {
                val imagePath = if (args.imagePath.isNotEmpty()) args.imagePath else ""
                NoteHelper.createImageNote(title, imagePath, content)
            }
            args.contentType == "drawing" -> {
                val drawingPath = if (args.drawingPath.isNotEmpty()) args.drawingPath else ""
                NoteHelper.createDrawingNote(title, drawingPath, content)
            }
            args.contentType == "todo" -> {
                // Get todo items from ViewModel
                val todoItems = todoViewModel.todoItems.value
                NoteHelper.createTodoNote(title, todoItems)
            }
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
            args.contentType == "drawing" -> note.title.isNotEmpty()
            args.contentType == "todo" -> {
                todoViewModel.hasContent()
            }
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
        imagePreviewView = null
        todoListView = null
    }
}