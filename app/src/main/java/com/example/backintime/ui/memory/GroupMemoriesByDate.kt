package com.example.backintime.ui.memory

import com.example.backintime.model.dao.MemoryEntity
import java.text.SimpleDateFormat
import java.util.*

fun groupMemoriesByDate(memories: List<MemoryEntity>): List<MemoryListItem> {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val grouped = memories.groupBy { dateFormat.format(Date(it.openDate)) }
    val result = mutableListOf<MemoryListItem>()
    grouped.toSortedMap(compareByDescending { it }).forEach { (date, mems) ->
        result.add(MemoryListItem.Header(date))
        mems.forEach { memory ->
            result.add(MemoryListItem.MemoryItem(memory))
        }
    }
    return result
}
