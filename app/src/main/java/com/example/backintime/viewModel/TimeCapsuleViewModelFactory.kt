package com.example.backintime.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.backintime.Repository.TimeCapsuleRepository

class TimeCapsuleViewModelFactory(private val repository: TimeCapsuleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeCapsuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeCapsuleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
