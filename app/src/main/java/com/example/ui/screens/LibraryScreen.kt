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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.AuraTheme

enum class SortMode(val label: String) {
    TITLE("By Title (A-Z)"),
    ARTIST("By Artist"),
    QUALITY("By Sample Rate (Highest)"),
    DATE_ADDED("By Recently Added")
}

@Composable
fun LibraryScreen(
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
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Albums", "Artists", "Folders")
    var sortMode by remember { mutableStateOf(SortMode.TITLE) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Selected album / artist / folder drilldown
    var filterKey by remember { mutableStateOf<String?>(null) }
    var filterType by remember { mutableStateOf<String?>(null) }

    val sortedTracks = remember(allTracks, sortMode) {
        when (sortMode) {
            SortMode.TITLE -> allTracks.sortedBy { it.title.lowercase() }
            SortMode.ARTIST -> allTracks.sortedBy { it.artist.lowercase() }
            SortMode.QUALITY -> allTracks.sortedByDescending { it.formatInfo.sampleRate ?: 0 }
            SortMode.DATE_ADDED -> allTracks.sortedByDescending { it.addedTimestamp }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Library Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Library",
                    color = AuraTheme.colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allTracks.size} tracks total",
                    color = AuraTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Sort button
            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier.testTag("library_sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sort,
                        contentDescription = "Sort Tracks",
                        tint = AuraTheme.colors.primary
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.background(AuraTheme.colors.surface)
                ) {
                    SortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = mode.label,
                                    color = if (sortMode == mode) AuraTheme.colors.primary else AuraTheme.colors.onSurface
                                )
                            },
                            onClick = {
                                sortMode = mode
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = AuraTheme.colors.background,
            contentColor = AuraTheme.colors.primary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AuraTheme.colors.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        filterKey = null
                        filterType = null
                    },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) AuraTheme.colors.primary else AuraTheme.colors.onSurfaceVariant,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // If drilled into a specific Album / Artist / Folder
        if (filterKey != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuraTheme.colors.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$filterType: $filterKey",
                    color = AuraTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Clear Filter",
                    color = AuraTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { filterKey = null; filterType = null }
                        .padding(4.dp)
                )
            }
        }

        // Tab Content
        val displayedTracks = remember(sortedTracks, filterKey, filterType) {
            if (filterKey != null) {
                when (filterType) {
                    "Album" -> sortedTracks.filter { it.album == filterKey }
                    "Artist" -> sortedTracks.filter { it.artist == filterKey }
                    "Folder" -> sortedTracks.filter { it.folderPath == filterKey }
                    else -> sortedTracks
                }
            } else {
                sortedTracks
            }
        }

        when (selectedTab) {
            0 -> {
                // Songs Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(displayedTracks) { track ->
                        TrackItemRow(
                            track = track,
                            isCurrentTrack = track.id == currentTrack?.id,
                            isPlaying = isPlaying,
                            onClick = { onTrackClick(track, displayedTracks) },
                            onToggleFavorite = { onToggleFavorite(track) },
                            onAddToQueue = { onAddToQueue(track) },
                            onAddToPlaylist = { onAddToPlaylist(track) },
                            onInspectSpecs = { onInspectSpecs(track) }
                        )
                    }
                }
            }
            1 -> {
                // Albums Tab
                val albumsMap = remember(allTracks) {
                    allTracks.groupBy { it.album.ifBlank { "Unknown Album" } }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(albumsMap.keys.toList()) { albumName ->
                        val count = albumsMap[albumName]?.size ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    filterKey = albumName
                                    filterType = "Album"
                                    selectedTab = 0
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Album,
                                    contentDescription = null,
                                    tint = AuraTheme.colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = albumName,
                                    color = AuraTheme.colors.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$count tracks",
                                    color = AuraTheme.colors.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Artists Tab
                val artistsMap = remember(allTracks) {
                    allTracks.groupBy { it.artist.ifBlank { "Unknown Artist" } }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(artistsMap.keys.toList()) { artistName ->
                        val count = artistsMap[artistName]?.size ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    filterKey = artistName
                                    filterType = "Artist"
                                    selectedTab = 0
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(AuraTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = AuraTheme.colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = artistName,
                                    color = AuraTheme.colors.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$count tracks",
                                    color = AuraTheme.colors.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                // Folders Tab
                val foldersMap = remember(allTracks) {
                    allTracks.groupBy { it.folderPath.ifBlank { "Default Storage" } }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(foldersMap.keys.toList()) { folderPath ->
                        val count = foldersMap[folderPath]?.size ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    filterKey = folderPath
                                    filterType = "Folder"
                                    selectedTab = 0
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = AuraTheme.colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = folderPath.substringAfterLast('/', folderPath),
                                    color = AuraTheme.colors.onSurface,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$count tracks · $folderPath",
                                    color = AuraTheme.colors.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
