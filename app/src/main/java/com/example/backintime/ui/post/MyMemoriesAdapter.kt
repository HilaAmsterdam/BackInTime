package com.example.backintime.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.backintime.R
import com.example.backintime.ui.post.MemoryItem

class MyMemoriesAdapter(private val items: List<MemoryItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_MEMORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MemoryItem.Header -> VIEW_TYPE_HEADER
            is MemoryItem.Memory -> VIEW_TYPE_MEMORY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_memory_group, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_memory, parent, false)
            MemoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is MemoryItem.Header -> (holder as HeaderViewHolder).bind(item)
            is MemoryItem.Memory -> (holder as MemoryViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.memoryGroupTitle)

        fun bind(item: MemoryItem.Header) {
            titleTextView.text = item.title
        }
    }

    class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.memoryImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.memoryTitle)
        private val dateTextView: TextView = itemView.findViewById(R.id.memoryDate)

        fun bind(item: MemoryItem.Memory) {
            imageView.setImageResource(item.imageResId)
            titleTextView.text = item.title
            dateTextView.text = item.date
        }
    }
}
