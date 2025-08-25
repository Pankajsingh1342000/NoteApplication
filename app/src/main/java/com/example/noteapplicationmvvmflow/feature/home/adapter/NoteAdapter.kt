package com.example.noteapplicationmvvmflow.feature.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapplicationmvvmflow.data.db.Note
import com.example.noteapplicationmvvmflow.databinding.ItemNoteBinding
import kotlin.collections.mutableListOf
import androidx.core.graphics.toColorInt

class NoteAdapter(
    private val onDeleteClick: (Note) -> Unit,
    private val onNoteClick: (Note, bgColor: Int) -> Unit,
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()
    private val pastelColors = listOf(
        "#AEC6CF".toColorInt(), // Soft Blue
        "#FFB7B2".toColorInt(), // Pastel Pink
        "#FFDAB9".toColorInt(), // Peach Puff
        "#E6E6FA".toColorInt(), // Lavender
        "#B5EAD7".toColorInt(), // Mint Pastel
        "#C7CEEA".toColorInt(), // Periwinkle
        "#FFFACD".toColorInt(), // Lemon Chiffon
        "#FDCFE8".toColorInt(), // Soft Blush
        "#D5AAFF".toColorInt(), // Lilac Mist
        "#A0E7E5".toColorInt()  // Aqua Pastel
    )

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

            val randomColor = pastelColors[adapterPosition % pastelColors.size]
            binding.layoutCard.setCardBackgroundColor(randomColor)

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
            binding.root.setOnClickListener { onNoteClick(note, randomColor) }
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
