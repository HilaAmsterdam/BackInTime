package com.example.backintime.Model.Dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_capsules")
data class TimeCapsuleEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val firebaseId: String = "",
    val title: String = "",
    val content: String = "",
    val openDate: Long = 0,
    val imageUrl: String = "",
    val creatorName: String = "",
    val creatorId: String = ""
)
