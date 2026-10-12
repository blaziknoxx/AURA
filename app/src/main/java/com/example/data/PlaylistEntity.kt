package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toPlaylist(trackCount: Int): Playlist {
        return Playlist(
            id = id,
            name = name,
            description = description,
            trackCount = trackCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
