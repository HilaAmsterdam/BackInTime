package com.example.backintime.ui.memory

sealed class MemoryItem {
    data class Header(val title: String) : MemoryItem()
    data class Memory(
        val title: String,
        val date: String,
        val imageResId: Int
    ) : MemoryItem()
}

