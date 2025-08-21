package com.example.noteapplicationmvvmflow.feature.home.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.databinding.ItemNoteBinding
import com.example.noteapplicationmvvmflow.feature.audio.CompactAudioPlayerView
import com.example.noteapplicationmvvmflow.feature.image.CompactImagePreview
import kotlin.collections.mutableListOf

class NoteAdapter(
    private val onDeleteClick: (Note) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()
    private val audioPlayers = mutableMapOf<Int, CompactAudioPlayerView>()
    private val imagePreviews = mutableMapOf<Int, CompactImagePreview>()

    companion object {
        private const val TAG = "NoteAdapter"
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.etTitle.text = note.title

            binding.etDescription.visibility = View.VISIBLE
            binding.audioPlayerContainer.removeAllViews()
            binding.imagePreviewContainer.removeAllViews()

            when (note.contentType) {
                "text" -> {
                    binding.etDescription.text = note.textContent ?: "No content"
                    binding.etDescription.visibility = View.VISIBLE
                }
                "audio" -> {
                    val description = note.textContent
                    if (!description.isNullOrEmpty()) {
                        binding.etDescription.text = description
                    } else {
                        binding.etDescription.text = "🎤 Audio Note"
                    }
                    binding.etDescription.visibility = View.VISIBLE
                    setupAudioPlayer(note, binding)
                }
                "image" -> {
                    val description = note.textContent
                    if (!description.isNullOrEmpty()) {
                        binding.etDescription.text = description
                    }else {
                        binding.etDescription.text = "🖼️ Image Note"
                    }
                    binding.etDescription.visibility = View.VISIBLE
                    setupImagePreview(note, binding)
                }
                "drawing" -> {
                    binding.etDescription.text = "✏️ Drawing Note"
                    binding.etDescription.visibility = View.VISIBLE
                }
                "todo" -> {
                    binding.etDescription.text = "✅ Todo List"
                    binding.etDescription.visibility = View.VISIBLE
                }
                else -> {
                    binding.etDescription.text = note.textContent ?: "No content"
                    binding.etDescription.visibility = View.VISIBLE
                }
            }

            binding.ivDelete.setOnClickListener {
                onDeleteClick(note)
            }
            binding.root.setOnClickListener {
                onNoteClick(note)
            }
        }

        private fun setupAudioPlayer(note: Note, binding: ItemNoteBinding) {
            try {
                Log.d(TAG, "Setting up audio player for note ID: ${note.id}")
                Log.d(TAG, "Audio path: ${note.audioPath}")

                val audioPlayer = CompactAudioPlayerView(binding.root.context)
                binding.audioPlayerContainer.addView(audioPlayer)

                audioPlayers[note.id] = audioPlayer

                note.audioPath?.let { path ->
                    Log.d(TAG, "Setting audio path: $path")
                    audioPlayer.setAudioPath(path)
                } ?: run {
                    Log.e(TAG, "Audio path is null for note ID: ${note.id}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error setting up audio player", e)
            }
        }

        private fun setupImagePreview(note: Note, binding: ItemNoteBinding) {
            try {
                val imagePreview = CompactImagePreview(binding.root.context)
                binding.imagePreviewContainer.addView(imagePreview)
                imagePreviews[note.id] = imagePreview

                note.imagePath?.let { path ->
                    imagePreview.setImage(path)
                } ?: run {
                    Log.e(TAG, "Image path is null for note ID: ${note.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up image preview", e)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onViewRecycled(holder: NoteViewHolder) {
        super.onViewRecycled(holder)

        // Clean up audio players for recycled views
        val noteId = notes[holder.layoutPosition].id
        audioPlayers[noteId]?.release()
        audioPlayers.remove(noteId)

        imagePreviews[noteId]?.let { preview ->
            // Clean up image preview if needed
            imagePreviews.remove(noteId)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(newNotes: List<Note>) {
        Log.d(TAG, "Setting ${newNotes.size} notes")

        // Pause all currently playing audio players
        audioPlayers.values.forEach { it.pauseIfPlaying() }

        // Reset all audio players to beginning
        audioPlayers.values.forEach { it.resetAudio() }

        // Release all audio players before clearing
        releaseAllAudioPlayers()
        imagePreviews.clear()

        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    fun resetAllAudioPlayers() {
        Log.d(TAG, "Resetting all audio players")
        audioPlayers.values.forEach { it.resetAudio() }
    }

    fun releaseAllAudioPlayers() {
        Log.d(TAG, "Releasing all audio players")
        audioPlayers.values.forEach { it.release() }
        audioPlayers.clear()
    }

    fun pauseAllAudioPlayers() {
        Log.d(TAG, "Pausing all audio players")
        audioPlayers.values.forEach { it.pauseIfPlaying() }
    }
}