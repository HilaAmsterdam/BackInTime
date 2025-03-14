package com.example.backintime.ui.auth

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.InputType
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

    // URI של התמונה שנבחרה או צולמה
    private var capturedImageUri: Uri? = null

    // Launcher לפתיחת מצלמה
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val safeBinding = binding ?: return@registerForActivityResult
        if (success) {
            capturedImageUri?.let { uri ->
                safeBinding.editProfileImage.setImageURI(uri)
            }
        } else {
            if (isAdded && context != null) {
                Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher לבחירת תמונה מהגלריה
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val safeBinding = binding ?: return@registerForActivityResult
        if (uri != null) {
            capturedImageUri = uri
            safeBinding.editProfileImage.setImageURI(uri)
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
            // מציגים את האימייל הקיים
            safeBinding.editProfileEmail.setText(currentUser.email)
            // טוענים את התמונה הנוכחית מ-Firestore
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (!isAdded) return@addOnSuccessListener
                    val url = doc.getString("profileImageUrl")
                    if (!url.isNullOrEmpty()) {
                        Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .error(R.drawable.baseline_account_circle_24)
                            .into(safeBinding.editProfileImage)
                    }
                }
                .addOnFailureListener {
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
                        0 -> { // מצלמה
                            val imageFile = createImageFile()
                            capturedImageUri = FileProvider.getUriForFile(
                                requireContext(),
                                "com.example.backintime.fileprovider",
                                imageFile
                            )
                            takePictureLauncher.launch(capturedImageUri)
                        }
                        1 -> { // גלריה
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

            // בדיקת סיסמאות
            if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                if (isAdded && context != null) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val pendingUpdates = AtomicInteger(0)

                // עדכון אימייל – אם שונה מהאימייל הנוכחי
                if (newEmail != user.email) {
                    pendingUpdates.incrementAndGet()
                    user.updateEmail(newEmail).addOnCompleteListener { task ->
                        if (!isAdded) return@addOnCompleteListener
                        if (task.isSuccessful) {
                            // עדכון המסמך ב-Firestore גם כן
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .update("email", newEmail)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Email updated", Toast.LENGTH_SHORT).show()
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to update email in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                        } else {
                            // אם נדרש re-authentication
                            if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                                showReauthenticationDialog(user, newEmail) { success ->
                                    if (!isAdded) return@showReauthenticationDialog
                                    if (success) {
                                        Toast.makeText(context, "Email updated after re-authentication", Toast.LENGTH_SHORT).show()
                                    } else {
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

                // עדכון סיסמה אם נדרש
                if (newPassword.isNotEmpty()) {
                    pendingUpdates.incrementAndGet()
                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (!isAdded) return@addOnCompleteListener
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                        if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                            findNavController().popBackStack()
                        }
                    }
                }

                // העלאת תמונה ל-Cloudinary אם נבחרה תמונה חדשה
                val localUri = capturedImageUri
                if (localUri != null) {
                    pendingUpdates.incrementAndGet()
                    CloudinaryHelper(requireContext()).uploadImage(
                        localUri,
                        onSuccess = { imageUrl ->
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .update("profileImageUrl", imageUrl)
                                .addOnSuccessListener {
                                    if (isAdded) {
                                        Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                                        Picasso.get()
                                            .load(imageUrl)
                                            .placeholder(R.drawable.baseline_account_circle_24)
                                            .error(R.drawable.baseline_account_circle_24)
                                            .into(safeBinding.editProfileImage)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    if (isAdded) {
                                        Toast.makeText(context, "Failed to update profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnCompleteListener {
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                        },
                        onFailure = { error ->
                            if (isAdded) {
                                Toast.makeText(context, "Image upload error: $error", Toast.LENGTH_SHORT).show()
                            }
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
            if (currentPassword.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (!isAdded) return@addOnCompleteListener
                    if (reauthTask.isSuccessful) {
                        user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                            callback(updateTask.isSuccessful)
                        }
                    } else {
                        callback(false)
                    }
                }
            } else {
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
