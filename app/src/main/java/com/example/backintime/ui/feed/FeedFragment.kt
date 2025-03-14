package com.example.backintime.ui.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.Model.SyncManager
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.databinding.FragmentFeedBinding
import com.example.backintime.ui.post.FeedAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding

    private val capsules = mutableListOf<TimeCapsule>()
    private lateinit var adapter: FeedAdapter

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

        adapter = FeedAdapter(capsules) { selectedCapsule ->
            val action = FeedFragmentDirections.actionFeedFragmentToSelectedMemoryFragment(selectedCapsule)
            safeBinding.root.findNavController().navigate(action)
        }
        safeBinding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        safeBinding.recyclerViewFeed.adapter = adapter

        // Set up the swipe-to-refresh behavior.
        safeBinding.swipeRefreshLayout.setOnRefreshListener {
            // Start the real-time sync; the data should update accordingly.
            SyncManager.listenFirebaseDataToRoom(requireContext())
            fetchCapsulesFromRoom()
        }

        // Initial load of data from Room
        fetchCapsulesFromRoom()
    }

    // Every time the fragment resumes, we start the real-time sync and refresh data.
    override fun onResume() {
        super.onResume()
        SyncManager.listenFirebaseDataToRoom(requireContext())
        fetchCapsulesFromRoom()
    }

    private fun fetchCapsulesFromRoom() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppLocalDb.getDatabase(requireContext())
            val capsuleEntities = db.timeCapsuleDao().getAllTimeCapsules()
            val capsulesList = capsuleEntities.map { entity ->
                TimeCapsule(
                    id = entity.firebaseId,
                    title = entity.title,
                    content = entity.content,
                    openDate = entity.openDate,
                    imageUrl = entity.imageUrl,
                    creatorName = entity.creatorName,
                    creatorId = entity.creatorId
                )
            }
            withContext(Dispatchers.Main) {
                capsules.clear()
                capsules.addAll(capsulesList.sortedByDescending { it.openDate })
                adapter.notifyDataSetChanged()
                binding?.swipeRefreshLayout?.isRefreshing = false // Stop the progress indicator.
                Log.d("FeedFragment", "Loaded ${capsules.size} capsules from Room")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
