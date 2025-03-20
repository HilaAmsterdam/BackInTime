package com.example.backintime.ui.post

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.Model.User
import com.example.backintime.R
import com.example.backintime.databinding.FragmentSelectedMemoryBinding
import com.example.backintime.viewModel.ProgressViewModel
import com.example.backintime.viewModel.TimeCapsuleViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
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
    private val progressViewModel: ProgressViewModel by activityViewModels()
    private lateinit var viewModel: TimeCapsuleViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSelectedMemoryBinding.inflate(inflater, container, false)
        viewModel = (requireActivity() as androidx.fragment.app.FragmentActivity).let {
            androidx.lifecycle.ViewModelProvider(it).get(TimeCapsuleViewModel::class.java)
        }
        return binding?.root ?: inflater.inflate(R.layout.fragment_selected_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.let { safeBinding ->
            val capsule = args.timeCapsule
            displayMemory(capsule)
            setupButtons(capsule)
            loadUserProfileImage(capsule.creatorId)
        }
        refreshMemoryData()
    }

    private fun refreshMemoryData() {
        progressViewModel.setLoading(true)
        viewModel.getCapsuleById(args.timeCapsule.id) { entity ->
            entity?.let {
                val updatedMemory = TimeCapsule(
                    id = it.firebaseId,
                    title = it.title,
                    content = it.content,
                    openDate = it.openDate,
                    imageUrl = it.imageUrl,
                    creatorName = it.creatorName,
                    creatorId = it.creatorId,
                    moodEmoji = it.moodEmoji
                )
                displayMemory(updatedMemory)
            }
            progressViewModel.setLoading(false)
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
            safeBinding.moodTextView.text = memory.moodEmoji
        }
    }

    private fun setupButtons(capsule: TimeCapsule) {
        binding?.let { safeBinding ->
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
                        .setPositiveButton("Yes") { _, _ ->
                            FirebaseFirestore.getInstance()
                                .collection("time_capsules")
                                .document(capsule.id)
                                .delete()
                                .addOnSuccessListener {
                                    val entity = com.example.backintime.Model.Dao.TimeCapsuleEntity(
                                        firebaseId = capsule.id,
                                        title = capsule.title,
                                        content = capsule.content,
                                        openDate = capsule.openDate,
                                        imageUrl = capsule.imageUrl,
                                        creatorName = capsule.creatorName,
                                        creatorId = capsule.creatorId,
                                        notified = false,
                                        moodEmoji = capsule.moodEmoji
                                    )
                                    viewModel.deleteCapsule(entity)
                                    safeBinding.root.findNavController().popBackStack()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to delete from Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    private fun loadUserProfileImage(creatorId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = com.example.backintime.Model.AppLocalDb.getDatabase(requireContext())
            val cachedUser = db.userDao().getUserById(creatorId)
            if (cachedUser != null && cachedUser.profileImageUrl.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    binding?.let { safeBinding ->
                        safeBinding.profileProgressBar.visibility = View.VISIBLE
                        com.squareup.picasso.Picasso.get().load(cachedUser.profileImageUrl)
                            .into(safeBinding.userProfileImage, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    safeBinding.profileProgressBar.visibility = View.GONE
                                }
                                override fun onError(e: Exception?) {
                                    safeBinding.profileProgressBar.visibility = View.GONE
                                }
                            })
                    }
                }
            } else {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(creatorId)
                    .get()
                    .addOnSuccessListener { document ->
                        val profileImageUrl = document.getString("profileImageUrl") ?: ""
                        val email = document.getString("email") ?: ""
                        val user = User(uid = creatorId, email = email, profileImageUrl = profileImageUrl)
                        viewModel.insertUser(user)
                        binding?.let { safeBinding ->
                            if (profileImageUrl.isNotEmpty()) {
                                safeBinding.profileProgressBar.visibility = View.VISIBLE
                                com.squareup.picasso.Picasso.get().load(profileImageUrl)
                                    .into(safeBinding.userProfileImage, object : com.squareup.picasso.Callback {
                                        override fun onSuccess() {
                                            safeBinding.profileProgressBar.visibility = View.GONE
                                        }
                                        override fun onError(e: Exception?) {
                                            safeBinding.profileProgressBar.visibility = View.GONE
                                        }
                                    })
                            } else {
                                safeBinding.userProfileImage.setImageResource(R.drawable.baseline_account_circle_24)
                                safeBinding.profileProgressBar.visibility = View.GONE
                            }
                        }
                    }
                    .addOnFailureListener {
                        binding?.let { safeBinding ->
                            safeBinding.userProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
                            safeBinding.profileProgressBar.visibility = View.GONE
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
