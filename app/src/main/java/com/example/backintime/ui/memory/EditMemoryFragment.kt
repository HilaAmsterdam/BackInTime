package com.example.backintime.ui.memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.backintime.R
import com.example.backintime.model.AppLocalDbRepository
import com.example.backintime.model.dao.MemoryEntity
import com.example.backintime.repository.MemoryRepository
import com.example.backintime.viewmodel.MemoryViewModel
import com.example.backintime.viewmodel.MemoryViewModelFactory

class EditMemoryFragment : Fragment() {

    private val viewModel: MemoryViewModel by viewModels {
        MemoryViewModelFactory(MemoryRepository(AppLocalDbRepository.getDatabase(requireContext()).memoryDao()))
    }

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var updateButton: Button

    private var memoryId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memoryId = arguments?.getInt("memoryId") ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_edit_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleEditText = view.findViewById(R.id.etEditMemoryTitle)
        contentEditText = view.findViewById(R.id.etEditMemoryContent)
        updateButton = view.findViewById(R.id.btnUpdateMemory)



        updateButton.setOnClickListener { updateMemory() }
    }

    private fun updateMemory() {
        val updatedTitle = titleEditText.text.toString()
        val updatedContent = contentEditText.text.toString()
        val updatedMemory = MemoryEntity(
            id = memoryId,
            title = updatedTitle,
            content = updatedContent,
            openDate = System.currentTimeMillis(),
            imageUrl = "", // יש לשמור או לקבל את כתובת התמונה הקיימת
            creatorId = "CURRENT_USER_ID",  // החליפי במזהה המשתמש האמיתי
            creatorEmail = "user@example.com" // החליפי בדוא"ל המשתמש
        )
        viewModel.addMemory(updatedMemory)
    }
}
