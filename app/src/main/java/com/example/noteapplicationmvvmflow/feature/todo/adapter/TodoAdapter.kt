package com.example.noteapplicationmvvmflow.feature.todo.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapplicationmvvmflow.data.model.todo.TodoItem
import com.example.noteapplicationmvvmflow.databinding.ItemTodoBinding
import com.example.noteapplicationmvvmflow.feature.todo.util.ItemMoveCallback
import java.util.Collections

class TodoAdapter(
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
    private val onTextChanged: (String, String) -> Unit,
    private val onItemToggled: (String) -> Unit,
    private val onItemDeleted: (String) -> Unit,
    private val onItemMoved: (Int, Int) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    private var todoItems = mutableListOf<TodoItem>()
    private var isDragging = false
    private var lastMoveTime = 0L
    private val MOVE_THROTTLE_MS = 50L
    private var pendingUpdates = false // Track if we have pending ViewModel updates

    fun setTodoItems(items: List<TodoItem>) {
        // Don't update during drag to preserve states
        if (!isDragging && !pendingUpdates) {
            todoItems.clear()
            todoItems.addAll(items.sortedBy { it.position })
            notifyDataSetChanged()
        } else if (!isDragging) {
            // Update the underlying data but don't refresh UI if pending
            todoItems.clear()
            todoItems.addAll(items.sortedBy { it.position })
        }
    }

    fun getCurrentItems(): List<TodoItem> {
        return todoItems.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        if (position < todoItems.size) {
            holder.bind(todoItems[position])
        }
    }

    override fun getItemCount(): Int = todoItems.size

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastMoveTime < MOVE_THROTTLE_MS) {
            return
        }
        lastMoveTime = currentTime

        if (fromPosition < todoItems.size && toPosition < todoItems.size && fromPosition != toPosition) {
            Collections.swap(todoItems, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            onItemMoved(fromPosition, toPosition)
            Log.d("TodoAdapter", "Throttled move: $fromPosition -> $toPosition")
        }
    }

    override fun onRowSelected(myViewHolder: TodoViewHolder) {
        isDragging = true
        pendingUpdates = false // Clear any pending updates when drag starts
        Log.d("TodoAdapter", "Drag started")
        myViewHolder.onRowSelected()
    }

    override fun onRowClear(myViewHolder: TodoViewHolder) {
        isDragging = false
        Log.d("TodoAdapter", "Drag ended")
        myViewHolder.onRowClear()

        // Allow updates again after a small delay
        myViewHolder.itemView.postDelayed({
            pendingUpdates = false
            notifyDataSetChanged()
        }, 150)
    }

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null
        private var currentItem: TodoItem? = null

        @SuppressLint("ClickableViewAccessibility")
        fun bind(todoItem: TodoItem) {
            currentItem = todoItem

            clearListeners()

            // Set data
            binding.checkboxTodo.isChecked = todoItem.isCompleted
            binding.etTodoText.setText(todoItem.text)
            updateItemAppearance(todoItem.isCompleted)

            // Disable interactions during drag
            binding.checkboxTodo.isEnabled = !isDragging
            binding.etTodoText.isEnabled = !isDragging
            binding.btnDeleteTodo.isEnabled = !isDragging

            Log.d("TodoAdapter", "Binding ${todoItem.id} at $adapterPosition: completed=${todoItem.isCompleted}, dragging=$isDragging")

            if (!isDragging) {
                setupListeners(todoItem)
            }
        }

        private fun clearListeners() {
            textWatcher?.let { binding.etTodoText.removeTextChangedListener(it) }
            textWatcher = null
            binding.checkboxTodo.setOnCheckedChangeListener(null)
            binding.btnDeleteTodo.setOnClickListener(null)
            binding.ivDragHandle.setOnTouchListener(null)
        }

        private fun setupListeners(todoItem: TodoItem) {
            // Checkbox listener - ONLY notify ViewModel, don't update directly
            binding.checkboxTodo.setOnCheckedChangeListener { _, isChecked ->
                if (!isDragging && currentItem?.id == todoItem.id) {
                    Log.d("TodoAdapter", "Checkbox changing: ${todoItem.id} from ${todoItem.isCompleted} to $isChecked")

                    // Set pending updates flag to prevent conflicting updates
                    pendingUpdates = true

                    // Update visual immediately for better UX
                    updateItemAppearance(isChecked)

                    // Let ViewModel handle the actual state change
                    onItemToggled(todoItem.id)

                    // Clear pending flag after a delay to allow ViewModel update
                    binding.root.postDelayed({
                        pendingUpdates = false
                    }, 100)
                }
            }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isDragging && currentItem?.id == todoItem.id) {
                        val newText = s.toString()
                        if (todoItem.text != newText) {
                            // Update immediately to prevent race conditions
                            todoItem.text = newText
                            onTextChanged(todoItem.id, newText)
                        }
                    }
                }
            }
            binding.etTodoText.addTextChangedListener(textWatcher)

            binding.btnDeleteTodo.setOnClickListener {
                if (!isDragging && currentItem?.id == todoItem.id) {
                    onItemDeleted(todoItem.id)
                }
            }

            binding.ivDragHandle.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN && !isDragging) {
                    onStartDrag(this)
                }
                false
            }
        }

        private fun updateItemAppearance(isCompleted: Boolean) {
            if (isCompleted) {
                binding.etTodoText.paintFlags = binding.etTodoText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.etTodoText.alpha = 0.6f
                binding.checkboxTodo.alpha = 0.6f
            } else {
                binding.etTodoText.paintFlags = binding.etTodoText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.etTodoText.alpha = 1.0f
                binding.checkboxTodo.alpha = 1.0f
            }
        }

        fun onRowSelected() {
            binding.root.alpha = 0.8f
            binding.root.scaleX = 1.05f
            binding.root.scaleY = 1.05f
        }

        fun onRowClear() {
            binding.root.alpha = 1.0f
            binding.root.scaleX = 1.0f
            binding.root.scaleY = 1.0f
        }
    }
}
