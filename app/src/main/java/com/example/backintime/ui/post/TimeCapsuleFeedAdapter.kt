package com.example.backintime.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.R
import com.example.backintime.databinding.ItemMemoryBinding
import com.squareup.picasso.Picasso

class TimeCapsuleFeedAdapter(private val memories: MutableList<TimeCapsule>) :
    RecyclerView.Adapter<TimeCapsuleFeedAdapter.MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val binding = ItemMemoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        holder.bind(memories[position])
    }

    override fun getItemCount(): Int = memories.size

    class MemoryViewHolder(private val binding: ItemMemoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(memory: TimeCapsule) {
            if (memory.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(memory.imageUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(binding.memoryImage)
            } else {
                binding.memoryImage.setImageResource(R.drawable.baseline_account_circle_24)
            }
            binding.memoryTitle.text = memory.title
            binding.memoryDate.text = memory.openDate.toString()
            //binding.memoryEmail.text = memory.userEmail
        }
    }
}
