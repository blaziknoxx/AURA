package com.example.playback

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class DecodedAudioTelemetry(
    val decoderName: String = "",
    val decodedSampleRate: Int = 0,
    val decodedChannelCount: Int = 0,
    val decodedBitDepth: Int = 16,
    val decodedBitrateKbps: Int = 0,
    val isHardwareAccelerated: Boolean = true
) {
    val summaryText: String
        get() = buildString {
            if (decoderName.isNotBlank()) append("$decoderName · ")
            if (decodedSampleRate > 0) append("${decodedSampleRate / 1000.0} kHz · ")
            if (decodedBitDepth > 0) append("$decodedBitDepth-bit · ")
            append(if (decodedChannelCount == 1) "Mono" else "$decodedChannelCount Channels")
        }
}
