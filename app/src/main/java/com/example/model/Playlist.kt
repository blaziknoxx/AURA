package com.example.model

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
