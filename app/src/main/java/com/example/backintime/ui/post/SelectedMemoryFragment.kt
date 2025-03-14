package com.example.backintime.ui.post

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.Model.SyncManager
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.R
import com.example.backintime.databinding.FragmentSelectedMemoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class SelectedMemoryFragment : Fragment() {

    private var _binding: FragmentSelectedMemoryBinding? = null
    private val binding get() = _binding

    private val args: SelectedMemoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectedMemoryBinding.inflate(inflater, container, false)
        return binding?.root ?: inflater.inflate(R.layout.fragment_selected_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { safeBinding ->
            val capsule = args.timeCapsule

            displayMemory(capsule)
            setupButtons(capsule)
            loadUserProfileImage(capsule.creatorId)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh local data from Firestore to ensure deleted posts are removed from Room.
        SyncManager.listenFirebaseDataToRoom(requireContext())
        refreshMemoryData()
    }

    private fun refreshMemoryData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppLocalDb.getDatabase(requireContext())
            val updatedEntity = db.timeCapsuleDao().getMemoryByFirebaseId(args.timeCapsule.id)
            updatedEntity?.let { entity ->
                val updatedMemory = TimeCapsule(
                    id = entity.firebaseId,
                    title = entity.title,
                    content = entity.content,
                    openDate = entity.openDate,
                    imageUrl = entity.imageUrl,
                    creatorName = entity.creatorName,
                    creatorId = entity.creatorId
                )
                withContext(Dispatchers.Main) {
                    displayMemory(updatedMemory)
                }
            }
        }
    }

    private fun displayMemory(memory: TimeCapsule) {
        binding?.let { safeBinding ->
            safeBinding.memoryTitle.text = memory.title
            safeBinding.memoryDescription.text = memory.content
            safeBinding.memoryEmail.text = memory.creatorName
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            safeBinding.memoryDate.text = dateFormat.format(memory.openDate)

            if (memory.imageUrl.isNotEmpty()) {
                // Show the progress bar before loading the image
                safeBinding.memoryProgressBar.visibility = View.VISIBLE
                Picasso.get()
                    .load(memory.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(safeBinding.memoryImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            safeBinding.memoryProgressBar.visibility = View.GONE
                        }
                        override fun onError(e: Exception?) {
                            safeBinding.memoryProgressBar.visibility = View.GONE
                        }
                    })
            } else {
                safeBinding.memoryImage.setImageResource(R.drawable.ic_profile_placeholder)
                safeBinding.memoryProgressBar.visibility = View.GONE
            }
        }
    }

    private fun setupButtons(capsule: TimeCapsule) {
        binding?.let { safeBinding ->
            // Ensure only the post owner sees the edit and delete buttons.
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null || currentUser.uid != capsule.creatorId) {
                safeBinding.goToEditMemoryFab.visibility = View.GONE
                safeBinding.deleteMemoryFab.visibility = View.GONE
            } else {
                safeBinding.goToEditMemoryFab.visibility = View.VISIBLE
                safeBinding.deleteMemoryFab.visibility = View.VISIBLE

                safeBinding.goToEditMemoryFab.setOnClickListener {
                    val action = SelectedMemoryFragmentDirections.actionSelectedMemoryFragmentToEditMemoryFragment(capsule)
                    safeBinding.root.findNavController().navigate(action)
                }

                safeBinding.deleteMemoryFab.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Memory")
                        .setMessage("Are you sure you want to delete this memory?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            FirebaseFirestore.getInstance()
                                .collection("time_capsules")
                                .document(capsule.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Memory deleted", Toast.LENGTH_SHORT).show()
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val localDb = AppLocalDb.getDatabase(requireContext())
                                        localDb.timeCapsuleDao().deleteTimeCapsule(capsule.id)
                                        withContext(Dispatchers.Main) {
                                            safeBinding.root.findNavController().popBackStack()
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Deletion failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    private fun loadUserProfileImage(creatorId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(creatorId)
            .get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl") ?: ""
                binding?.let { safeBinding ->
                    if (profileImageUrl.isNotEmpty()) {
                        safeBinding.profileProgressBar.visibility = View.VISIBLE
                        Picasso.get()
                            .load(profileImageUrl)
                            .into(safeBinding.userProfileImage, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    safeBinding.profileProgressBar.visibility = View.GONE
                                }
                                override fun onError(e: Exception?) {
                                    safeBinding.profileProgressBar.visibility = View.GONE
                                }
                            })
                    } else {
                        safeBinding.userProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
                        safeBinding.profileProgressBar.visibility = View.GONE
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
