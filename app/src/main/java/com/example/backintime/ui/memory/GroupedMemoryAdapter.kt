package com.example.backintime.ui.memory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.backintime.R

class GroupedMemoryAdapter(
    private var items: List<MemoryListItem>,
    private val onItemClick: (MemoryListItem.MemoryItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_MEMORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MemoryListItem.Header -> VIEW_TYPE_HEADER
            is MemoryListItem.MemoryItem -> VIEW_TYPE_MEMORY
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
            is MemoryListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is MemoryListItem.MemoryItem -> (holder as MemoryViewHolder).bind(item, onItemClick)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<MemoryListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTextView: TextView = itemView.findViewById(R.id.memoryGroupTitle)
        fun bind(header: MemoryListItem.Header) {
            headerTextView.text = header.date
        }
    }

    class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.selectedMemoryImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.selectedMemoryTitle)
        private val dateTextView: TextView = itemView.findViewById(R.id.selectedMemoryDate)
        fun bind(item: MemoryListItem.MemoryItem, onItemClick: (MemoryListItem.MemoryItem) -> Unit) {
            val memory = item.memory
            titleTextView.text = memory.title
            dateTextView.text = memory.openDate.toString() // מומלץ לעצב את התאריך בצורה קריאה
            Glide.with(itemView.context)
                .load(memory.imageUrl)
                .into(imageView)
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
