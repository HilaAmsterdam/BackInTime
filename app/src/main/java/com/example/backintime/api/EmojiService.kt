package com.example.backintime.api

import retrofit2.http.GET

data class Emoji(
    val name: String,
    val category: String,
    val group: String,
    val htmlCode: List<String>,
    val unicode: List<String>
)

interface EmojiService {
    @GET("all")
    suspend fun getAllEmojis(): List<Emoji>
}
