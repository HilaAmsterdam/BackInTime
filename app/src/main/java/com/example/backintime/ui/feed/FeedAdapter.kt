package com.example.backintime.ui.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.R
import com.example.backintime.databinding.ItemMemoryBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class FeedAdapter(
    private val capsules: MutableList<TimeCapsule>,
    private val onItemClick: (TimeCapsule) -> Unit
) : RecyclerView.Adapter<FeedAdapter.MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val binding = ItemMemoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoryViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val capsule = capsules[position]
        holder.bind(capsules[position])
        holder.itemView.setOnClickListener { onItemClick(capsule) }
    }

    override fun getItemCount(): Int = capsules.size

    class MemoryViewHolder(private val binding: ItemMemoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(capsule: TimeCapsule) {
            if (capsule.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(capsule.imageUrl)
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(binding.memoryImage)
            } else {
                binding.memoryImage.setImageResource(R.drawable.logo_back_in_time)
            }
            binding.memoryTitle.text = capsule.title

            // המרת openDate למחרוזת בפורמט dd/MM/yy
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            binding.memoryDate.text = dateFormat.format(capsule.openDate)

            // הצבת האימייל של הכותב
            binding.memoryEmail.text = capsule.creatorName

            // הצבת התוכן של הזיכרון
            binding.memoryContent.text = capsule.content
        }
    }
}
