package com.example.audio

import java.io.InputStream

/**
 * Pure Kotlin parser for FLAC metadata headers.
 * Extracts accurate nominal stream parameters directly from the FLAC STREAMINFO block.
 *
 * Spec reference: Xiph.Org FLAC format specification.
 */
object FlacHeaderParser {

    data class FlacStreamInfo(
        val minBlockSize: Int,
        val maxBlockSize: Int,
        val minFrameSize: Int,
        val maxFrameSize: Int,
        val sampleRate: Int,
        val channels: Int,
        val bitsPerSample: Int,
        val totalSamples: Long
    )

    fun parseStreamInfo(inputStream: InputStream): FlacStreamInfo? {
        try {
            val header = ByteArray(4)
            if (inputStream.read(header) != 4) return null
            // Check "fLaC" ASCII marker: 0x66, 0x4C, 0x61, 0x43
            if (header[0] != 0x66.toByte() || header[1] != 0x4C.toByte() ||
                header[2] != 0x61.toByte() || header[3] != 0x43.toByte()
            ) {
                return null
            }

            // Loop metadata blocks until STREAMINFO (type 0)
            while (true) {
                val blockHeader = ByteArray(4)
                if (inputStream.read(blockHeader) != 4) return null
                val firstByte = blockHeader[0].toInt() and 0xFF
                val isLast = (firstByte and 0x80) != 0
                val blockType = firstByte and 0x7F
                val length = ((blockHeader[1].toInt() and 0xFF) shl 16) or
                        ((blockHeader[2].toInt() and 0xFF) shl 8) or
                        (blockHeader[3].toInt() and 0xFF)

                if (blockType == 0) {
                    // Block 0 is STREAMINFO (must be at least 34 bytes)
                    val data = ByteArray(length)
                    var read = 0
                    while (read < length) {
                        val n = inputStream.read(data, read, length - read)
                        if (n < 0) break
                        read += n
                    }
                    if (read < 34) return null

                    val minBlockSize = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
                    val maxBlockSize = ((data[2].toInt() and 0xFF) shl 8) or (data[3].toInt() and 0xFF)
                    val minFrameSize = ((data[4].toInt() and 0xFF) shl 16) or
                            ((data[5].toInt() and 0xFF) shl 8) or (data[6].toInt() and 0xFF)
                    val maxFrameSize = ((data[7].toInt() and 0xFF) shl 16) or
                            ((data[8].toInt() and 0xFF) shl 8) or (data[9].toInt() and 0xFF)

                    // Bytes 10 to 17 contain:
                    // Sample rate: 20 bits (bytes 10, 11, top 4 bits of 12)
                    // Channels: 3 bits (bits 3..1 of byte 12) -> value + 1
                    // Bits per sample: 5 bits (bit 0 of byte 12, top 4 bits of byte 13) -> value + 1
                    // Total samples: 36 bits (bottom 4 bits of byte 13, bytes 14..17)
                    val b10 = data[10].toLong() and 0xFF
                    val b11 = data[11].toLong() and 0xFF
                    val b12 = data[12].toLong() and 0xFF
                    val b13 = data[13].toLong() and 0xFF
                    val b14 = data[14].toLong() and 0xFF
                    val b15 = data[15].toLong() and 0xFF
                    val b16 = data[16].toLong() and 0xFF
                    val b17 = data[17].toLong() and 0xFF

                    val sampleRate = ((b10 shl 12) or (b11 shl 4) or (b12 ushr 4)).toInt()
                    val channels = (((b12 ushr 1) and 0x07) + 1).toInt()
                    val bitsPerSample = ((((b12 and 0x01) shl 4) or (b13 ushr 4)) + 1).toInt()
                    val totalSamples = ((b13 and 0x0F) shl 32) or
                            (b14 shl 24) or (b15 shl 16) or (b16 shl 8) or b17

                    return FlacStreamInfo(
                        minBlockSize = minBlockSize,
                        maxBlockSize = maxBlockSize,
                        minFrameSize = minFrameSize,
                        maxFrameSize = maxFrameSize,
                        sampleRate = sampleRate,
                        channels = channels,
                        bitsPerSample = bitsPerSample,
                        totalSamples = totalSamples
                    )
                } else {
                    // Skip this block
                    var skipped = 0L
                    while (skipped < length) {
                        val s = inputStream.skip(length - skipped)
                        if (s <= 0) break
                        skipped += s
                    }
                    if (isLast) break
                }
            }
        } catch (_: Exception) {
            // Stream read exception, return null
        }
        return null
    }
}
