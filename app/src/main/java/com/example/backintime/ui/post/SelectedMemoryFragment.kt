package com.example.backintime.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.backintime.R
import com.example.backintime.databinding.FragmentSelectedMemoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class SelectedMemoryFragment : Fragment() {

    private var _binding: FragmentSelectedMemoryBinding? = null
    private val binding get() = _binding

    // Retrieve the passed TimeCapsule argument using Safe Args
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

            // Set the memory data in the UI
            safeBinding.memoryTitle.text = capsule.title
            safeBinding.memoryDescription.text = capsule.content
            safeBinding.memoryEmail.text = capsule.creatorName

            // Format and display the open date (assuming openDate is in milliseconds)
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            safeBinding.memoryDate.text = dateFormat.format(capsule.openDate)

            // Load the memory image
            if (capsule.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(capsule.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(safeBinding.memoryImage)
            } else {
                safeBinding.memoryImage.setImageResource(R.drawable.ic_profile_placeholder)
            }
            val currentUser= FirebaseAuth.getInstance().currentUser
            val isOwner= currentUser?.uid == capsule.creatorId

            if(isOwner)
            {
                safeBinding.goToEditMemoryFab.visibility = View.VISIBLE
                safeBinding.deleteMemoryFab.visibility = View.VISIBLE
            }
            else
            {
                safeBinding.goToEditMemoryFab.visibility = View.GONE
                safeBinding.deleteMemoryFab.visibility = View.GONE
            }



            // Fetch and display the user’s profile image using the capsule.creatorId
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(capsule.creatorId)
                .get()
                .addOnSuccessListener { document ->
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get()
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(safeBinding.userProfileImage)
                    } else {
                        safeBinding.userProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
