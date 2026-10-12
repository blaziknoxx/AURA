package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AudioFormatInfo
import com.example.model.AudioQuality
import com.example.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val folderPath: String,
    // Audio Format Specifications
    val codec: String,
    val container: String,
    val bitDepth: Int?,
    val sampleRate: Int?,
    val bitrateKbps: Int?,
    val channels: Int,
    val quality: String, // from AudioQuality.name
    val isVerifiedAuthentic: Boolean,
    val albumArtUri: String?,
    val isFavorite: Boolean,
    val playCount: Int,
    val lastPlayedTimestamp: Long,
    val addedTimestamp: Long
) {
    fun toTrack(): Track {
        val audioQuality = try {
            AudioQuality.valueOf(quality)
        } catch (_: Exception) {
            AudioQuality.UNKNOWN
        }

        return Track(
            id = id,
            uri = uri,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            sizeBytes = sizeBytes,
            folderPath = folderPath,
            formatInfo = AudioFormatInfo(
                codec = codec,
                container = container,
                bitDepth = bitDepth,
                sampleRate = sampleRate,
                bitrateKbps = bitrateKbps,
                channels = channels,
                quality = audioQuality,
                isVerifiedAuthentic = isVerifiedAuthentic
            ),
            albumArtUri = albumArtUri,
            isFavorite = isFavorite,
            playCount = playCount,
            lastPlayedTimestamp = lastPlayedTimestamp,
            addedTimestamp = addedTimestamp
        )
    }

    companion object {
        fun fromTrack(track: Track): TrackEntity {
            return TrackEntity(
                id = track.id,
                uri = track.uri,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                sizeBytes = track.sizeBytes,
                folderPath = track.folderPath,
                codec = track.formatInfo.codec,
                container = track.formatInfo.container,
                bitDepth = track.formatInfo.bitDepth,
                sampleRate = track.formatInfo.sampleRate,
                bitrateKbps = track.formatInfo.bitrateKbps,
                channels = track.formatInfo.channels,
                quality = track.formatInfo.quality.name,
                isVerifiedAuthentic = track.formatInfo.isVerifiedAuthentic,
                albumArtUri = track.albumArtUri,
                isFavorite = track.isFavorite,
                playCount = track.playCount,
                lastPlayedTimestamp = track.lastPlayedTimestamp,
                addedTimestamp = track.addedTimestamp
            )
        }
    }
}
