package com.example.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.model.AudioFormatInfo
import com.example.model.AudioQuality
import com.example.model.Track
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

object MetadataExtractor {
    private const val TAG = "MetadataExtractor"

    data class ExtractedMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val formatInfo: AudioFormatInfo,
        val albumArtUri: String? = null
    )

    fun extract(context: Context, uri: Uri, fileNameHint: String? = null, fileSize: Long = 0L): ExtractedMetadata {
        val retriever = MediaMetadataRetriever()
        var embeddedArtUri: String? = null

        var title = fileNameHint?.substringBeforeLast('.') ?: "Unknown Track"
        var artist = "Unknown Artist"
        var album = "Unknown Album"
        var durationMs = 0L

        var mimeType = ""
        var sampleRate: Int? = null
        var bitDepth: Int? = null
        var bitrateKbps: Int? = null
        var channels = 2

        val lowerName = fileNameHint?.lowercase() ?: uri.toString().lowercase()
        val extension = lowerName.substringAfterLast('.', "")

        try {
            retriever.setDataSource(context, uri)

            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }?.let {
                title = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }?.let {
                artist = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }?.let {
                album = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
                durationMs = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.let {
                mimeType = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()?.let {
                bitrateKbps = it / 1000
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()?.let {
                    sampleRate = it
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)?.toIntOrNull()?.let {
                    bitDepth = it
                }
            }

            // Extract embedded artwork
            val pictureBytes = retriever.embeddedPicture
            if (pictureBytes != null && pictureBytes.isNotEmpty()) {
                embeddedArtUri = saveArtToCache(context, pictureBytes, "$artist-$album")
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaMetadataRetriever could not inspect $uri: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        // Direct FLAC header inspection for exact bit depth & sample rate
        val isFlac = extension == "flac" || mimeType.contains("flac")
        val isWav = extension == "wav" || mimeType.contains("wav")

        if (isFlac) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val streamInfo = FlacHeaderParser.parseStreamInfo(stream)
                    if (streamInfo != null) {
                        bitDepth = streamInfo.bitsPerSample
                        sampleRate = streamInfo.sampleRate
                        channels = streamInfo.channels
                        if (streamInfo.sampleRate > 0 && streamInfo.totalSamples > 0 && durationMs == 0L) {
                            durationMs = (streamInfo.totalSamples * 1000L) / streamInfo.sampleRate
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading FLAC stream: ${e.message}")
            }
        } else if (isWav) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val wavInfo = WavHeaderParser.parse(stream)
                    if (wavInfo != null) {
                        bitDepth = wavInfo.bitsPerSample
                        sampleRate = wavInfo.sampleRate
                        channels = wavInfo.channels
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading WAV stream: ${e.message}")
            }
        }

        // Determine codec and container
        val codec = when {
            isFlac -> "FLAC"
            isWav -> "WAV"
            extension == "alac" || (mimeType.contains("alac")) -> "ALAC"
            extension == "mp3" || mimeType.contains("mp3") || mimeType.contains("mpeg") -> "MP3"
            extension == "m4a" || extension == "aac" || mimeType.contains("aac") -> "AAC"
            extension == "ogg" || mimeType.contains("ogg") -> "OGG"
            extension == "opus" || mimeType.contains("opus") -> "OPUS"
            else -> extension.uppercase().ifBlank { "AUDIO" }
        }

        val container = extension.ifBlank { codec.lowercase() }

        // Compute bitrate if not found but bitDepth and sampleRate are known (uncompressed/lossless approximation)
        if (bitrateKbps == null && bitDepth != null && sampleRate != null && channels > 0) {
            val rawBitrate = (sampleRate!!.toLong() * bitDepth!! * channels) / 1000
            if (isFlac) {
                // FLAC typical compression is ~50-60% of raw PCM
                bitrateKbps = (rawBitrate * 0.55).toInt()
            } else if (isWav) {
                bitrateKbps = rawBitrate.toInt()
            }
        }

        // Determine AudioQuality classification
        val isLossless = isFlac || isWav || codec == "ALAC" || mimeType.contains("flac") || mimeType.contains("wav")

        val quality = when {
            isLossless -> {
                val depth = bitDepth ?: 16
                val rate = sampleRate ?: 44100
                if (depth > 16 || rate > 44100) {
                    AudioQuality.HI_RES
                } else if (depth == 16 && rate == 44100) {
                    AudioQuality.CD_QUALITY
                } else {
                    AudioQuality.LOSSLESS
                }
            }
            codec == "MP3" || codec == "AAC" || codec == "OGG" || codec == "OPUS" -> {
                AudioQuality.LOSSY
            }
            else -> AudioQuality.UNKNOWN
        }

        val formatInfo = AudioFormatInfo(
            codec = codec,
            container = container,
            bitDepth = if (isLossless) bitDepth else null, // Never invent bit depth for lossy
            sampleRate = sampleRate,
            bitrateKbps = bitrateKbps,
            channels = channels,
            quality = quality,
            isVerifiedAuthentic = true,
            mimeType = mimeType
        )

        return ExtractedMetadata(
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            formatInfo = formatInfo,
            albumArtUri = embeddedArtUri
        )
    }

    private fun saveArtToCache(context: Context, bytes: ByteArray, key: String): String? {
        return try {
            val hash = MessageDigest.getInstance("MD5").digest(key.toByteArray())
                .joinToString("") { "%02x".format(it) }
            val dir = File(context.filesDir, "album_art")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "art_$hash.jpg")
            FileOutputStream(file).use { it.write(bytes) }
            Uri.fromFile(file).toString()
        } catch (_: Exception) {
            null
        }
    }
}
