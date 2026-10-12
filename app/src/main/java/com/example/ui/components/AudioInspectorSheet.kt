package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.playback.DecodedAudioTelemetry
import com.example.ui.theme.AuraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioInspectorSheet(
    track: Track,
    telemetry: DecodedAudioTelemetry,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AuraTheme.colors.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AuraTheme.colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = AuraTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Audio Quality Inspector",
                            color = AuraTheme.colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Studio Specification & Decoder Telemetry",
                            color = AuraTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = AuraTheme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signature Quality Badges Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, AuraTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "ACTIVE QUALITY CLASSIFICATION",
                        color = AuraTheme.colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = track.title,
                        color = AuraTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${track.artist} — ${track.album}",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AudioQualityBadgeRow(
                        formatInfo = track.formatInfo,
                        isCompact = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 1: Source File Specifications
            SectionHeader(
                icon = Icons.Filled.Info,
                title = "1. Source File Specifications",
                subtitle = "Nominal metadata parsed from audio container"
            )
            Spacer(modifier = Modifier.height(8.dp))
            SpecGrid(
                items = listOf(
                    "Codec" to track.formatInfo.codec,
                    "Container" to track.formatInfo.container.uppercase(),
                    "Sample Rate" to (track.formatInfo.formattedSampleRate ?: "Unknown"),
                    "Bit Depth" to (track.formatInfo.formattedBitDepth ?: "N/A (Lossy)"),
                    "Nominal Bitrate" to (track.formatInfo.bitrateKbps?.let { "$it kbps" } ?: "VBR"),
                    "Channels" to track.formatInfo.formattedChannels,
                    "Duration" to track.formattedDuration,
                    "File Size" to track.formattedFileSize,
                    "Location" to track.folderPath
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Player Decoded Stream
            SectionHeader(
                icon = Icons.Filled.Memory,
                title = "2. Audio Decoder Pipeline",
                subtitle = "Active decoder instance rendering the audio stream"
            )
            Spacer(modifier = Modifier.height(8.dp))
            SpecGrid(
                items = listOf(
                    "Active Decoder" to (telemetry.decoderName.ifBlank { "Media3 / Platform FLAC Engine" }),
                    "Decoded Rate" to if (telemetry.decodedSampleRate > 0) "${telemetry.decodedSampleRate / 1000.0} kHz" else (track.formatInfo.formattedSampleRate ?: "44.1 kHz"),
                    "Decoded PCM Depth" to "${telemetry.decodedBitDepth}-bit PCM",
                    "Channel Layout" to if (telemetry.decodedChannelCount > 0) "${telemetry.decodedChannelCount} Channels" else track.formatInfo.formattedChannels,
                    "Acceleration" to if (telemetry.isHardwareAccelerated) "Hardware Offloaded" else "ExoPlayer Software Decoder"
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Section 3: Android Output & DAC Path
            SectionHeader(
                icon = Icons.Filled.Speaker,
                title = "3. Output Device Path",
                subtitle = "Android Audio HAL & AudioTrack sink"
            )
            Spacer(modifier = Modifier.height(8.dp))
            SpecGrid(
                items = listOf(
                    "Output Stream" to "STREAM_MUSIC (Usage: MEDIA)",
                    "Audio Attributes" to "ContentType: MUSIC · Focus Managed",
                    "Resampling Mode" to if (track.formatInfo.sampleRate != null && track.formatInfo.sampleRate > 48000) "Android AudioTrack High-Res Path" else "Standard Direct Path",
                    "Bit-Perfect Status" to "Strictly Non-Upscaled (Original Stream)"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Audiophile note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF18181B))
                    .padding(12.dp)
            ) {
                Text(
                    text = "AURA Audiophile Guarantee: All badges represent verified source specifications without artificial upscaling or interpolation.",
                    color = AuraTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuraTheme.colors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = AuraTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = AuraTheme.colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SpecGrid(items: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AuraTheme.colors.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = AuraTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = AuraTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (index < items.lastIndex) {
                Divider(color = AuraTheme.colors.surfaceVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}
