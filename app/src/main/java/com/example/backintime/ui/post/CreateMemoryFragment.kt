package com.example.backintime.ui.post

import android.app.DatePickerDialog
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
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.example.backintime.R
import com.example.backintime.databinding.FragmentCreateMemoryBinding
import com.example.backintime.utils.CloudinaryHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateMemoryFragment : Fragment() {

    private var _binding: FragmentCreateMemoryBinding? = null
    private val binding get() = _binding

    private var capturedImageUri: Uri? = null

    // Launcher for taking a picture with the camera
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        binding?.let { safeBinding ->
            if (success) {
                capturedImageUri?.let { uri ->
                    Picasso.get().load(uri)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .error(R.drawable.baseline_account_circle_24)
                        .into(safeBinding.imagePreview)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for picking an image from the gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding?.let { safeBinding ->
            if (uri != null) {
                capturedImageUri = uri
                Picasso.get().load(uri)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(safeBinding.imagePreview)
            }
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

        safeBinding.addFromGallaryButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        safeBinding.captureMemoryButton.setOnClickListener {
            val imageFile = createImageFile()
            capturedImageUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.backintime.fileprovider",
                imageFile
            )
            takePictureLauncher.launch(capturedImageUri)
        }

        safeBinding.memoryDateInput.setOnClickListener {
            showDatePickerDialog()
        }

        safeBinding.publishMemoryFab.setOnClickListener {
            val title = safeBinding.memoryTitleInput.text?.toString()?.trim() ?: ""
            val caption = safeBinding.memoryCaptionInput.text?.toString()?.trim() ?: ""
            val dateText = safeBinding.memoryDateInput.text?.toString()?.trim() ?: ""

            if (title.isEmpty() || caption.isEmpty() || dateText.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val openDate = try {
                sdf.parse(dateText)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (capturedImageUri == null) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CloudinaryHelper(requireContext()).uploadImage(
                capturedImageUri!!,
                onSuccess = { imageUrl ->
                    uploadTimeCapsuleToFirestore(imageUrl, title, caption, openDate, user.email ?: "", user.uid)
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), "Image upload error: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val formattedDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(selectedCalendar.time)
                binding?.memoryDateInput?.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun uploadTimeCapsuleToFirestore(
        imageUrl: String,
        title: String,
        content: String,
        openDate: Long,
        userEmail: String,
        userId: String
    ) {
        val capsule = TimeCapsule(
            id = "",
            title = title,
            content = content,
            openDate = openDate,
            imageUrl = imageUrl,
            creatorName = userEmail,
            creatorId = userId
        )
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("time_capsules").document()
        capsule.id = docRef.id

        docRef.set(capsule)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Time Capsule created!", Toast.LENGTH_SHORT).show()
                // הוספת הזיכרון גם למסד הנתונים המקומי (Room)
                CoroutineScope(Dispatchers.IO).launch {
                    val localDb = AppLocalDb.getDatabase(requireContext())
                    localDb.timeCapsuleDao().insertTimeCapsule(
                        TimeCapsuleEntity(
                            firebaseId = capsule.id,
                            title = capsule.title,
                            content = capsule.content,
                            openDate = capsule.openDate,
                            imageUrl = capsule.imageUrl,
                            creatorName = capsule.creatorName,
                            creatorId = capsule.creatorId
                        )
                    )
                }
                val action = CreateMemoryFragmentDirections.actionCreateMemoryFragmentToFeedFragment()
                findNavController().navigate(action)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to create Time Capsule: ${e.message}", Toast.LENGTH_SHORT).show()
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
