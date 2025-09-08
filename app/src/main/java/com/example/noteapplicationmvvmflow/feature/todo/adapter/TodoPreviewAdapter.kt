package com.example.noteapplicationmvvmflow.feature.todo.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapplicationmvvmflow.data.model.todo.TodoItem
import com.example.noteapplicationmvvmflow.databinding.ItemTodoPreviewBinding

class TodoPreviewAdapter : RecyclerView.Adapter<TodoPreviewAdapter.TodoPreviewViewHolder>() {

    private var todoItems = listOf<TodoItem>()
    private val maxPreviewItems = 4

    fun setTodoItems(items: List<TodoItem>) {
        todoItems = items.sortedBy { it.position }.take(maxPreviewItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoPreviewViewHolder {
        val binding = ItemTodoPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoPreviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoPreviewViewHolder, position: Int) {
        holder.bind(todoItems[position])
    }

    override fun getItemCount(): Int = todoItems.size

    inner class TodoPreviewViewHolder(private val binding: ItemTodoPreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todoItem: TodoItem) {
            binding.checkboxTodoPreview.isChecked = todoItem.isCompleted
            binding.checkboxTodoPreview.isEnabled = false
            binding.tvTodoTextPreview.text = todoItem.text.ifEmpty { "Empty item" }

            updateItemAppearance(todoItem.isCompleted)
        }

        private fun updateItemAppearance(isCompleted: Boolean) {
            if (isCompleted) {
                binding.tvTodoTextPreview.paintFlags = binding.tvTodoTextPreview.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTodoTextPreview.alpha = 0.6f
                binding.checkboxTodoPreview.alpha = 0.6f
            } else {
                binding.tvTodoTextPreview.paintFlags = binding.tvTodoTextPreview.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTodoTextPreview.alpha = 1.0f
                binding.checkboxTodoPreview.alpha = 1.0f
            }
        }
    }
}