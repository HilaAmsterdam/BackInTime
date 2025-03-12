package com.example.backintime.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.Model.Memory
import com.example.backintime.R
import com.example.backintime.databinding.FragmentFeedBinding
import com.example.backintime.ui.post.MemoryFeedAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding

    private val memories = mutableListOf<Memory>()
    private lateinit var adapter: MemoryFeedAdapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val safeBinding = binding ?: return

        adapter = MemoryFeedAdapter(memories)
        safeBinding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        safeBinding.recyclerViewFeed.adapter = adapter

        // מאזינים לשינויים בקולקציה "memories"
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("memories")
            .orderBy("timestamp") // ממוין לפי הזמן
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    memories.clear()
                    for (doc in snapshot.documents) {
                        val memory = doc.toObject(Memory::class.java)
                        if (memory != null) {
                            memories.add(memory)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove() // מפסיקים להאזין
        _binding = null
    }
}
