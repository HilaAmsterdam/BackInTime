package com.example.backintime.ui.auth

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    // במקום להשתמש ב-!! נשתמש ב-get() עם בדיקה בטוחה למטה
    private val binding get() = _binding

    // שמירת ה-URI של התמונה שנבחרה או צולמה
    private var capturedImageUri: Uri? = null

    // מפעיל לפתיחת מצלמה
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val safeBinding = binding ?: return@registerForActivityResult
        if (success) {
            // אם הצילום הצליח, מציגים את התמונה ב-ImageView
            capturedImageUri?.let { uri ->
                safeBinding.editProfileImage.setImageURI(uri)
            }
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // מפעיל לבחירת תמונה מהגלריה
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
        // יוצרים Binding מקומי ושומרים ב-_binding
        val fragmentBinding = FragmentEditProfileBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        // מחזירים את root של ה-Binding
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
                    Toast.makeText(requireContext(), "Failed to load current profile image", Toast.LENGTH_SHORT).show()
                }
        }

        // כפתור לשינוי תמונה (מצלמה/גלריה)
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

        // כפתור שמירה: עדכון אימייל/סיסמה ותמונה (אם נבחרה)
        safeBinding.saveProfileButton.setOnClickListener {
            val newEmail = safeBinding.editProfileEmail.text.toString().trim()
            val newPassword = safeBinding.editProfilePassword.text.toString().trim()
            val confirmPassword = safeBinding.confirmProfilePassword.text.toString().trim()

            // בדיקת סיסמאות
            if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val pendingUpdates = AtomicInteger(0)

                // עדכון אימייל
                if (newEmail != user.email) {
                    pendingUpdates.incrementAndGet()
                    user.updateEmail(newEmail).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Email updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                        if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                            findNavController().popBackStack()
                        }
                    }
                }

                // עדכון סיסמה
                if (newPassword.isNotEmpty()) {
                    pendingUpdates.incrementAndGet()
                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
                            // מעדכנים את profileImageUrl ב-Firestore
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .update("profileImageUrl", imageUrl)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show()
                                    // מציגים את התמונה החדשה
                                    Picasso.get()
                                        .load(imageUrl)
                                        .placeholder(R.drawable.baseline_account_circle_24)
                                        .error(R.drawable.baseline_account_circle_24)
                                        .into(safeBinding.editProfileImage)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to update profile image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener {
                                    if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                        findNavController().popBackStack()
                                    }
                                }
                        },
                        onFailure = { error ->
                            Toast.makeText(requireContext(), "Image upload error: $error", Toast.LENGTH_SHORT).show()
                            if (pendingUpdates.decrementAndGet() == 0 && isAdded) {
                                findNavController().popBackStack()
                            }
                        }
                    )
                }

                // אם לא בוצעו עדכונים בכלל, נחזור מיד
                if (pendingUpdates.get() == 0 && isAdded) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    // יצירת קובץ זמני עבור תמונת מצלמה
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
