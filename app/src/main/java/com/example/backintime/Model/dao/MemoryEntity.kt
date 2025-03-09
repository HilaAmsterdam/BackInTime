package com.example.backintime.model.dao

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val openDate: Long,
    val imageUrl: String,
    val creatorId: String,    // Firebase UID of the creator
    val creatorEmail: String  // Creator’s email
) : Parcelable