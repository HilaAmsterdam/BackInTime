package com.example.backintime.ui.memory

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.backintime.R
import com.example.backintime.model.AppLocalDbRepository
import com.example.backintime.model.dao.MemoryEntity
import com.example.backintime.repository.MemoryRepository
import com.example.backintime.viewmodel.MemoryViewModel
import com.example.backintime.viewmodel.MemoryViewModelFactory

class CreateMemoryFragment : Fragment() {

    private val viewModel: MemoryViewModel by viewModels {
        MemoryViewModelFactory(MemoryRepository(AppLocalDbRepository.getDatabase(requireContext()).memoryDao()))
    }

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var saveButton: Button
    private lateinit var imageView: ImageView

    private var selectedImageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_create_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleEditText = view.findViewById(R.id.etMemoryTitle)
        contentEditText = view.findViewById(R.id.etMemoryContent)
        selectImageButton = view.findViewById(R.id.btnSelectImage)
        saveButton = view.findViewById(R.id.btnSaveMemory)
        imageView = view.findViewById(R.id.btnSelectImage)

        selectImageButton.setOnClickListener { openGallery() }
        saveButton.setOnClickListener { saveMemory() }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imageView.setImageURI(selectedImageUri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveMemory() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val openDate = System.currentTimeMillis()
        // כאן אמורים להעלות את התמונה ל־Cloudinary ולקבל URL אמיתי; כרגע נשתמש ב־URI כמשתנה
        val imageUrl = selectedImageUri?.toString() ?: ""
        val creatorId = "CURRENT_USER_ID"       // החליפי במזהה המשתמש האמיתי
        val creatorEmail = "user@example.com"    // החליפי בדוא"ל המשתמש

        if (title.isBlank() || content.isBlank() || imageUrl.isBlank()) {
            Toast.makeText(requireContext(), "fill all fields and choose image", Toast.LENGTH_SHORT).show()
            return
        }

        val memory = MemoryEntity(
            title = title,
            content = content,
            openDate = openDate,
            imageUrl = imageUrl,
            creatorId = creatorId,
            creatorEmail = creatorEmail
        )

        viewModel.addMemory(memory)
        Toast.makeText(requireContext(), "memory saved", Toast.LENGTH_SHORT).show()
    }
}
