package com.example.backintime.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.backintime.R
import com.example.backintime.databinding.FragmentMyMemoriesBinding
import java.util.*

class MyMemoriesFragment : Fragment() {

    private var _binding: FragmentMyMemoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMemoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupedMemories = generateGroupedMemories()

        binding.recyclerViewMyMemories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMyMemories.adapter = MyMemoriesAdapter(groupedMemories)
    }

    private fun generateGroupedMemories(): List<MemoryItem> {
        return listOf(
            MemoryItem.Header("On This Day, 2 Years Ago"),
            MemoryItem.Memory("Post", "Open date", R.drawable.no_photo_uploaded),
            MemoryItem.Memory("Post", "Open date", R.drawable.no_photo_uploaded),

            MemoryItem.Header("On This Day, 4 Years Ago"),
            MemoryItem.Memory("Post", "Open date", R.drawable.no_photo_uploaded),

            MemoryItem.Header("On This Day, 5 Years Ago"),
            MemoryItem.Memory("Post", "Open date", R.drawable.no_photo_uploaded),
            MemoryItem.Memory("Post", "Open date", R.drawable.no_photo_uploaded)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}