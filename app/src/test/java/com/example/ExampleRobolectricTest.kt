package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.FlacHeaderParser
import com.example.model.AudioFormatInfo
import com.example.model.AudioQuality
import com.example.model.ThemeOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies app name is AURA`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AURA", appName)
    }

    @Test
    fun `verify audio quality badge classification logic`() {
        val hiResFlac = AudioFormatInfo(
            codec = "FLAC",
            container = "flac",
            bitDepth = 24,
            sampleRate = 96000,
            bitrateKbps = 4608,
            quality = AudioQuality.HI_RES
        )
        assertEquals("24-bit · 96 kHz", hiResFlac.formattedSpecBadge)
        assertEquals(AudioQuality.HI_RES, hiResFlac.quality)

        val cdFlac = AudioFormatInfo(
            codec = "FLAC",
            container = "flac",
            bitDepth = 16,
            sampleRate = 44100,
            bitrateKbps = 1411,
            quality = AudioQuality.CD_QUALITY
        )
        assertEquals("16-bit · 44.1 kHz", cdFlac.formattedSpecBadge)
        assertEquals(AudioQuality.CD_QUALITY, cdFlac.quality)

        val lossyMp3 = AudioFormatInfo(
            codec = "MP3",
            container = "mp3",
            bitrateKbps = 320,
            quality = AudioQuality.LOSSY
        )
        assertEquals("320 kbps", lossyMp3.formattedSpecBadge)
        assertEquals(AudioQuality.LOSSY, lossyMp3.quality)
    }

    @Test
    fun `verify theme options include all six luxury themes`() {
        val themes = ThemeOption.entries
        assertEquals(6, themes.size)
        assertTrue(themes.any { it.name == "AURA_GREEN" })
        assertTrue(themes.any { it.name == "OBSIDIAN" })
        assertTrue(themes.any { it.name == "MIDNIGHT_BLUE" })
        assertTrue(themes.any { it.name == "ROYAL_VIOLET" })
        assertTrue(themes.any { it.name == "CHAMPAGNE_GOLD" })
        assertTrue(themes.any { it.name == "ARCTIC_SILVER" })
    }

    @Test
    fun `flac stream info parser correctly parses sample rate and bit depth`() {
        // Construct mock 4-byte "fLaC" + STREAMINFO block
        val stream = ByteArray(4 + 4 + 34)
        // Magic
        stream[0] = 0x66.toByte() // 'f'
        stream[1] = 0x4C.toByte() // 'L'
        stream[2] = 0x61.toByte() // 'a'
        stream[3] = 0x43.toByte() // 'C'
        // Block header: isLast=true, type=0 (STREAMINFO), length=34
        stream[4] = 0x80.toByte()
        stream[5] = 0x00
        stream[6] = 0x00
        stream[7] = 0x22

        // Set sample rate 96000 (0x17700), channels=2 (val 1), bits=24 (val 23 = 0x17)
        // In STREAMINFO, byte 10 is (sr shr 12), byte 11 is (sr shr 4), byte 12 is (sr & 0xF) shl 4 | (ch & 0x7) shl 1 | (bps shr 4)
        val sr = 96000L
        val ch = 1L // 2 - 1
        val bps = 23L // 24 - 1
        stream[8 + 10] = ((sr shr 12) and 0xFF).toByte()
        stream[8 + 11] = ((sr shr 4) and 0xFF).toByte()
        stream[8 + 12] = (((sr and 0x0F) shl 4) or (ch shl 1) or ((bps shr 4) and 0x01)).toByte()
        stream[8 + 13] = (((bps and 0x0F) shl 4)).toByte()

        val parsed = FlacHeaderParser.parseStreamInfo(ByteArrayInputStream(stream))
        assertNotNull(parsed)
        assertEquals(96000, parsed?.sampleRate)
        assertEquals(2, parsed?.channels)
        assertEquals(24, parsed?.bitsPerSample)
    }
}
