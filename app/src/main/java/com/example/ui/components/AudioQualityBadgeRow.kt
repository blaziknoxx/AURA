package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioFormatInfo
import com.example.model.AudioQuality
import com.example.ui.theme.AuraTheme

/**
 * Signature Audio Quality Badges system.
 * Renders individual bordered pills with rounded corners, authentic specs,
 * and responsive wrapping to prevent overlapping.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AudioQualityBadgeRow(
    formatInfo: AudioFormatInfo,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        maxItemsInEachRow = 4
    ) {
        // 1. Codec / Container Badge
        QualityBadgePill(
            text = formatInfo.codec.uppercase(),
            borderColor = AuraTheme.colors.onSurfaceVariant.copy(alpha = 0.35f),
            backgroundColor = AuraTheme.colors.surfaceVariant.copy(alpha = 0.5f),
            textColor = AuraTheme.colors.onSurfaceVariant,
            isCompact = isCompact
        )

        // 2. Technical Spec Badge (Bit Depth + Sample Rate OR Bitrate)
        val specText = formatInfo.formattedSpecBadge
        if (specText.isNotBlank() && specText != formatInfo.codec) {
            QualityBadgePill(
                text = specText,
                borderColor = AuraTheme.colors.primary.copy(alpha = 0.4f),
                backgroundColor = AuraTheme.colors.surfaceVariant.copy(alpha = 0.6f),
                textColor = AuraTheme.colors.onSurface,
                isCompact = isCompact
            )
        }

        // 3. Quality Classification Tier Badge
        when (formatInfo.quality) {
            AudioQuality.HI_RES -> {
                QualityBadgePill(
                    text = "HI-RES",
                    borderColor = AuraTheme.colors.gold,
                    backgroundColor = AuraTheme.colors.gold.copy(alpha = 0.15f),
                    textColor = AuraTheme.colors.gold,
                    indicatorColor = AuraTheme.colors.gold,
                    isCompact = isCompact
                )
            }
            AudioQuality.CD_QUALITY -> {
                QualityBadgePill(
                    text = "CD QUALITY",
                    borderColor = Color(0xFF38BDF8),
                    backgroundColor = Color(0xFF38BDF8).copy(alpha = 0.14f),
                    textColor = Color(0xFF38BDF8),
                    indicatorColor = Color(0xFF38BDF8),
                    isCompact = isCompact
                )
            }
            AudioQuality.LOSSLESS -> {
                QualityBadgePill(
                    text = "LOSSLESS",
                    borderColor = AuraTheme.colors.accent.copy(alpha = 0.6f),
                    backgroundColor = AuraTheme.colors.accent.copy(alpha = 0.12f),
                    textColor = AuraTheme.colors.accent,
                    isCompact = isCompact
                )
            }
            AudioQuality.LOSSY -> {
                // Lossy format specs are already shown via bitrate badge
            }
            AudioQuality.UNKNOWN -> {
                // Do not display false badges if unknown
            }
        }
    }
}

@Composable
fun QualityBadgePill(
    text: String,
    borderColor: Color,
    backgroundColor: Color,
    textColor: Color,
    indicatorColor: Color? = null,
    isCompact: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hPadding = if (isCompact) 5.dp else 8.dp
    val vPadding = if (isCompact) 1.5.dp else 3.dp
    val textSize = if (isCompact) 9.sp else 10.5.sp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(width = 0.75.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = hPadding, vertical = vPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (indicatorColor != null) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
            Text(
                text = text,
                color = textColor,
                fontSize = textSize,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.4.sp,
                maxLines = 1
            )
        }
    }
}
