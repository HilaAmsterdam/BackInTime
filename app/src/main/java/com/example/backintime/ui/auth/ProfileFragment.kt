package com.example.backintime.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.backintime.Model.FirebaseModel
import com.example.backintime.Model.SyncManager
import com.example.backintime.R
import com.example.backintime.activities.MainActivity
import com.example.backintime.databinding.FragmentProfileBinding
import com.example.backintime.Model.AppLocalDb
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding

    private val firebaseModel = FirebaseModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeBinding = binding ?: return

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            safeBinding.profileEmailEdit.setText(currentUser.email)

            val photoUrl = currentUser.photoUrl?.toString()
            if (!photoUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(safeBinding.imageView)
            } else {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val url = document.getString("profileImageUrl")
                        if (!url.isNullOrEmpty()) {
                            Picasso.get()
                                .load(url)
                                .placeholder(R.drawable.baseline_account_circle_24)
                                .error(R.drawable.baseline_account_circle_24)
                                .into(safeBinding.imageView)
                        } else {
                            Log.d("ProfileFragment", "No profileImageUrl in Firestore.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to load profile image", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileFragment", "Firestore error", e)
                    }
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }

        safeBinding.goToEditProfileFab.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        safeBinding.logoutButton.setOnClickListener {
            firebaseModel.logoutUser()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        safeBinding.deleteAccountFab.setOnClickListener {
            currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    deleteUserPosts(currentUser.uid) { postsDeleted ->
                        if (postsDeleted) {
                            Toast.makeText(requireContext(), "Account and posts deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Account deleted, but failed to delete some posts", Toast.LENGTH_SHORT).show()
                        }
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error deleting account", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteUserPosts(userId: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("time_capsules")
            .whereEqualTo("creatorId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val deleteTasks = querySnapshot.documents.map { document ->
                    document.reference.delete()
                }
                Tasks.whenAllComplete(deleteTasks)
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val db = AppLocalDb.getDatabase(requireContext())
                            val userPosts = db.timeCapsuleDao().getTimeCapsulesByCreator(userId)
                            userPosts.forEach { post ->
                                db.timeCapsuleDao().deleteTimeCapsule(post.firebaseId)
                            }
                            withContext(Dispatchers.Main) {
                                callback(true)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        callback(false)
                    }
            }
            .addOnFailureListener { e ->
                callback(false)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
