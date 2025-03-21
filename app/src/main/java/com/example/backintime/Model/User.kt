package com.example.backintime.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var uid: String = "",
    var email: String = "",
    var profileImageUrl: String = ""
)
