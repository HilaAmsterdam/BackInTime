package com.example.backintime.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.Model.FeedItem
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.databinding.FragmentMyMemoriesBinding
import com.example.backintime.viewModel.ProgressViewModel
import com.example.backintime.viewModel.TimeCapsuleViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MyMemoriesFragment : Fragment() {

    private var _binding: FragmentMyMemoriesBinding? = null
    private val binding get() = _binding
    private val feedItems = mutableListOf<FeedItem>()
    private val progressViewModel: ProgressViewModel by activityViewModels()
    private lateinit var adapter: FeedAdapter
    private lateinit var viewModel: TimeCapsuleViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyMemoriesBinding.inflate(inflater, container, false)
        viewModel = (requireActivity() as androidx.fragment.app.FragmentActivity).let {
            androidx.lifecycle.ViewModelProvider(it).get(TimeCapsuleViewModel::class.java)
        }
        return binding?.root ?: View(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bindingNonNull = binding ?: return
        adapter = FeedAdapter(feedItems) { selectedCapsule ->
            val action = MyMemoriesFragmentDirections.actionMyMemoriesFragmentToSelectedMemoryFragment(selectedCapsule)
            bindingNonNull.root.findNavController().navigate(action)
        }
        bindingNonNull.recyclerViewMyMemories.layoutManager = LinearLayoutManager(requireContext())
        bindingNonNull.recyclerViewMyMemories.adapter = adapter
        bindingNonNull.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadCapsules()
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        progressViewModel.setLoading(true)
        viewModel.capsules.observe(viewLifecycleOwner) { capsules ->
            val capsulesList = capsules.map { entity ->
                TimeCapsule(
                    id = entity.firebaseId,
                    title = entity.title,
                    content = entity.content,
                    openDate = entity.openDate,
                    imageUrl = entity.imageUrl,
                    creatorName = entity.creatorName,
                    creatorId = entity.creatorId,
                    moodEmoji = entity.moodEmoji
                )
            }
            val userCapsules = capsulesList.filter { it.creatorId == user.uid }
            val sortedCapsules = userCapsules.sortedBy { it.openDate }
            feedItems.clear()
            feedItems.addAll(prepareFeedItems(sortedCapsules))
            adapter.notifyDataSetChanged()
            binding?.swipeRefreshLayout?.isRefreshing = false
            progressViewModel.setLoading(false)
        }
        viewModel.loadCapsules()
    }

    private fun prepareFeedItems(capsules: List<TimeCapsule>): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem")).apply {
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
            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
            val groupedUpcoming = futureCapsules.groupBy { dateFormat.format(it.openDate) }
            val sortedUpcomingKeys = groupedUpcoming.keys.sortedBy { dateFormat.parse(it)?.time ?: Long.MAX_VALUE }
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
