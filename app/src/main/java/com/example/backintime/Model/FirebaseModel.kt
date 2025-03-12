package com.example.backintime.Model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseModel {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Register a new user with email, password, and optional profile image URL
    fun registerUser(
        email: String,
        password: String,
        profileImageUrl: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    // Prepare user data with optional profile image URL
                    val user = hashMapOf(
                        "uid" to uid,
                        "email" to email
                    )
                    if (!profileImageUrl.isNullOrBlank()) {
                        user["profileImageUrl"] = profileImageUrl
                    }
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener { e -> onComplete(false, e.message) }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    // Login user with email and password
    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    // Check if a user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Logout the current user
    fun logoutUser() {
        auth.signOut()
    }
}
