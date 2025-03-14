package com.example.backintime.Model

import com.example.backintime.Model.TimeCapsule

sealed class FeedItem {
    data class Header(val date: String) : FeedItem()
    data class Post(val capsule: TimeCapsule) : FeedItem()
}
