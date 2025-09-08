package com.example.noteapplicationmvvmflow.feature.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapplicationmvvmflow.data.model.todo.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor() : ViewModel() {

    private val _todoItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItems: StateFlow<List<TodoItem>> = _todoItems.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setTodoItems(items: List<TodoItem>) {
        _todoItems.value = items.toList()
    }

    fun addTodoItem() {
        viewModelScope.launch {
            val currentItems = _todoItems.value.toMutableList()
            val newItem = TodoItem(
                text = "",
                position = currentItems.size
            )
            currentItems.add(newItem)
            _todoItems.value = currentItems
        }
    }

    fun updateTodoItems(items: List<TodoItem>) {
        _todoItems.value = items.toList()
    }

    fun deleteTodoItem(itemId: String) {
        viewModelScope.launch {
            val currentItems = _todoItems.value.toMutableList()
            currentItems.removeAll { it.id == itemId }
            // Update positions
            currentItems.forEachIndexed { index, item ->
                item.position = index
            }
            _todoItems.value = currentItems
        }
    }

    fun moveTodoItem(fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            val currentItems = _todoItems.value.toMutableList()
            if (fromPosition < currentItems.size && toPosition < currentItems.size) {
                val item = currentItems.removeAt(fromPosition)
                currentItems.add(toPosition, item)

                // Update positions for all items
                currentItems.forEachIndexed { index, todoItem ->
                    todoItem.position = index
                }

                _todoItems.value = currentItems
            }
        }
    }

    fun updateTodoItemText(itemId: String, newText: String) {
        viewModelScope.launch {
            val currentItems = _todoItems.value.toMutableList()
            val item = currentItems.find { it.id == itemId }
            if (item != null) {
                item.text = newText
                _todoItems.value = currentItems
            }
        }
    }

    fun toggleTodoItemCompletion(itemId: String) {
        viewModelScope.launch {
            val currentItems = _todoItems.value.toMutableList()
            val item = currentItems.find { it.id == itemId }
            if (item != null) {
                item.isCompleted = !item.isCompleted
                _todoItems.value = currentItems
            }
        }
    }

    fun getCompletedCount(): Int = _todoItems.value.count { it.isCompleted }

    fun getTotalCount(): Int = _todoItems.value.size

    fun hasContent(): Boolean = _title.value.isNotEmpty() || _todoItems.value.any { it.text.isNotEmpty() }

    fun clearAll() {
        _todoItems.value = emptyList()
        _title.value = ""
    }

    fun initializeIfEmpty() {
        if (_todoItems.value.isEmpty()) {
            addTodoItem()
        }
    }
}
