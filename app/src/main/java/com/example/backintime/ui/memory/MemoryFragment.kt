package com.example.backintime.ui.memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.R
import com.example.backintime.model.AppLocalDbRepository
import com.example.backintime.repository.MemoryRepository
import com.example.backintime.viewmodel.MemoryViewModel
import com.example.backintime.viewmodel.MemoryViewModelFactory

class MemoryFragment : Fragment() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: GroupedMemoryAdapter
    private val viewModel: MemoryViewModel by viewModels {
        MemoryViewModelFactory(
            MemoryRepository(AppLocalDbRepository.getDatabase(requireContext()).memoryDao())
        )
    }
    private var isUserPosts: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUserPosts = arguments?.getBoolean(ARG_IS_USER_POSTS, false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // ודאי שקובץ fragment_memory.xml כולל RecyclerView עם id="recyclerViewMemory"
        return inflater.inflate(R.layout.fragment_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewMemory)
        adapter = GroupedMemoryAdapter(emptyList()) { memoryItem ->
            Toast.makeText(requireContext(), "Clicked: ${memoryItem.memory.title}", Toast.LENGTH_SHORT).show()
            // כאן ניתן להוסיף ניווט למסך פרטי הפוסט (SelectedMemoryFragment)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        if (isUserPosts) {
            val userId = "CURRENT_USER_ID" // החליפי במזהה המשתמש האמיתי
            viewModel.loadUserMemories(userId)
        } else {
            viewModel.loadAllMemories()
        }

        viewModel.memories.observe(viewLifecycleOwner) { memories ->
            val groupedItems = groupMemoriesByDate(memories)
            adapter.updateList(groupedItems)
        }
    }

    companion object {
        private const val ARG_IS_USER_POSTS = "isUserPosts"
        fun newInstance(isUserPosts: Boolean): MemoryFragment {
            val fragment = MemoryFragment()
            val args = Bundle()
            args.putBoolean(ARG_IS_USER_POSTS, isUserPosts)
            fragment.arguments = args
            return fragment
        }
    }
}
