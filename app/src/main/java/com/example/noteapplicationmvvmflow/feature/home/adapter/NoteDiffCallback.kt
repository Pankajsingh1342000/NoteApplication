package com.example.noteapplicationmvvmflow.feature.home.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.noteapplicationmvvmflow.data.db.Note

class NoteDiffCallback(
    private val oldList: List<Note>,
    private val newList: List<Note>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(o: Int, n: Int) = oldList[o].id == newList[n].id
    override fun areContentsTheSame(o: Int, n: Int): Boolean {
        val old = oldList[o]
        val new = newList[n]
        return old.title == new.title &&
                old.textContent == new.textContent &&
                old.contentType == new.contentType &&
                old.audioPath == new.audioPath &&
                old.imagePath == new.imagePath &&
                old.updatedAt == new.updatedAt
    }
}