package com.example.backintime.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.Model.FeedItem
import com.example.backintime.Model.SyncManager
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.databinding.FragmentMyMemoriesBinding
import com.example.backintime.ui.post.FeedAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MyMemoriesFragment : Fragment() {

    private var _binding: FragmentMyMemoriesBinding? = null
    private val binding get() = _binding

    private val feedItems = mutableListOf<FeedItem>()
    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMemoriesBinding.inflate(inflater, container, false)
        return _binding?.root ?: View(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { bindingNonNull ->
            adapter = FeedAdapter(feedItems) { selectedCapsule ->
                val action = MyMemoriesFragmentDirections.actionMyMemoriesFragmentToSelectedMemoryFragment(selectedCapsule)
                bindingNonNull.root.findNavController().navigate(action)
            }
            bindingNonNull.recyclerViewMyMemories.layoutManager = LinearLayoutManager(requireContext())
            bindingNonNull.recyclerViewMyMemories.adapter = adapter

            bindingNonNull.swipeRefreshLayout.setOnRefreshListener {
                SyncManager.listenFirebaseDataToRoom(requireContext())
                fetchUserCapsules(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        fetchUserCapsules(user.uid)
    }

    private fun fetchUserCapsules(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppLocalDb.getDatabase(requireContext())
            val capsuleEntities = db.timeCapsuleDao().getTimeCapsulesByCreator(userId)
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
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val now = System.currentTimeMillis()

        val upcomingCapsules = capsules.filter { it.openDate > now }
        val openedCapsules = capsules.filter { it.openDate <= now }

        if (upcomingCapsules.isNotEmpty()) {
            val groupedUpcoming = upcomingCapsules.groupBy { dateFormat.format(it.openDate) }
            val sortedUpcomingKeys = groupedUpcoming.keys.sortedBy { dateFormat.parse(it)?.time ?: Long.MAX_VALUE }
            for (date in sortedUpcomingKeys) {
                items.add(FeedItem.Header("$date"))
                groupedUpcoming[date]?.sortedBy { it.openDate }?.forEach { items.add(FeedItem.Post(it)) }
            }
        }

        if (openedCapsules.isNotEmpty()) {
            items.add(FeedItem.Header("Opened"))
            openedCapsules.sortedBy { it.openDate }.forEach { items.add(FeedItem.Post(it)) }
        }

        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
