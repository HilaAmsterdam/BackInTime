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
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.databinding.FragmentMyMemoriesBinding
import com.example.backintime.ui.post.FeedAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyMemoriesFragment : Fragment() {

    private var _binding: FragmentMyMemoriesBinding? = null
    private val binding get() = _binding

    private val myCapsules = mutableListOf<TimeCapsule>()
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

        _binding?.let { bindingNonNull ->
            adapter = FeedAdapter(myCapsules) { selectedCapsule ->
                val action = MyMemoriesFragmentDirections.actionMyMemoriesFragmentToSelectedMemoryFragment(selectedCapsule)
                bindingNonNull.root.findNavController().navigate(action)
            }
            bindingNonNull.recyclerViewMyMemories.layoutManager = LinearLayoutManager(requireContext())
            bindingNonNull.recyclerViewMyMemories.adapter = adapter
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
            withContext(Dispatchers.Main) {
                myCapsules.clear()
                myCapsules.addAll(capsulesList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
