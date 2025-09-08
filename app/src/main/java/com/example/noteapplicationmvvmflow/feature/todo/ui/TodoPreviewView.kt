package com.example.noteapplicationmvvmflow.feature.todo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapplicationmvvmflow.data.model.todo.TodoItem
import com.example.noteapplicationmvvmflow.databinding.ViewTodoPreviewBinding
import com.example.noteapplicationmvvmflow.feature.todo.adapter.TodoPreviewAdapter

class TodoPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewTodoPreviewBinding =
        ViewTodoPreviewBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var previewAdapter: TodoPreviewAdapter

    init {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        previewAdapter = TodoPreviewAdapter()
        binding.rvTodoPreview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = previewAdapter
        }
    }


    fun setTodoItems(items: List<TodoItem>) {
        previewAdapter.setTodoItems(items)
    }
}
