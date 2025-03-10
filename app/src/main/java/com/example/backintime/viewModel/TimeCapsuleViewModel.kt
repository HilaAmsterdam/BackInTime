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
            _capsules.value = repository.getAllLocalCapsules()
        }
    }

    fun insertCapsule(capsule: TimeCapsuleEntity) {
        viewModelScope.launch {
            repository.insertCapsule(capsule)
            // לאחר השמירה, טען מחדש כדי לעדכן את רשימת הקפסולות
            loadCapsules()
        }
    }

    fun deleteCapsule(localId: Int) {
        viewModelScope.launch {
            repository.deleteCapsuleByLocalId(localId)
            loadCapsules()
        }
    }
}
