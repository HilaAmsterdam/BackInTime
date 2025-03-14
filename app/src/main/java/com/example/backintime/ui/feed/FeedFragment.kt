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
import com.example.backintime.Model.FeedItem
import com.example.backintime.Model.SyncManager
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.databinding.FragmentFeedBinding
import com.example.backintime.ui.post.FeedAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding

    // Use a list of FeedItem for grouped data
    private val feedItems = mutableListOf<FeedItem>()
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

        adapter = FeedAdapter(feedItems) { selectedCapsule ->
            val action = FeedFragmentDirections.actionFeedFragmentToSelectedMemoryFragment(selectedCapsule)
            safeBinding.root.findNavController().navigate(action)
        }
        safeBinding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        safeBinding.recyclerViewFeed.adapter = adapter

        // Set up swipe-to-refresh behavior.
        safeBinding.swipeRefreshLayout.setOnRefreshListener {
            SyncManager.listenFirebaseDataToRoom(requireContext())
            fetchCapsulesFromRoom()
        }

        // Initial load from Room
        fetchCapsulesFromRoom()
    }

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
            // Sort by openDate ascending (closest date first)
            val sortedCapsules = capsulesList.sortedBy { it.openDate }
            // Group the capsules into FeedItems
            val groupedItems = prepareFeedItems(sortedCapsules)
            withContext(Dispatchers.Main) {
                feedItems.clear()
                feedItems.addAll(groupedItems)
                adapter.notifyDataSetChanged()
                binding?.swipeRefreshLayout?.isRefreshing = false
                Log.d("FeedFragment", "Loaded ${feedItems.size} items (headers and posts) from Room")
            }
        }
    }

    private fun prepareFeedItems(capsules: List<TimeCapsule>): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        // Group capsules by the formatted openDate.
        val grouped = capsules.groupBy { dateFormat.format(it.openDate) }
        // Sort the keys (dates) ascending (closest date first)
        val sortedKeys = grouped.keys.sortedBy { dateFormat.parse(it)?.time ?: Long.MAX_VALUE }
        for (date in sortedKeys) {
            items.add(FeedItem.Header(date))
            val posts = grouped[date]?.sortedBy { it.openDate } ?: emptyList()
            posts.forEach { items.add(FeedItem.Post(it)) }
        }
        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
