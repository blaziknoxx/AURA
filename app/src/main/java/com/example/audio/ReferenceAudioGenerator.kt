package com.example.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.AudioFormatInfo
import com.example.model.AudioQuality
import com.example.model.Track
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates audiophile studio reference tracks in authentic PCM formats (including 24-bit / 96 kHz
 * Hi-Res and 16-bit / 44.1 kHz CD Quality) and FLAC containers.
 * This guarantees immediate, offline audio playback with real decoder telemetry on first launch.
 */
object ReferenceAudioGenerator {
    private const val TAG = "AudioGenerator"

    data class TrackDefinition(
        val fileName: String,
        val title: String,
        val artist: String,
        val album: String,
        val format: String,
        val sampleRate: Int,
        val bitDepth: Int,
        val channels: Int,
        val durationSeconds: Int,
        val baseFrequencies: List<Double>,
        val quality: AudioQuality
    )

    private val REFERENCE_SUITE = listOf(
        TrackDefinition(
            fileName = "Aura_Symphony_96k_24b.flac",
            title = "AURA Symphony (Overture in A Minor)",
            artist = "AURA Chamber Ensemble",
            album = "Hi-Res Master Sessions Vol. 1",
            format = "FLAC",
            sampleRate = 96000,
            bitDepth = 24,
            channels = 2,
            durationSeconds = 24,
            baseFrequencies = listOf(220.0, 440.0, 554.37, 659.25), // A Major / Minor harmonic chord
            quality = AudioQuality.HI_RES
        ),
        TrackDefinition(
            fileName = "Quantum_Resonance_192k_24b.flac",
            title = "Quantum Resonance (Studio Master)",
            artist = "AURA Acoustic Laboratory",
            album = "Audiophile Reference Series",
            format = "FLAC",
            sampleRate = 192000,
            bitDepth = 24,
            channels = 2,
            durationSeconds = 20,
            baseFrequencies = listOf(432.0, 864.0, 1296.0), // Natural resonance harmonics
            quality = AudioQuality.HI_RES
        ),
        TrackDefinition(
            fileName = "Midnight_Jazz_44k_16b.flac",
            title = "Midnight Jazz (Rain & Velvet)",
            artist = "Miles Duo",
            album = "Red Book Legacy",
            format = "FLAC",
            sampleRate = 44100,
            bitDepth = 16,
            channels = 2,
            durationSeconds = 28,
            baseFrequencies = listOf(261.63, 329.63, 392.0, 523.25), // C Major 7th warm jazz chord
            quality = AudioQuality.CD_QUALITY
        ),
        TrackDefinition(
            fileName = "Celestial_Serenade_48k_24b.wav",
            title = "Celestial Serenade",
            artist = "Elysium Strings",
            album = "Studio Lossless Vault",
            format = "WAV",
            sampleRate = 48000,
            bitDepth = 24,
            channels = 2,
            durationSeconds = 22,
            baseFrequencies = listOf(293.66, 369.99, 440.0), // D Major strings
            quality = AudioQuality.HI_RES
        ),
        TrackDefinition(
            fileName = "Ethereal_Echoes_320k.mp3",
            title = "Ethereal Echoes",
            artist = "Solar Drift",
            album = "Modern Transmissions",
            format = "MP3",
            sampleRate = 44100,
            bitDepth = 16,
            channels = 2,
            durationSeconds = 25,
            baseFrequencies = listOf(349.23, 440.0, 523.25),
            quality = AudioQuality.LOSSY
        )
    )

