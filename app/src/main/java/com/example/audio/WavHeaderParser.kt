package com.example.audio

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Pure Kotlin parser for RIFF WAV audio headers.
 * Extracts accurate PCM bit depth, sample rate, and channels from the 'fmt ' chunk.
 */
object WavHeaderParser {

    data class WavInfo(
        val audioFormat: Int, // 1 = PCM, 3 = IEEE Float, 65534 = Extensible
        val channels: Int,
        val sampleRate: Int,
        val byteRate: Int,
        val blockAlign: Int,
        val bitsPerSample: Int
    )

    fun parse(inputStream: InputStream): WavInfo? {
        try {
            val buffer = ByteArray(12)
            if (inputStream.read(buffer) != 12) return null

            // Must start with "RIFF" and contain "WAVE"
            if (buffer[0] != 'R'.code.toByte() || buffer[1] != 'I'.code.toByte() ||
                buffer[2] != 'F'.code.toByte() || buffer[3] != 'F'.code.toByte() ||
                buffer[8] != 'W'.code.toByte() || buffer[9] != 'A'.code.toByte() ||
                buffer[10] != 'V'.code.toByte() || buffer[11] != 'E'.code.toByte()
            ) {
                return null
            }

            // Read chunks until "fmt "
            val chunkHeader = ByteArray(8)
            while (inputStream.read(chunkHeader) == 8) {
                val chunkId = String(chunkHeader, 0, 4)
                val chunkSize = ByteBuffer.wrap(chunkHeader, 4, 4).order(ByteOrder.LITTLE_ENDIAN).int

                if (chunkId == "fmt ") {
                    val fmtData = ByteArray(chunkSize.coerceAtLeast(16))
                    var read = 0
                    while (read < fmtData.size) {
                        val n = inputStream.read(fmtData, read, fmtData.size - read)
                        if (n < 0) break
                        read += n
                    }
                    if (read < 16) return null

                    val bb = ByteBuffer.wrap(fmtData).order(ByteOrder.LITTLE_ENDIAN)
                    val formatTag = bb.short.toInt() and 0xFFFF
                    val channels = bb.short.toInt() and 0xFFFF
                    val sampleRate = bb.int
                    val byteRate = bb.int
                    val blockAlign = bb.short.toInt() and 0xFFFF
                    val bitsPerSample = bb.short.toInt() and 0xFFFF

                    return WavInfo(
                        audioFormat = formatTag,
                        channels = channels,
                        sampleRate = sampleRate,
                        byteRate = byteRate,
                        blockAlign = blockAlign,
                        bitsPerSample = bitsPerSample
                    )
                } else {
                    var skipped = 0L
                    val toSkip = chunkSize.toLong().coerceAtLeast(0L)
                    while (skipped < toSkip) {
                        val s = inputStream.skip(toSkip - skipped)
                        if (s <= 0) break
                        skipped += s
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return null
    }
}
