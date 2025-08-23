package com.example.noteapplicationmvvmflow.feature.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.databinding.ItemNoteBinding
import kotlin.collections.mutableListOf

class NoteAdapter(
    private val onDeleteClick: (Note) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()

    init {
        setHasStableIds(true)
    }

    inner class NoteViewHolder(val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.etTitle.text = note.title
            binding.etDescription.visibility = View.VISIBLE

            binding.compactAudioPlayer.visibility = View.GONE
            binding.compactImagePreview.visibility = View.GONE

            when (note.contentType) {
                "image" -> {
                    binding.etDescription.text =
                        note.textContent.takeUnless { it.isNullOrEmpty() } ?: "🖼️ Image Note"
                    binding.compactImagePreview.visibility = View.VISIBLE
                    note.imagePath?.let { binding.compactImagePreview.setImage(it) }
                }
                "audio" -> {
                    binding.etDescription.text =
                        note.textContent.takeUnless { it.isNullOrEmpty() } ?: "🎤 Audio Note"
                    binding.compactAudioPlayer.visibility = View.VISIBLE
                    note.audioPath?.let { binding.compactAudioPlayer.setAudioPath(it) }
                }
                "text" -> binding.etDescription.text = note.textContent ?: "No content"
                "drawing" -> binding.etDescription.text = "✏️ Drawing Note"
                "todo" -> binding.etDescription.text = "✅ Todo List"
            }

            binding.ivDelete.setOnClickListener { onDeleteClick(note) }
            binding.root.setOnClickListener { onNoteClick(note) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemId(position: Int): Long {
        return notes[position].id.toLong()
    }

    override fun getItemCount(): Int = notes.size

    fun setNotes(newNotes: List<Note>) {
        val diffCallback = NoteDiffCallback(notes, newNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        notes.clear()
        notes.addAll(newNotes)

        diffResult.dispatchUpdatesTo(this)
    }

}
