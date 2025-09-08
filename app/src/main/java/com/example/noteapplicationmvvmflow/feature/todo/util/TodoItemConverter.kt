package com.example.noteapplicationmvvmflow.feature.todo.util

import android.util.Log
import com.example.noteapplicationmvvmflow.data.model.todo.TodoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TodoItemConverter {
    private val gson = Gson()

    fun toJson(todoItems: List<TodoItem>): String {
        return gson.toJson(todoItems)
    }

    fun fromJson(json: String?): List<TodoItem> {
        if(json.isNullOrEmpty()) return emptyList()
        return try {
            val listType = object : TypeToken<List<TodoItem>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: Exception) {
            Log.e("TodoItemConverter", "Error parsing JSON: ${e.message}")
            emptyList()
        }
    }
}