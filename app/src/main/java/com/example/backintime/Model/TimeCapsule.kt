package com.example.backintime.Model

data class TimeCapsule(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var openDate: Long = 0,
    var imageUrl: String = "",
    var creatorName: String = "",
    var creatorId: String = ""
)