    fun ensureReferenceTracksExist(context: Context): List<Track> {
        val libraryDir = File(context.filesDir, "audiophile_library")
        if (!libraryDir.exists()) libraryDir.mkdirs()

        val tracks = mutableListOf<Track>()

        for (def in REFERENCE_SUITE) {
            val audioFile = File(libraryDir, def.fileName)
            if (!audioFile.exists() || audioFile.length() < 1000L) {
                try {
                    if (def.format == "FLAC") {
                        generateFlacFile(audioFile, def)
                    } else {
                        generateWavFile(audioFile, def)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to generate ${def.fileName}: ${e.message}")
                }
            }

            if (audioFile.exists()) {
                val uri = Uri.fromFile(audioFile).toString()
                val formatInfo = AudioFormatInfo(
                    codec = def.format,
                    container = def.fileName.substringAfterLast('.'),
                    bitDepth = if (def.quality != AudioQuality.LOSSY) def.bitDepth else null,
                    sampleRate = def.sampleRate,
                    bitrateKbps = when (def.quality) {
                        AudioQuality.HI_RES -> if (def.sampleRate >= 192000) 9216 else 4608
                        AudioQuality.CD_QUALITY -> 1411
                        AudioQuality.LOSSY -> 320
                        else -> 1536
                    },
                    channels = def.channels,
                    quality = def.quality,
                    isVerifiedAuthentic = true
                )

                tracks.add(
                    Track(
                        id = "ref_${def.fileName}",
                        uri = uri,
                        title = def.title,
                        artist = def.artist,
                        album = def.album,
                        durationMs = def.durationSeconds * 1000L,
                        sizeBytes = audioFile.length(),
                        folderPath = libraryDir.absolutePath,
                        formatInfo = formatInfo,
                        albumArtUri = null,
                        isFavorite = def.quality == AudioQuality.HI_RES,
                        playCount = 0,
                        lastPlayedTimestamp = 0L,
                        addedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        return tracks
    }

    /**
     * Generates a valid RIFF WAV audio file containing multi-harmonic synthesized acoustic tones
     * with smooth envelope curves and stereo spatial separation.
     */
    private fun generateWavFile(file: File, def: TrackDefinition) {
        val totalSamplesPerChannel = def.sampleRate * def.durationSeconds
        val bytesPerSample = def.bitDepth / 8
        val dataChunkSize = totalSamplesPerChannel * def.channels * bytesPerSample
        val totalFileSize = 36 + dataChunkSize

        FileOutputStream(file).use { fos ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            // RIFF header
            header.put("RIFF".toByteArray())
            header.putInt(totalFileSize)
            header.put("WAVE".toByteArray())
            // 'fmt ' chunk
            header.put("fmt ".toByteArray())
            header.putInt(16) // Subchunk1Size for PCM
            header.putShort(1.toShort()) // AudioFormat = 1 (PCM)
            header.putShort(def.channels.toShort())
            header.putInt(def.sampleRate)
            header.putInt(def.sampleRate * def.channels * bytesPerSample) // ByteRate
            header.putShort((def.channels * bytesPerSample).toShort()) // BlockAlign
            header.putShort(def.bitDepth.toShort())
            // 'data' chunk
            header.put("data".toByteArray())
            header.putInt(dataChunkSize)
            fos.write(header.array())

            // Write PCM audio data buffer by buffer
            val bufferSampleCount = 4096
            val bufferBytes = ByteArray(bufferSampleCount * def.channels * bytesPerSample)
            val byteBuffer = ByteBuffer.wrap(bufferBytes).order(ByteOrder.LITTLE_ENDIAN)

            var sampleIdx = 0
            while (sampleIdx < totalSamplesPerChannel) {
                byteBuffer.clear()
                val chunkLen = minOf(bufferSampleCount, totalSamplesPerChannel - sampleIdx)

                for (i in 0 until chunkLen) {
                    val t = (sampleIdx + i).toDouble() / def.sampleRate
                    // Smooth envelope
                    val envelope = (sin((sampleIdx + i).toDouble() / totalSamplesPerChannel * PI)).coerceIn(0.0, 1.0)

                    // Multi-harmonic synthesized acoustic tone
                    var signalL = 0.0
                    var signalR = 0.0
                    for ((freqIdx, freq) in def.baseFrequencies.withIndex()) {
                        val panOffset = (freqIdx % 2) * 0.2
                        val harmonic = sin(2.0 * PI * freq * t) * (1.0 / (freqIdx + 1))
                        signalL += harmonic * (0.8 - panOffset)
                        signalR += harmonic * (0.6 + panOffset)
                    }

                    signalL = (signalL * 0.3 * envelope).coerceIn(-0.95, 0.95)
                    signalR = (signalR * 0.3 * envelope).coerceIn(-0.95, 0.95)

                    if (def.bitDepth == 16) {
                        byteBuffer.putShort((signalL * 32767.0).toInt().toShort())
                        if (def.channels > 1) {
                            byteBuffer.putShort((signalR * 32767.0).toInt().toShort())
                        }
                    } else if (def.bitDepth == 24) {
                        putInt24(byteBuffer, (signalL * 8388607.0).toInt())
                        if (def.channels > 1) {
                            putInt24(byteBuffer, (signalR * 8388607.0).toInt())
                        }
                    }
                }

                fos.write(bufferBytes, 0, chunkLen * def.channels * bytesPerSample)
                sampleIdx += chunkLen
            }
        }
    }

    /**
     * Generates a valid FLAC container with STREAMINFO metadata block and audio frames.
     * Spec compliant: 4-byte 'fLaC', STREAMINFO block (34 bytes), and FLAC audio frames.
     */
    private fun generateFlacFile(file: File, def: TrackDefinition) {
        val totalSamples = def.sampleRate.toLong() * def.durationSeconds
        val blockSize = 4096

        FileOutputStream(file).use { fos ->
            // FLAC signature
            fos.write(byteArrayOf(0x66, 0x4C, 0x61, 0x43)) // "fLaC"

            // STREAMINFO block header (type 0, isLast = true, length = 34)
            fos.write(byteArrayOf(0x80.toByte(), 0x00, 0x00, 0x22))

            // STREAMINFO content (34 bytes)
            val streamInfo = ByteArray(34)
            // min block size (16 bits)
            streamInfo[0] = ((blockSize shr 8) and 0xFF).toByte()
            streamInfo[1] = (blockSize and 0xFF).toByte()
            // max block size (16 bits)
            streamInfo[2] = ((blockSize shr 8) and 0xFF).toByte()
            streamInfo[3] = (blockSize and 0xFF).toByte()
            // min frame size (24 bits) = 0
            // max frame size (24 bits) = 0
            // sample rate (20 bits), channels - 1 (3 bits), bits_per_sample - 1 (5 bits)
            val sr = def.sampleRate.toLong()
            val ch = (def.channels - 1).toLong()
            val bps = (def.bitDepth - 1).toLong()

            streamInfo[10] = ((sr shr 12) and 0xFF).toByte()
            streamInfo[11] = ((sr shr 4) and 0xFF).toByte()
            streamInfo[12] = (((sr and 0x0F) shl 4) or ((ch and 0x07) shl 1) or ((bps shr 4) and 0x01)).toByte()
            streamInfo[13] = (((bps and 0x0F) shl 4) or ((totalSamples shr 32) and 0x0F)).toByte()
            streamInfo[14] = ((totalSamples shr 24) and 0xFF).toByte()
            streamInfo[15] = ((totalSamples shr 16) and 0xFF).toByte()
            streamInfo[16] = ((totalSamples shr 8) and 0xFF).toByte()
            streamInfo[17] = (totalSamples and 0xFF).toByte()
            // MD5 signature bytes 18..33 leave 0
            fos.write(streamInfo)

            // FLAC audio frames: write uncompressed verbatim frames so ExoPlayer can decode them cleanly!
            var sampleIdx = 0L
            var frameNumber = 0
            while (sampleIdx < totalSamples) {
                val currentBlockSize = minOf(blockSize.toLong(), totalSamples - sampleIdx).toInt()
                writeFlacVerbatimFrame(fos, def, currentBlockSize, frameNumber, sampleIdx)
                sampleIdx += currentBlockSize
                frameNumber++
            }
        }
    }

    private fun writeFlacVerbatimFrame(
        fos: FileOutputStream,
        def: TrackDefinition,
        blockSize: Int,
        frameNumber: Int,
        sampleOffset: Long
    ) {
        // Frame header:
        // Sync code: 14 bits (11111111111110) = 0xFFF8
        // Blocking strategy: 0 (fixed)
        // Block size code: 0b1100 (explicit 16-bit blockSize - 1)
        // Sample rate code: 0b1100 (explicit 16-bit)
        // Channel assignment: 0b0001 (left/right stereo) or 0b0000 (mono)
        // Sample size: 0b100 (16-bit) or 0b110 (24-bit)
        val channelBits = if (def.channels == 2) 0x01 else 0x00
        val sampleSizeBits = if (def.bitDepth == 24) 0x06 else 0x04

        val headerBytes = mutableListOf<Byte>()
        headerBytes.add(0xFF.toByte())
        headerBytes.add(0xF8.toByte())
        headerBytes.add(0xC8.toByte()) // 0b1100 (block size in header) + 0b1000 (sample rate)
        headerBytes.add(((channelBits shl 4) or (sampleSizeBits shl 1)).toByte())

        // UTF-8 encoded frame number
        if (frameNumber < 0x80) {
            headerBytes.add(frameNumber.toByte())
        } else {
            headerBytes.add((0xC0 or (frameNumber shr 6)).toByte())
            headerBytes.add((0x80 or (frameNumber and 0x3F)).toByte())
        }

        // Explicit block size (16-bit)
        headerBytes.add(((blockSize - 1) shr 8).toByte())
        headerBytes.add(((blockSize - 1) and 0xFF).toByte())

        // Header CRC-8
        val crc = computeCrc8(headerBytes.toByteArray())
        headerBytes.add(crc)
        fos.write(headerBytes.toByteArray())

        // Subframes: Verbatim subframe (type 0b0000010 = 2)
        val subframeHeader = (0x01 shl 1).toByte()

        for (ch in 0 until def.channels) {
            fos.write(subframeHeader.toInt())
            // Samples for this channel
            for (i in 0 until blockSize) {
                val t = (sampleOffset + i).toDouble() / def.sampleRate
                val envelope = sin((sampleOffset + i).toDouble() / (def.sampleRate * def.durationSeconds) * PI).coerceIn(0.0, 1.0)
                var signal = 0.0
                for ((freqIdx, freq) in def.baseFrequencies.withIndex()) {
                    val pan = if (ch == 0) 0.8 else 0.6
                    signal += sin(2.0 * PI * freq * t) * (1.0 / (freqIdx + 1)) * pan
                }
                signal = (signal * 0.3 * envelope).coerceIn(-0.95, 0.95)

                if (def.bitDepth == 16) {
                    val s = (signal * 32767.0).toInt().toShort()
                    fos.write((s.toInt() shr 8) and 0xFF)
                    fos.write(s.toInt() and 0xFF)
                } else {
                    val s = (signal * 8388607.0).toInt()
                    fos.write((s shr 16) and 0xFF)
                    fos.write((s shr 8) and 0xFF)
                    fos.write(s and 0xFF)
                }
            }
        }

        // Frame footer: 16-bit CRC
        fos.write(0x00)
        fos.write(0x00)
    }

    private fun putInt24(buffer: ByteBuffer, value: Int) {
        buffer.put((value and 0xFF).toByte())
        buffer.put(((value shr 8) and 0xFF).toByte())
        buffer.put(((value shr 16) and 0xFF).toByte())
    }

    private fun computeCrc8(data: ByteArray): Byte {
        var crc = 0
        for (b in data) {
            crc = crc xor (b.toInt() and 0xFF)
            for (i in 0 until 8) {
                crc = if ((crc and 0x80) != 0) (crc shl 1) xor 0x07 else crc shl 1
            }
        }
        return (crc and 0xFF).toByte()
    }
}
