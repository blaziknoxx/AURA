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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioQuality
import com.example.model.Track
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.AuraTheme

@Composable
fun SearchScreen(
    allTracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackClick: (Track, List<Track>) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onInspectSpecs: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFormatFilter by remember { mutableStateOf("ALL") }

    val formatFilters = listOf("ALL", "HI-RES", "CD QUALITY", "FLAC", "WAV", "MP3")

    val filteredTracks = remember(allTracks, searchQuery, selectedFormatFilter) {
        allTracks.filter { track ->
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                track.title.contains(searchQuery, ignoreCase = true) ||
                        track.artist.contains(searchQuery, ignoreCase = true) ||
                        track.album.contains(searchQuery, ignoreCase = true) ||
                        track.formatInfo.codec.contains(searchQuery, ignoreCase = true)
            }

            val matchesFilter = when (selectedFormatFilter) {
                "ALL" -> true
                "HI-RES" -> track.formatInfo.quality == AudioQuality.HI_RES
                "CD QUALITY" -> track.formatInfo.quality == AudioQuality.CD_QUALITY
                "FLAC" -> track.formatInfo.codec.equals("FLAC", ignoreCase = true)
                "WAV" -> track.formatInfo.codec.equals("WAV", ignoreCase = true)
                "MP3" -> track.formatInfo.codec.equals("MP3", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Search",
                color = AuraTheme.colors.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search songs, artists, albums, or codecs...", color = AuraTheme.colors.onSurfaceVariant)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = AuraTheme.colors.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = AuraTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraTheme.colors.primary,
                    unfocusedBorderColor = AuraTheme.colors.surfaceVariant,
                    focusedContainerColor = AuraTheme.colors.surface,
                    unfocusedContainerColor = AuraTheme.colors.surface,
                    focusedTextColor = AuraTheme.colors.onSurface,
                    unfocusedTextColor = AuraTheme.colors.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Format Quick Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(formatFilters) { filter ->
                    val isSelected = selectedFormatFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AuraTheme.colors.primary else AuraTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                if (isSelected) AuraTheme.colors.primary else AuraTheme.colors.surfaceVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedFormatFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) AuraTheme.colors.onPrimary else AuraTheme.colors.onSurface,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Search Results List
        if (filteredTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = AuraTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching audio tracks found",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = "${filteredTracks.size} results",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                items(filteredTracks) { track ->
                    TrackItemRow(
                        track = track,
                        isCurrentTrack = track.id == currentTrack?.id,
                        isPlaying = isPlaying,
                        onClick = { onTrackClick(track, filteredTracks) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onAddToQueue = { onAddToQueue(track) },
                        onAddToPlaylist = { onAddToPlaylist(track) },
                        onInspectSpecs = { onInspectSpecs(track) }
                    )
                }
            }
        }
    }
}
