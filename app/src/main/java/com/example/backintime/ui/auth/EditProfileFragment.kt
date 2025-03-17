package com.example.backintime.ui.auth

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.backintime.R
import com.example.backintime.databinding.FragmentEditProfileBinding
import com.example.backintime.utils.CloudinaryHelper
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding

    private var capturedImageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val safeBinding = binding ?: return@registerForActivityResult
        if (success) {
            capturedImageUri?.let { uri ->
                safeBinding.editProfileImage.setImageURI(uri)
                Log.d("EditProfile", "Image captured successfully: $uri")
            }
        } else {
            Log.e("EditProfile", "Failed to capture image")
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val safeBinding = binding ?: return@registerForActivityResult
        if (uri != null) {
            capturedImageUri = uri
            safeBinding.editProfileImage.setImageURI(uri)
            Log.d("EditProfile", "Image selected from gallery: $uri")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentEditProfileBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeBinding = binding ?: return

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            safeBinding.editProfileEmail.setText(currentUser.email)
            Log.d("EditProfile", "Current email: ${currentUser.email}")
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (!isAdded) return@addOnSuccessListener
                    val url = doc.getString("profileImageUrl")
                    Log.d("EditProfile", "Loaded profileImageUrl: $url")
                    if (!url.isNullOrEmpty()) {
                        Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .error(R.drawable.baseline_account_circle_24)
                            .into(safeBinding.editProfileImage)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfile", "Failed to load profile image: ${e.message}")
                    if (isAdded && context != null) {
                        Toast.makeText(context, "Failed to load current profile image", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        safeBinding.editProfileImageFab.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")
            AlertDialog.Builder(requireContext())
                .setTitle("Choose Image Source")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val imageFile = createImageFile()
                            capturedImageUri = FileProvider.getUriForFile(
                                requireContext(),
                                "com.example.backintime.fileprovider",
                                imageFile
                            )
                            Log.d("EditProfile", "Launching camera with URI: $capturedImageUri")
                            takePictureLauncher.launch(capturedImageUri)
                        }
                        1 -> {
                            Log.d("EditProfile", "Launching gallery picker")
                            pickImageLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }

        safeBinding.saveProfileButton.setOnClickListener {
            val newEmail = safeBinding.editProfileEmail.text.toString().trim()
            val newPassword = safeBinding.editProfilePassword.text.toString().trim()
            val confirmPassword = safeBinding.confirmProfilePassword.text.toString().trim()

            if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                Log.d("EditProfile", "Passwords do not match.")
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val isEmailProvider = user.providerData.any { it.providerId == "password" }
                if (!isEmailProvider) {
                    Toast.makeText(context, "Email update not supported for this sign-in provider", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val pendingUpdates = AtomicInteger(0)

                if (newEmail != user.email) {
                    Log.d("EditProfile", "Attempting to update email from ${user.email} to $newEmail")
                    pendingUpdates.incrementAndGet()
                    user.updateEmail(newEmail).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("EditProfile", "Email update successful.")
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .update("email", newEmail)
                                .addOnSuccessListener {
                                    Log.d("EditProfile", "Firestore email update successful.")
                                    Toast.makeText(context, "Email updated", Toast.LENGTH_SHORT).show()
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("EditProfile", "Firestore email update failed: ${e.message}")
                                    Toast.makeText(context, "Failed to update email in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                        } else {
                            Log.e("EditProfile", "Email update failed: ${task.exception}")
                            if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                                Log.d("EditProfile", "Re-authentication required for email update.")
                                showReauthenticationDialog(user, newEmail) { success ->
                                    if (success) {
                                        Log.d("EditProfile", "Email updated after re-authentication.")
                                        Toast.makeText(context, "Email updated after re-authentication", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("EditProfile", "Failed to update email after re-authentication.")
                                        Toast.makeText(context, "Failed to update email after re-authentication", Toast.LENGTH_SHORT).show()
                                    }
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Failed to update email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }

                if (newPassword.isNotEmpty()) {
                    Log.d("EditProfile", "Attempting to update password.")
                    pendingUpdates.incrementAndGet()
                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("EditProfile", "Password update successful.")
                            Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("EditProfile", "Password update failed: ${task.exception}")
                            Toast.makeText(context, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                        if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                            findNavController().popBackStack()
                        }
                    }
                }

                val localUri = capturedImageUri
                if (localUri != null) {
                    Log.d("EditProfile", "Attempting to upload new profile image: $localUri")
                    pendingUpdates.incrementAndGet()
                    CloudinaryHelper(requireContext()).uploadImage(
                        localUri,
                        onSuccess = { imageUrl ->
                            Log.d("EditProfile", "Image upload successful. URL: $imageUrl")
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .update("profileImageUrl", imageUrl)
                                .addOnSuccessListener {
                                    Log.d("EditProfile", "Firestore profile image update successful.")
                                    Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                                    Picasso.get()
                                        .load(imageUrl)
                                        .placeholder(R.drawable.baseline_account_circle_24)
                                        .error(R.drawable.baseline_account_circle_24)
                                        .into(safeBinding.editProfileImage)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("EditProfile", "Firestore profile image update failed: ${e.message}")
                                    Toast.makeText(context, "Failed to update profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener {
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                        },
                        onFailure = { error ->
                            Log.e("EditProfile", "Image upload error: $error")
                            Toast.makeText(context, "Image upload error: $error", Toast.LENGTH_SHORT).show()
                            if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                findNavController().popBackStack()
                            }
                        }
                    )
                }

                if (pendingUpdates.get() == 0 && isAdded) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun showReauthenticationDialog(user: FirebaseUser, newEmail: String, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Re-authentication required")
        val input = EditText(requireContext())
        input.hint = "Enter current password"
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)
        builder.setPositiveButton("Confirm") { dialog, which ->
            val currentPassword = input.text.toString()
            Log.d("EditProfile", "Reauthentication: user entered password.")
            if (currentPassword.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        Log.d("EditProfile", "Reauthentication successful.")
                        user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                            Log.d("EditProfile", "Email update after reauthentication: success=${updateTask.isSuccessful}")
                            callback(updateTask.isSuccessful)
                        }
                    } else {
                        Log.e("EditProfile", "Reauthentication failed: ${reauthTask.exception}")
                        callback(false)
                    }
                }
            } else {
                Log.e("EditProfile", "Reauthentication failed: no password provided.")
                callback(false)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
            callback(false)
        }
        builder.show()
    }

    private fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
