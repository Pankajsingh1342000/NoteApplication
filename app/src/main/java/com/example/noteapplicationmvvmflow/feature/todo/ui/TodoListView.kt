package com.example.noteapplicationmvvmflow.feature.todo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapplicationmvvmflow.data.model.todo.TodoItem
import com.example.noteapplicationmvvmflow.databinding.ViewTodoListBinding
import com.example.noteapplicationmvvmflow.feature.todo.adapter.TodoAdapter
import com.example.noteapplicationmvvmflow.feature.todo.util.ItemMoveCallback

class TodoListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
    private val binding: ViewTodoListBinding =
        ViewTodoListBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    var onAddTodoItem: (() -> Unit)? = null
    var onItemTextChanged: ((String, String) -> Unit)? = null
    var onItemToggled: ((String) -> Unit)? = null
    var onItemDeleted: ((String) -> Unit)? = null
    var onItemMoved: ((Int, Int) -> Unit)? = null

    init {
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {

        todoAdapter = TodoAdapter(
            onStartDrag = { viewHolder ->
                itemTouchHelper.startDrag(viewHolder)
            },
            onTextChanged = { itemId, nextItem ->
                onItemTextChanged?.invoke(itemId, nextItem)
            },
            onItemToggled = { itemId ->
                onItemToggled?.invoke(itemId)
            },
            onItemDeleted = { itemId ->
                onItemDeleted?.invoke(itemId)
            },
            onItemMoved = { fromPos, toPos ->
                onItemMoved?.invoke(fromPos, toPos)
            }
        )

        val callback = ItemMoveCallback(todoAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvTodoItems)

        binding.rvTodoItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddTodoItem.setOnClickListener {
            onAddTodoItem?.invoke()
        }
    }

    fun setTodoItems(items: List<TodoItem>) {
        todoAdapter.setTodoItems(items)
    }

    fun getTodoItems(): List<TodoItem> {
        return todoAdapter.getCurrentItems()
    }
}