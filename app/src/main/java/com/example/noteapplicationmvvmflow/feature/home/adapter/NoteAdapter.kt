package com.example.noteapplicationmvvmflow.feature.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.databinding.ItemNoteBinding
import kotlin.collections.mutableListOf

class NoteAdapter(
    private val onDeleteClick: (Note) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()

    inner class NoteViewHolder(private val binding: ItemNoteBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.etTitle.text = note.title

            // Display content based on type
            val contentText = when (note.contentType) {
                "text" -> note.textContent ?: "No content"
                "audio" -> "🎤 Audio Note"
                "image" -> "🖼️ Image Note"
                "drawing" -> "✏️ Drawing Note"
                "todo" -> "✅ Todo List"
                else -> note.textContent ?: "No content"
            }

            binding.etDescription.text = contentText

            binding.ivDelete.setOnClickListener {
                onDeleteClick(note)
            }
            binding.root.setOnClickListener {
                onNoteClick(note)
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

    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}