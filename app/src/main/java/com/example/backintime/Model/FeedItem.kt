package com.example.backintime.Model


sealed class FeedItem {
    data class Header(val date: String) : FeedItem()
    data class Post(val capsule: TimeCapsule) : FeedItem()
}
