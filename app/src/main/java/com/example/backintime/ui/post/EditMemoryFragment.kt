package com.example.backintime.ui.post

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.example.backintime.R
import com.example.backintime.api.RetrofitInstance
import com.example.backintime.databinding.FragmentEditMemoryBinding
import com.example.backintime.utils.CloudinaryHelper
import com.example.backintime.viewModel.ProgressViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditMemoryFragment : Fragment() {

    private var _binding: FragmentEditMemoryBinding? = null
    private val binding get() = _binding

    private val args: EditMemoryFragmentArgs by navArgs()
    private var capturedImageUri: Uri? = null

    private val progressViewModel: ProgressViewModel by activityViewModels()

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        binding?.let { safeBinding ->
            if (success) {
                capturedImageUri?.let { uri ->
                    safeBinding.editMemoryImage.setImageURI(uri)
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
                safeBinding.editMemoryImage.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditMemoryBinding.inflate(inflater, container, false)
        return binding?.root ?: View(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeBinding = binding ?: return

        val memory: TimeCapsule = args.timeCapsule

        safeBinding.editMemoryTitle.setText(memory.title)
        safeBinding.editMemoryDescription.setText(memory.content)
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        safeBinding.memoryDateInput.setText(sdf.format(memory.openDate))
        if (memory.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(memory.imageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(safeBinding.editMemoryImage)
        }

        val emojiAutoComplete = safeBinding.emojiAutoCompleteTextView
        emojiAutoComplete.setText(memory.moodEmoji, false)
        lifecycleScope.launch {
            try {
                val emojiList = RetrofitInstance.api.getAllEmojis()
                val emojiStrings = emojiList.map { emoji ->
                    Html.fromHtml(emoji.htmlCode.firstOrNull() ?: "&#128528;", Html.FROM_HTML_MODE_LEGACY).toString()
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, emojiStrings)
                emojiAutoComplete.setAdapter(adapter)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load emojis", Toast.LENGTH_SHORT).show()
            }
        }

        safeBinding.addFromGalleryButton.setOnClickListener {
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
                            takePictureLauncher.launch(capturedImageUri)
                        }
                        1 -> {
                            pickImageLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }

        safeBinding.memoryDateInput.setOnClickListener {
            showDatePickerDialog()
        }

        safeBinding.updateMemoryButton.setOnClickListener {
            val newTitle = safeBinding.editMemoryTitle.text.toString().trim()
            val newContent = safeBinding.editMemoryDescription.text.toString().trim()
            val dateText = safeBinding.memoryDateInput.text.toString().trim()

            if (newTitle.isEmpty() || newContent.isEmpty() || dateText.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val openDate = try {
                sdf.parse(dateText)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val selectedEmoji = emojiAutoComplete.text.toString().trim()
            if (selectedEmoji.isEmpty()) {
                Toast.makeText(requireContext(), "Please select an emoji", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressViewModel.setLoading(true)
            capturedImageUri?.let { uri ->
                CloudinaryHelper(requireContext()).uploadImage(
                    uri,
                    onSuccess = { imageUrl ->
                        val updatedMemory = memory.copy(
                            title = newTitle,
                            content = newContent,
                            openDate = openDate,
                            imageUrl = imageUrl,
                            moodEmoji = selectedEmoji
                        )
                        updateMemory(updatedMemory)
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), "Image upload error: $error", Toast.LENGTH_SHORT).show()
                        progressViewModel.setLoading(false)
                    }
                )
            } ?: run {
                val updatedMemory = memory.copy(
                    title = newTitle,
                    content = newContent,
                    openDate = openDate,
                    moodEmoji = selectedEmoji
                )
                updateMemory(updatedMemory)
            }
        }
    }

    private fun updateMemory(updatedMemory: TimeCapsule) {
        FirebaseFirestore.getInstance()
            .collection("time_capsules")
            .document(updatedMemory.id)
            .update(
                mapOf(
                    "title" to updatedMemory.title,
                    "content" to updatedMemory.content,
                    "openDate" to updatedMemory.openDate,
                    "imageUrl" to updatedMemory.imageUrl,
                    "moodEmoji" to updatedMemory.moodEmoji
                )
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Memory updated", Toast.LENGTH_SHORT).show()
                progressViewModel.setLoading(false)
                lifecycleScope.launch(Dispatchers.IO) {
                    val localDb = AppLocalDb.getDatabase(requireContext())
                    localDb.timeCapsuleDao().insertTimeCapsule(
                        TimeCapsuleEntity(
                            firebaseId = updatedMemory.id,
                            title = updatedMemory.title,
                            content = updatedMemory.content,
                            openDate = updatedMemory.openDate,
                            imageUrl = updatedMemory.imageUrl,
                            creatorName = updatedMemory.creatorName,
                            creatorId = updatedMemory.creatorId,
                            moodEmoji = updatedMemory.moodEmoji
                        )
                    )
                    withContext(Dispatchers.Main) {
                        findNavController().popBackStack()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update memory: ${e.message}", Toast.LENGTH_SHORT).show()
                progressViewModel.setLoading(false)
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


