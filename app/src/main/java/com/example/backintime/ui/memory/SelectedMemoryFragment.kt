package com.example.backintime.ui.memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.backintime.R
import com.example.backintime.model.dao.MemoryEntity

class SelectedMemoryFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var emailTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_selected_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageView = view.findViewById(R.id.selectedMemoryImage)
        titleTextView = view.findViewById(R.id.selectedMemoryTitle)
        contentTextView = view.findViewById(R.id.selectedMemoryContent)
        dateTextView = view.findViewById(R.id.selectedMemoryDate)
        emailTextView = view.findViewById(R.id.selectedMemoryEmail)

        // קבלת האובייקט MemoryEntity שהועבר כארגומנט (באמצעות SafeArgs או Bundle)
        val memory: MemoryEntity? = arguments?.getParcelable("memory")
        memory?.let {
            titleTextView.text = it.title
            contentTextView.text = it.content
            dateTextView.text = it.openDate.toString()
            emailTextView.text = it.creatorEmail
            Glide.with(requireContext())
                .load(it.imageUrl)
                .into(imageView)
        }
    }
}
