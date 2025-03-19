package com.example.backintime.ui.feed

import android.os.Bundle
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
import java.util.Calendar
import java.util.Locale

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding

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

        safeBinding.swipeRefreshLayout.setOnRefreshListener {
            SyncManager.listenFirebaseDataToRoom(requireContext())
            fetchCapsulesFromRoom()
        }

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
            val sortedCapsules = capsulesList.sortedBy { it.openDate }
            val groupedItems = prepareFeedItems(sortedCapsules)
            withContext(Dispatchers.Main) {
                feedItems.clear()
                feedItems.addAll(groupedItems)
                adapter.notifyDataSetChanged()
                binding?.swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    private fun prepareFeedItems(capsules: List<TimeCapsule>): List<FeedItem> {
        val items = mutableListOf<FeedItem>()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val tomorrowStart = todayStart + 24 * 60 * 60 * 1000L

        val openedCapsules = capsules.filter { it.openDate < todayStart }       // עבר
        val todayCapsules = capsules.filter { it.openDate in todayStart until tomorrowStart } // היום
        val futureCapsules = capsules.filter { it.openDate >= tomorrowStart }   // עתידי

        if (todayCapsules.isNotEmpty()) {
            items.add(FeedItem.Header("TODAY MEMORIES"))
            todayCapsules.sortedBy { it.openDate }
                .forEach { items.add(FeedItem.Post(it)) }
        }

        if (futureCapsules.isNotEmpty()) {
            items.add(FeedItem.Header("UPCOMING MEMORIES"))

            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val groupedUpcoming = futureCapsules.groupBy { dateFormat.format(it.openDate) }
            val sortedUpcomingKeys = groupedUpcoming.keys.sortedBy {
                dateFormat.parse(it)?.time ?: Long.MAX_VALUE
            }
            for (date in sortedUpcomingKeys) {
                items.add(FeedItem.Header(date))
                groupedUpcoming[date]
                    ?.sortedBy { it.openDate }
                    ?.forEach { items.add(FeedItem.Post(it)) }
            }
        }
        if (openedCapsules.isNotEmpty()) {
            items.add(FeedItem.Header("OPENED MEMORIES"))
            openedCapsules.sortedBy { it.openDate }
                .forEach { items.add(FeedItem.Post(it)) }
        }

        return items
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
