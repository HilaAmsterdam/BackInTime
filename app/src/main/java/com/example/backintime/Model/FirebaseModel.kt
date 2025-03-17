package com.example.backintime.Model

import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseModel {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun fetchTimeCapsulesFromFirebase(onComplete: (List<TimeCapsuleEntity>) -> Unit) {
        db.collection("time_capsules")
            .get()
            .addOnSuccessListener { result ->
                val capsules = result.documents.map { doc ->
                    TimeCapsuleEntity(
                        firebaseId = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        openDate = doc.getLong("openDate") ?: 0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        creatorName = doc.getString("creatorName") ?: "",
                        creatorId = doc.getString("creatorId") ?: "",
                        moodEmoji = doc.getString("moodEmoji") ?: "",
                        notified = doc.getBoolean("notified") ?: false
                    )
                }
                onComplete(capsules)
            }
            .addOnFailureListener { exception ->
                onComplete(emptyList())
            }
    }

    fun listenTimeCapsulesFromFirebase(onUpdate: (List<TimeCapsuleEntity>) -> Unit) {
        db.collection("time_capsules")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val capsules = snapshot?.documents?.map { doc ->
                    TimeCapsuleEntity(
                        firebaseId = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        openDate = doc.getLong("openDate") ?: 0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        creatorName = doc.getString("creatorName") ?: "",
                        creatorId = doc.getString("creatorId") ?: "",
                        moodEmoji = doc.getString("moodEmoji") ?: "",
                        notified = doc.getBoolean("notified") ?: false
                    )
                } ?: emptyList()
                onUpdate(capsules)
            }
    }
}
