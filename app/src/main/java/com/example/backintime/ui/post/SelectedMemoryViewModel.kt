package com.example.backintime.ui.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SelectedMemoryViewModel : ViewModel() {
    private val _navigateToEditMemory = MutableLiveData<Boolean>()
    val navigateToEditMemory: LiveData<Boolean> get() = _navigateToEditMemory

    fun onEditMemoryClicked() {
        _navigateToEditMemory.value = true
    }

    fun onEditMemoryNavigated() {
        _navigateToEditMemory.value = false
    }
}