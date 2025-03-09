package com.example.backintime.ui.memory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.backintime.R
import com.example.backintime.model.dao.MemoryEntity

class MemoryAdapter(
    private var memories: List<MemoryEntity>,
    private val onItemClick: (MemoryEntity) -> Unit
) : RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder>() {

    class MemoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.selectedMemoryImage)
        val title: TextView = view.findViewById(R.id.selectedMemoryTitle)
        val date: TextView = view.findViewById(R.id.selectedMemoryDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = memories[position]

        holder.title.text = memory.title
        holder.date.text = memory.openDate.toString()

        Glide.with(holder.itemView.context)
            .load(memory.imageUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemClick(memory)
        }
    }

    override fun getItemCount(): Int = memories.size

    fun updateList(newMemories: List<MemoryEntity>) {
        memories = newMemories
        notifyDataSetChanged()
    }
}
