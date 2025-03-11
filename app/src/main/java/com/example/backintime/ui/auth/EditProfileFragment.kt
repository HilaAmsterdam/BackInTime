package com.example.backintime.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.backintime.R
import com.example.backintime.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.atomic.AtomicInteger

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding?.root ?: inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeBinding = binding ?: return

        // Populate the fields with the current user's data
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            safeBinding.editProfileEmail.setText(currentUser.email)
        }

        // Save button: update user data on Firebase
        safeBinding.saveProfileButton.setOnClickListener {
            val newEmail = safeBinding.editProfileEmail.text.toString().trim()
            val newPassword = safeBinding.editProfilePassword.text.toString().trim()
            val confirmPassword = safeBinding.confirmProfilePassword.text.toString().trim()

            if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val pendingUpdates = AtomicInteger(0)
                // Update email if needed
                if (newEmail != user.email) {
                    pendingUpdates.incrementAndGet()
                    user.updateEmail(newEmail).addOnCompleteListener { task ->
                        context?.let { ctx ->
                            if (task.isSuccessful) {
                                Toast.makeText(ctx, "Email updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Failed to update email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                            findNavController().popBackStack() // חוזרים למסך הקודם (Memory)
                        }
                    }
                }
                // Update password if provided
                if (newPassword.isNotEmpty()) {
                    pendingUpdates.incrementAndGet()
                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                        context?.let { ctx ->
                            if (task.isSuccessful) {
                                Toast.makeText(ctx, "Password updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                            findNavController().popBackStack() // חוזרים למסך הקודם (Memory)
                        }
                    }
                }
                // אם לא בוצעו עדכונים, חוזרים מיד למסך הקודם (Memory)
                if (pendingUpdates.get() == 0 && isAdded) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
