package com.example.backintime.ui.post

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
import com.example.backintime.Model.Memory
import com.example.backintime.R
import com.example.backintime.databinding.FragmentCreateMemoryBinding
import com.example.backintime.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.io.File

class CreateMemoryFragment : Fragment() {

    private var _binding: FragmentCreateMemoryBinding? = null
    private val binding get() = _binding

    private var capturedImageUri: Uri? = null

    // מצלמה
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val safeBinding = binding ?: return@registerForActivityResult
        if (success) {
            capturedImageUri?.let { uri ->
                Picasso.get().load(uri).into(safeBinding.imagePreview)
            }
        } else {
            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // גלריה
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val safeBinding = binding ?: return@registerForActivityResult
        if (uri != null) {
            capturedImageUri = uri
            Picasso.get().load(uri).into(safeBinding.imagePreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateMemoryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeBinding = binding ?: return

        // כפתור גלריה
        safeBinding.addFromGallaryButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // כפתור מצלמה
        safeBinding.captureMemoryButton.setOnClickListener {
            val imageFile = createImageFile()
            capturedImageUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.backintime.fileprovider",
                imageFile
            )
            takePictureLauncher.launch(capturedImageUri)
        }

        // כפתור שליחה
        safeBinding.publishMemoryFab.setOnClickListener {
            val caption = safeBinding.publishMemoryFab.textAlignment.toString().trim()
            if (caption.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a caption", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (capturedImageUri == null) {
                // במקרה שאין תמונה, אפשר להעלות זיכרון עם imageUrl ריק, או לחייב תמונה
                uploadMemoryToFirestore("", caption, user.email ?: "")
            } else {
                // העלאת תמונה ל-Cloudinary
                CloudinaryHelper(requireContext()).uploadImage(
                    capturedImageUri!!,
                    onSuccess = { imageUrl ->
                        // מעלים את הזיכרון ל-Firestore עם ה-URL
                        uploadMemoryToFirestore(imageUrl, caption, user.email ?: "")
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), "Image upload error: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun uploadMemoryToFirestore(imageUrl: String, caption: String, userEmail: String) {
        val memory = Memory(
            id = "",
            title = caption,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            userEmail = userEmail
        )
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("memories").document() // יצירת doc אקראי
        memory.id = docRef.id

        docRef.set(memory)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Memory created!", Toast.LENGTH_SHORT).show()
                // אפשר לחזור ל-FeedFragment
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to create memory: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
