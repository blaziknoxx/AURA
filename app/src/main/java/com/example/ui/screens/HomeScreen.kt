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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.components.AlbumArtView
import com.example.ui.components.AudioQualityBadgeRow
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.AuraTheme

@Composable
fun HomeScreen(
    recentlyPlayed: List<Track>,
    favoriteTracks: List<Track>,
    hiResTracks: List<Track>,
    cdQualityTracks: List<Track>,
    losslessTracks: List<Track>,
    albums: List<String>,
    artists: List<String>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onInspectSpecs: (Track) -> Unit,
    onImportFilesClick: () -> Unit,
    onScanStorageClick: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Header & Audiophile Status Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AuraTheme.colors.surfaceVariant,
                                AuraTheme.colors.surface
                            )
                        )
                    )
                    .border(1.dp, AuraTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AuraTheme.colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    color = AuraTheme.colors.onPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AURA",
                                    color = AuraTheme.colors.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Hi-Res Studio Engine",
                                    color = AuraTheme.colors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Storage Scan Action
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onImportFilesClick,
                                colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.primary),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("import_audio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AuraTheme.colors.onPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Import",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AuraTheme.colors.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Lossless local audio rendered with uncompressed bit-perfect resolution. Verified metadata extraction and studio playback badges.",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Curated Audiophile Quality Collections (Hi-Res, CD Quality, Lossless)
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Audiophile Collections",
                    color = AuraTheme.colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        CollectionBadgeCard(
                            title = "Hi-Res Collection",
                            subtitle = "${hiResTracks.size} verified tracks",
                            icon = Icons.Filled.Diamond,
                            accentColor = AuraTheme.colors.gold,
                            onClick = {
                                if (hiResTracks.isNotEmpty()) onTrackClick(hiResTracks.first(), hiResTracks)
                            }
                        )
                    }
                    item {
                        CollectionBadgeCard(
                            title = "CD Quality Master",
                            subtitle = "${cdQualityTracks.size} Red Book tracks",
                            icon = Icons.Filled.DiscFull,
                            accentColor = Color(0xFF38BDF8),
                            onClick = {
                                if (cdQualityTracks.isNotEmpty()) onTrackClick(cdQualityTracks.first(), cdQualityTracks)
                            }
                        )
                    }
                    item {
                        CollectionBadgeCard(
                            title = "All Lossless Audio",
                            subtitle = "${losslessTracks.size} FLAC & WAV tracks",
                            icon = Icons.Filled.Hearing,
                            accentColor = AuraTheme.colors.accent,
                            onClick = {
                                if (losslessTracks.isNotEmpty()) onTrackClick(losslessTracks.first(), losslessTracks)
                            }
                        )
                    }
                }
            }
        }

        // Recently Played Section
        if (recentlyPlayed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionTitle(title = "Recently Played")
            }
            items(recentlyPlayed.take(4)) { track ->
                TrackItemRow(
                    track = track,
                    isCurrentTrack = track.id == currentTrack?.id,
                    isPlaying = isPlaying,
                    onClick = { onTrackClick(track, recentlyPlayed) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onAddToQueue = { onAddToQueue(track) },
                    onAddToPlaylist = { onAddToPlaylist(track) },
                    onInspectSpecs = { onInspectSpecs(track) }
                )
            }
        }

        // Favorite Tracks Section
        if (favoriteTracks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionTitle(title = "Favorite Tracks")
            }
            items(favoriteTracks.take(4)) { track ->
                TrackItemRow(
                    track = track,
                    isCurrentTrack = track.id == currentTrack?.id,
                    isPlaying = isPlaying,
                    onClick = { onTrackClick(track, favoriteTracks) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onAddToQueue = { onAddToQueue(track) },
                    onAddToPlaylist = { onAddToPlaylist(track) },
                    onInspectSpecs = { onInspectSpecs(track) }
                )
            }
        }

        // Hi-Res Tracks Showcase
        if (hiResTracks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionTitle(title = "Hi-Res Studio Master Vault (24-bit / 96k+)")
            }
            items(hiResTracks.take(5)) { track ->
                TrackItemRow(
                    track = track,
                    isCurrentTrack = track.id == currentTrack?.id,
                    isPlaying = isPlaying,
                    onClick = { onTrackClick(track, hiResTracks) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onAddToQueue = { onAddToQueue(track) },
                    onAddToPlaylist = { onAddToPlaylist(track) },
                    onInspectSpecs = { onInspectSpecs(track) }
                )
            }
        }

        // Albums Carousel
        if (albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionTitle(title = "Albums")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albums) { album ->
                        CategoryCard(
                            title = album,
                            subtitle = "Album",
                            icon = Icons.Filled.Album,
                            onClick = onNavigateToLibrary
                        )
                    }
                }
            }
        }

        // Artists Carousel
        if (artists.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionTitle(title = "Artists")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artists) { artist ->
                        CategoryCard(
                            title = artist,
                            subtitle = "Artist",
                            icon = Icons.Filled.Person,
                            isCircle = true,
                            onClick = onNavigateToLibrary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = AuraTheme.colors.onSurface,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun CollectionBadgeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AuraTheme.colors.surface)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = AuraTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = AuraTheme.colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isCircle: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(if (isCircle) CircleShape else RoundedCornerShape(12.dp))
                .background(AuraTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraTheme.colors.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = AuraTheme.colors.onSurface,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            color = AuraTheme.colors.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
