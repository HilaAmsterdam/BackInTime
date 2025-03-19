package com.example.backintime.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.Model.FeedItem
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.Repository.TimeCapsuleRepository
import com.example.backintime.databinding.FragmentFeedBinding
import com.example.backintime.ui.post.FeedAdapter
import com.example.backintime.viewModel.ProgressViewModel
import com.example.backintime.viewModel.TimeCapsuleViewModel
import com.example.backintime.viewModel.TimeCapsuleViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var viewModel: TimeCapsuleViewModel
    private val progressViewModel by lazy {
        (requireActivity() as androidx.fragment.app.FragmentActivity).run {
            androidx.lifecycle.ViewModelProvider(this).get(ProgressViewModel::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val repository = TimeCapsuleRepository(AppLocalDb.getDatabase(requireContext()).timeCapsuleDao())
        viewModel = androidx.lifecycle.ViewModelProvider(
            requireActivity(),
            TimeCapsuleViewModelFactory(repository)
        ).get(TimeCapsuleViewModel::class.java)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val currentBinding = binding ?: return

        adapter = FeedAdapter(feedItems) { selectedCapsule ->
            val action = FeedFragmentDirections.actionFeedFragmentToSelectedMemoryFragment(selectedCapsule)
            currentBinding.root.findNavController().navigate(action)
        }
        currentBinding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        currentBinding.recyclerViewFeed.adapter = adapter

        currentBinding.swipeRefreshLayout.setOnRefreshListener {
            fetchCapsulesFromFirestore()
        }

        fetchCapsulesFromFirestore()
    }

    private fun fetchCapsulesFromFirestore() {
        progressViewModel.setLoading(true)
        FirebaseFirestore.getInstance()
            .collection("time_capsules")
            .get()
            .addOnSuccessListener { snapshot ->
                val capsulesList = snapshot.documents.mapNotNull { document ->
                    document.toObject(TimeCapsule::class.java)?.copy(id = document.id)
                }
                val sortedCapsules = capsulesList.sortedBy { it.openDate }
                val groupedItems = prepareFeedItems(sortedCapsules)
                feedItems.clear()
                feedItems.addAll(groupedItems)
                adapter.notifyDataSetChanged()
                binding?.swipeRefreshLayout?.isRefreshing = false
                progressViewModel.setLoading(false)
            }
            .addOnFailureListener {
                progressViewModel.setLoading(false)
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
        val openedCapsules = capsules.filter { it.openDate < todayStart }
        val todayCapsules = capsules.filter { it.openDate in todayStart until tomorrowStart }
        val futureCapsules = capsules.filter { it.openDate >= tomorrowStart }

        if (todayCapsules.isNotEmpty()) {
            items.add(FeedItem.Header("TODAY MEMORIES"))
            todayCapsules.sortedBy { it.openDate }.forEach { items.add(FeedItem.Post(it)) }
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
                groupedUpcoming[date]?.sortedBy { it.openDate }?.forEach { items.add(FeedItem.Post(it)) }
            }
        }

        if (openedCapsules.isNotEmpty()) {
            items.add(FeedItem.Header("OPENED MEMORIES"))
            openedCapsules.sortedBy { it.openDate }.forEach { items.add(FeedItem.Post(it)) }
        }

        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
