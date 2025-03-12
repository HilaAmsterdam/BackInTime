package com.example.backintime.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.databinding.FragmentFeedBinding
import com.example.backintime.ui.post.TimeCapsuleFeedAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding

    // במקום "memories" עכשיו "capsules" מסוג TimeCapsule
    private val capsules = mutableListOf<TimeCapsule>()
    private lateinit var adapter: TimeCapsuleFeedAdapter
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

        adapter = TimeCapsuleFeedAdapter(capsules)
        safeBinding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        safeBinding.recyclerViewFeed.adapter = adapter

        // מאזינים לשינויים בקולקציה "time_capsules" וממיינים לפי openDate
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("time_capsules")
            .orderBy("openDate")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    capsules.clear()
                    for (doc in snapshot.documents) {
                        val capsule = doc.toObject(TimeCapsule::class.java)
                        if (capsule != null) {
                            capsules.add(capsule)
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
