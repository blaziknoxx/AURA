package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemeOption
import com.example.ui.theme.AuraTheme

@Composable
fun SettingsScreen(
    currentTheme: ThemeOption,
    onSelectTheme: (ThemeOption) -> Unit,
    onImportFiles: () -> Unit,
    onScanStorage: () -> Unit,
    totalTrackCount: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Settings",
                color = AuraTheme.colors.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AURA Audiophile Configuration & Luxury Theming",
                color = AuraTheme.colors.onSurfaceVariant,
                fontSize = 12.5.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section: Luxury Themes (Signature requirement)
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = AuraTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Luxury Theming System",
                        color = AuraTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant 6-palette luxury switching with persistent storage",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 6 Theme Preview Cards
        items(ThemeOption.entries) { theme ->
            val isSelected = currentTheme == theme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.surfaceColor)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) theme.primaryColor else theme.surfaceVariantColor,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectTheme(theme) }
                    .padding(14.dp)
                    .testTag("theme_card_${theme.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = theme.title,
                                color = if (isSelected) theme.primaryColor else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Active Theme",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = theme.subtitle,
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Swatches
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(theme.backgroundColor)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceColor)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                        )
                    }
                }
            }
        }

        // Section: Audio Engine Specifications
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = AuraTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Audiophile Engine & DAC Output",
                        color = AuraTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Media3 ExoPlayer High-Fidelity Audio Pipeline",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraTheme.colors.surface)
                    .border(1.dp, AuraTheme.colors.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingDetailRow(
                        label = "FLAC Stream Decoder",
                        value = "Xiph.Org Spec / Platform Decoder"
                    )
                    SettingDetailRow(
                        label = "PCM Resolution",
                        value = "Up to 32-bit float / 24-bit integer"
                    )
                    SettingDetailRow(
                        label = "Sampling Frequencies",
                        value = "44.1, 48.0, 88.2, 96.0, 192.0 kHz"
                    )
                    SettingDetailRow(
                        label = "Audio Becoming Noisy",
                        value = "Auto-Pause on Headphone Disconnect"
                    )
                    SettingDetailRow(
                        label = "Audio Focus Policy",
                        value = "Gain Transient with Duck / Call Pause"
                    )
                }
            }
        }

        // Section: Storage & Music Library
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = null,
                    tint = AuraTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Local Music Library",
                        color = AuraTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalTrackCount offline tracks indexed",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onImportFiles,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AuraTheme.colors.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Import Audio",
                        color = AuraTheme.colors.onPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onScanStorage,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AuraTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Scan Device",
                        color = AuraTheme.colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section: About & Audiophile Integrity Statement
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF111115))
                    .border(1.dp, AuraTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "AURA v1.0 AUDIOPHILE PLAYER",
                        color = AuraTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Built for pure, bit-authentic offline listening. Audio badges display true container metadata and decoded telemetry without artificial upscaling.",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = AuraTheme.colors.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = AuraTheme.colors.onSurface,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
