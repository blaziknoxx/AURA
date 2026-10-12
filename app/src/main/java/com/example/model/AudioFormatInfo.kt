package com.example.model

/**
 * Detailed technical audio format specifications extracted from actual file headers
 * and decoder metadata.
 */
data class AudioFormatInfo(
    val codec: String,             // e.g. "FLAC", "WAV", "ALAC", "MP3", "AAC", "OGG"
    val container: String,         // e.g. "flac", "wav", "m4a", "mp3", "ogg"
    val bitDepth: Int? = null,     // e.g. 16, 24, 32 (only for lossless/PCM)
    val sampleRate: Int? = null,   // e.g. 44100, 48000, 96000, 192000
    val bitrateKbps: Int? = null,  // e.g. 320, 1411, 4608
    val channels: Int = 2,         // 1 = Mono, 2 = Stereo, 6 = 5.1
    val quality: AudioQuality = AudioQuality.UNKNOWN,
    val isVerifiedAuthentic: Boolean = true,
    val mimeType: String = ""
) {
    /** Formatted sample rate string, e.g. "44.1 kHz", "96.0 kHz", "192.0 kHz" */
    val formattedSampleRate: String?
        get() = sampleRate?.let { sr ->
            if (sr % 1000 == 0) {
                "${sr / 1000} kHz"
            } else {
                String.format("%.1f kHz", sr / 1000.0)
            }
        }

    /** Formatted bit depth string, e.g. "16-bit", "24-bit" */
    val formattedBitDepth: String?
        get() = bitDepth?.let { "$it-bit" }

    /** Formatted combined spec string, e.g. "24-bit · 96.0 kHz" or "320 kbps" */
    val formattedSpecBadge: String
        get() = when {
            bitDepth != null && formattedSampleRate != null -> "$formattedBitDepth · $formattedSampleRate"
            formattedSampleRate != null -> formattedSampleRate!!
            bitrateKbps != null -> "$bitrateKbps kbps"
            else -> codec
        }

    /** Formatted channel string, e.g. "Stereo", "Mono", "5.1 Surround" */
    val formattedChannels: String
        get() = when (channels) {
            1 -> "Mono"
            2 -> "Stereo"
            6 -> "5.1 Surround"
            else -> "$channels Channels"
        }

    /** Complete technical specification summary */
    val fullTechnicalSummary: String
        get() = buildString {
            append(codec)
            if (bitDepth != null) append(" · $formattedBitDepth")
            if (formattedSampleRate != null) append(" · $formattedSampleRate")
            if (bitrateKbps != null) append(" · $bitrateKbps kbps")
            append(" · $formattedChannels")
        }

    companion object {
        fun defaultUnknown(codec: String = "AUDIO"): AudioFormatInfo {
            return AudioFormatInfo(
                codec = codec,
                container = codec.lowercase(),
                quality = AudioQuality.UNKNOWN,
                isVerifiedAuthentic = false
            )
        }
    }
}
