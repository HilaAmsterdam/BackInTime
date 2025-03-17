package com.example.backintime.viewModel

import androidx.lifecycle.*
import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.example.backintime.Repository.TimeCapsuleRepository
import kotlinx.coroutines.launch

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

    fun deleteCapsule(capsule: TimeCapsuleEntity) {
        viewModelScope.launch {
            repository.deleteTimeCapsule(capsule)
            loadCapsules()
        }
    }
}
