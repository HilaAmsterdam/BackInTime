package com.example.backintime.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.example.backintime.Model.User
import com.example.backintime.Repository.TimeCapsuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimeCapsuleViewModel(private val repository: TimeCapsuleRepository) : ViewModel() {

    private val _capsules = MutableLiveData<List<TimeCapsuleEntity>>()
    val capsules: LiveData<List<TimeCapsuleEntity>> get() = _capsules

    fun loadCapsules() {
        viewModelScope.launch {
            _capsules.value = repository.getAllTimeCapsules()
        }
    }

    fun insertCapsule(capsule: TimeCapsuleEntity) {
        viewModelScope.launch {
            repository.insertTimeCapsule(capsule)
            loadCapsules()
        }
    }

    fun updateCapsule(capsule: TimeCapsuleEntity) {
        viewModelScope.launch {
            repository.updateTimeCapsule(capsule)
            loadCapsules()
        }
    }

    fun deleteCapsule(capsule: TimeCapsuleEntity) {
        viewModelScope.launch {
            repository.deleteTimeCapsule(capsule)
            loadCapsules()
        }
    }

    fun getCapsuleById(id: String, callback: (TimeCapsuleEntity?) -> Unit) {
        viewModelScope.launch {
            val capsule = repository.getTimeCapsuleById(id)
            withContext(Dispatchers.Main) {
                callback(capsule)
            }
        }
    }

    fun insertUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }
}
