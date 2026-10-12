package com.example.model

/**
 * Standardized audio quality classification.
 * Strictly separates authentic source file specifications from verified decoded output.
 */
enum class AudioQuality(val displayName: String, val shortLabel: String) {
    /** Lossless audio exceeding CD audio specifications (>= 24-bit or >= 48 kHz lossless) */
    HI_RES("Hi-Res Audio", "HI-RES"),

    /** Exact Red Book CD Quality (16-bit / 44.1 kHz lossless) */
    CD_QUALITY("CD Quality", "CD QUALITY"),

    /** Lossless audio that does not meet Hi-Res or CD Quality standards */
    LOSSLESS("Lossless", "LOSSLESS"),

    /** Compressed lossy formats (MP3, AAC, OGG, etc.) */
    LOSSY("Lossy", "LOSSY"),

    /** Indeterminate or missing quality information */
    UNKNOWN("Unknown Quality", "UNKNOWN");

    val isHiRes: Boolean get() = this == HI_RES
    val isCdQuality: Boolean get() = this == CD_QUALITY
    val isLosslessAudio: Boolean get() = this == HI_RES || this == CD_QUALITY || this == LOSSLESS
}
