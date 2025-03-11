package com.example.backintime.ui.memory

import com.example.backintime.model.dao.MemoryEntity


sealed class MemoryListItem {
    data class Header(val date: String) : MemoryListItem()
    data class MemoryItem(val memory: MemoryEntity) : MemoryListItem()
}
