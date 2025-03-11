package com.example.backintime.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backintime.model.dao.MemoryEntity
import com.example.backintime.repository.MemoryRepository
import kotlinx.coroutines.launch

class MemoryViewModel(private val repository: MemoryRepository) : ViewModel() {

    private val _memories = MutableLiveData<List<MemoryEntity>>()
    val memories: LiveData<List<MemoryEntity>> get() = _memories

    fun loadAllMemories() {
        viewModelScope.launch {
            _memories.value = repository.getAllMemories()
        }
    }

    fun loadUserMemories(userId: String) {
        viewModelScope.launch {
            _memories.value = repository.getUserMemories(userId)
        }
    }

    fun addMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.insertMemory(memory)
            // עדכון רשימת הפוסטים לפי המשתמש
            loadUserMemories(memory.creatorId)
        }
    }

    fun deleteMemory(memoryId: Int) {
        viewModelScope.launch {
            repository.deleteMemory(memoryId)
            _memories.value = _memories.value?.filterNot { it.id == memoryId }
        }
    }
}
