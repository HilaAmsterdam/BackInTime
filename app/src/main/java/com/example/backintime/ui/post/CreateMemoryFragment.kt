package com.example.backintime.ui.post

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.example.backintime.R
import com.example.backintime.api.RetrofitInstance
import com.example.backintime.databinding.FragmentCreateMemoryBinding
import com.example.backintime.utils.CloudinaryHelper
import com.example.backintime.viewModel.ProgressViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateMemoryFragment : Fragment() {

    private var _binding: FragmentCreateMemoryBinding? = null
    private val binding get() = _binding
    private var capturedImageUri: Uri? = null
    private val progressViewModel: ProgressViewModel by activityViewModels()

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        binding?.let { safeBinding ->
            if (success) {
                capturedImageUri?.let { uri ->
                    Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .error(R.drawable.baseline_account_circle_24)
                        .into(safeBinding.imagePreview)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        binding?.let { safeBinding ->
            if (uri != null) {
                capturedImageUri = uri
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(safeBinding.imagePreview)
            }
        }
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): android.view.View? {
        _binding = FragmentCreateMemoryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        val safeBinding = binding ?: return

        safeBinding.addFromGallaryButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        safeBinding.captureMemoryButton.setOnClickListener {
            val imageFile = createImageFile()
            capturedImageUri = FileProvider.getUriForFile(requireContext(), "com.example.backintime.fileprovider", imageFile)
            takePictureLauncher.launch(capturedImageUri)
        }

        safeBinding.memoryDateInput.setOnClickListener {
            showDatePickerDialog()
        }

        val emojiAutoComplete = safeBinding.emojiAutoCompleteTextView
        lifecycleScope.launch {
            try {
                val emojiList = RetrofitInstance.api.getAllEmojis()
                val emojiStrings = emojiList.map { emoji ->
                    val htmlCode = emoji.htmlCode.firstOrNull() ?: "&#128528;"
                    Html.fromHtml(htmlCode, Html.FROM_HTML_MODE_LEGACY).toString()
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, emojiStrings)
                emojiAutoComplete.setAdapter(adapter)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load emojis: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        emojiAutoComplete.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                (view as? AutoCompleteTextView)?.showDropDown()
                emojiAutoComplete.threshold = 0
            }
        }

        safeBinding.publishMemoryFab.setOnClickListener {
            // מניעת לחיצה חוזרת על הכפתור
            safeBinding.publishMemoryFab.isEnabled = false

            val title = safeBinding.memoryTitleInput.text?.toString()?.trim() ?: ""
            val caption = safeBinding.memoryCaptionInput.text?.toString()?.trim() ?: ""
            val dateText = safeBinding.memoryDateInput.text?.toString()?.trim() ?: ""
            val selectedEmoji = emojiAutoComplete.text.toString().trim()

            if (title.isEmpty() || caption.isEmpty() || dateText.isEmpty() || selectedEmoji.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                safeBinding.publishMemoryFab.isEnabled = true
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
                safeBinding.publishMemoryFab.isEnabled = true
                return@setOnClickListener
            }

            if (capturedImageUri != null) {
                progressViewModel.setLoading(true)
                capturedImageUri?.let { uri ->
                    CloudinaryHelper(requireContext()).uploadImage(
                        uri,
                        onSuccess = { imageUrl ->
                            val capsule = TimeCapsule(
                                id = "",
                                title = title,
                                content = caption,
                                openDate = openDate,
                                imageUrl = imageUrl,
                                creatorName = user.email ?: "",
                                creatorId = user.uid,
                                moodEmoji = selectedEmoji
                            )
                            val db = FirebaseFirestore.getInstance()
                            val docRef = db.collection("time_capsules").document()
                            capsule.id = docRef.id
                            docRef.set(capsule)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Time Capsule created!", Toast.LENGTH_SHORT).show()
                                    CoroutineScope(Dispatchers.IO).launch {
                                        AppLocalDb.getDatabase(requireContext()).timeCapsuleDao().insertTimeCapsule(
                                            TimeCapsuleEntity(
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
                                        )
                                    }
                                    val action = CreateMemoryFragmentDirections.actionCreateMemoryFragmentToFeedFragment()
                                    findNavController().navigate(action)
                                    progressViewModel.setLoading(false)
                                    safeBinding.publishMemoryFab.isEnabled = true
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to create Time Capsule: ${e.message}", Toast.LENGTH_SHORT).show()
                                    progressViewModel.setLoading(false)
                                    safeBinding.publishMemoryFab.isEnabled = true
                                }
                        },
                        onFailure = { error ->
                            Toast.makeText(requireContext(), "Image upload error: $error", Toast.LENGTH_SHORT).show()
                            progressViewModel.setLoading(false)
                            safeBinding.publishMemoryFab.isEnabled = true
                        }
                    )
                }
            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                safeBinding.publishMemoryFab.isEnabled = true
            }
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
