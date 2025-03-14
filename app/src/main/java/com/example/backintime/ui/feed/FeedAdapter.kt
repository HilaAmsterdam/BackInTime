package com.example.backintime.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.backintime.Model.FeedItem
import com.example.backintime.Model.TimeCapsule
import com.example.backintime.R
import com.example.backintime.databinding.ItemMemoryBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class FeedAdapter(
    private val items: List<FeedItem>,
    private val onItemClick: (TimeCapsule) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_POST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FeedItem.Header -> TYPE_HEADER
            is FeedItem.Post -> TYPE_POST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemMemoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PostViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FeedItem.Header -> (holder as HeaderViewHolder).bind(item)
            is FeedItem.Post -> {
                (holder as PostViewHolder).bind(item.capsule)
                holder.itemView.setOnClickListener { onItemClick(item.capsule) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.headerTitle)
        fun bind(header: FeedItem.Header) {
            headerTitle.text = "Open Date is: ${header.date}"
        }
    }

    class PostViewHolder(private val binding: ItemMemoryBinding) : RecyclerView.ViewHolder(binding.root) {
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
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            binding.memoryDate.text = dateFormat.format(capsule.openDate)
            binding.memoryEmail.text = capsule.creatorName
            binding.memoryContent.text = capsule.content
        }
    }
}
