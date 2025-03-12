package com.example.backintime.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.databinding.FragmentMyMemoriesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MyMemoriesFragment : Fragment() {

    private var _binding: FragmentMyMemoriesBinding? = null
    private val binding get() = _binding!!

    // רשימה לאחסון הקפסולות של המשתמש הנוכחי
    private val myCapsules = mutableListOf<TimeCapsule>()
    private lateinit var adapter: FeedAdapter
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMemoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FeedAdapter(myCapsules)
        binding.recyclerViewMyMemories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMyMemories.adapter = adapter

        // בדיקת משתמש מחובר
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // שאילתת Firestore לקבלת הקפסולות שנוצרו ע"י המשתמש הנוכחי
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("time_capsules")
            .whereEqualTo("creatorId", user.uid)
            .orderBy("openDate") // ניתן לשנות את הסדר לפי הצורך
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    myCapsules.clear()
                    for (doc in snapshot.documents) {
                        val capsule = doc.toObject(TimeCapsule::class.java)
                        if (capsule != null) {
                            myCapsules.add(capsule)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove() // מפסיקים להאזין לשינויים
        _binding = null
    }
}
