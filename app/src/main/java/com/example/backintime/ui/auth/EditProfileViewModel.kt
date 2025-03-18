package com.example.backintime.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> get() = _updateStatus

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun updateProfile(newEmail: String, newPassword: String) {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.value = "User not logged in"
            _updateStatus.value = false
            return
        }

        var pendingOperations = 0
        var overallSuccess = true

        fun checkCompletion() {
            if (pendingOperations == 0) {
                _updateStatus.value = overallSuccess
            }
        }

        if (newEmail != user.email) {
            pendingOperations++
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firestore.collection("users").document(user.uid)
                        .update("email", newEmail)
                        .addOnFailureListener { e ->
                            overallSuccess = false
                            _errorMessage.value = "Failed to update email in Firestore: ${e.message}"
                        }
                } else {
                    overallSuccess = false
                    _errorMessage.value = task.exception?.message
                }
                pendingOperations--
                checkCompletion()
            }
        }

        if (newPassword.isNotEmpty()) {
            pendingOperations++
            user.updatePassword(newPassword).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    overallSuccess = false
                    _errorMessage.value = task.exception?.message
                }
                pendingOperations--
                checkCompletion()
            }
        }

        if (pendingOperations == 0) {
            _updateStatus.value = true
        }
    }

    fun updateProfileImage(imageUrl: String) {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.value = "User not logged in"
            _updateStatus.value = false
            return
        }
        firestore.collection("users").document(user.uid)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                _updateStatus.value = true
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to update profile image: ${e.message}"
                _updateStatus.value = false
            }
    }
}
