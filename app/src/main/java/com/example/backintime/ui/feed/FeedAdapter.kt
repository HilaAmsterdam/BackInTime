package com.example.backintime.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
            if (header.date == "Opened") {
                headerTitle.text = "OPENED MEMORIES"
                headerTitle.textSize = 32f
                headerTitle.setTextColor(android.graphics.Color.parseColor("#5FB3F9"))
                headerTitle.textAlignment = View.TEXT_ALIGNMENT_CENTER
                val params = headerTitle.layoutParams as ConstraintLayout.LayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                headerTitle.layoutParams = params
            } else {
                headerTitle.text = "Open Date is: ${header.date}"
                headerTitle.textSize = 18f
                headerTitle.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                val params = headerTitle.layoutParams as ConstraintLayout.LayoutParams
                params.width = 0
                headerTitle.layoutParams = params
            }
        }
    }

    class PostViewHolder(private val binding: ItemMemoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(capsule: TimeCapsule) {
            if (capsule.imageUrl.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                Picasso.get()
                    .load(capsule.imageUrl)
                    .into(binding.memoryImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            binding.progressBar.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {
                            binding.progressBar.visibility = View.GONE
                        }
                    })
            } else {
                binding.memoryImage.setImageResource(R.drawable.logo_back_in_time)
                binding.progressBar.visibility = View.GONE
            }

            binding.memoryTitle.text = capsule.title
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            binding.memoryDate.text = dateFormat.format(capsule.openDate)
            binding.memoryEmail.text = capsule.creatorName
            binding.memoryContent.text = capsule.content
        }
    }

}
