package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AuraTheme

@Composable
fun AlbumArtView(
    albumArtUri: String?,
    title: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 8.dp,
    showVinylGrooves: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(AuraTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!albumArtUri.isNullOrBlank()) {
            AsyncImage(
                model = albumArtUri,
                contentDescription = "Album art for $title",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // High-fidelity audiophile generative artwork with vinyl grooves
            val primaryColor = AuraTheme.colors.primary
            val surfaceColor = AuraTheme.colors.surface
            val goldColor = AuraTheme.colors.gold

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2f

                // Dark radial vinyl gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF26262B), Color(0xFF101014), Color(0xFF09090C)),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                if (showVinylGrooves) {
                    // Concentric vinyl sound grooves
                    for (i in 2..8) {
                        val r = radius * (i / 10f)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.04f + (i % 2) * 0.03f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 1f)
                        )
                    }
                }

                // Center label disc
                val labelRadius = radius * 0.35f
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(surfaceColor, primaryColor.copy(alpha = 0.25f), goldColor.copy(alpha = 0.2f))
                    ),
                    radius = labelRadius,
                    center = center
                )

                // Outer border of center label
                drawCircle(
                    color = primaryColor.copy(alpha = 0.5f),
                    radius = labelRadius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Spindle hole
                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.08f,
                    center = center
                )
            }

            // Monogram initial
            val letter = title.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "A"
            if (size >= 48.dp) {
                Text(
                    text = letter,
                    color = AuraTheme.colors.primary.copy(alpha = 0.85f),
                    fontSize = (size.value * 0.24f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
