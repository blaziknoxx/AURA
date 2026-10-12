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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.components.AlbumArtView
import com.example.ui.components.AudioQualityBadgeRow
import com.example.ui.theme.AuraTheme

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    selectedPlaylist: Playlist?,
    playlistTracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onSelectPlaylist: (Playlist?) -> Unit,
    onCreatePlaylist: (String, String) -> Unit,
    onRenamePlaylist: (String, String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onPlayTrackInPlaylist: (Track, List<Track>) -> Unit,
    onRemoveTrackFromPlaylist: (String, String) -> Unit,
    onMoveTrack: (String, String, Int) -> Unit,
    onInspectSpecs: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var playlistNameInput by remember { mutableStateOf("") }
    var playlistDescInput by remember { mutableStateOf("") }

    if (selectedPlaylist == null) {
        // Playlists Overview List
        Column(
            modifier = modifier
                .fillMaxSize()
                .testTag("playlists_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playlists",
                        color = AuraTheme.colors.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${playlists.size} playlists created",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        playlistNameInput = ""
                        playlistDescInput = ""
                        showCreateDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("create_playlist_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AuraTheme.colors.onPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "New",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuraTheme.colors.onPrimary
                    )
                }
            }

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PlaylistPlay,
                            contentDescription = null,
                            tint = AuraTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No custom playlists yet",
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
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectPlaylist(playlist) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AuraTheme.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlaylistPlay,
                                    contentDescription = null,
                                    tint = AuraTheme.colors.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.name,
                                    color = AuraTheme.colors.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${playlist.trackCount} tracks ${if (playlist.description.isNotBlank()) "· ${playlist.description}" else ""}",
                                    color = AuraTheme.colors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Playlist Detail View
        Column(
            modifier = modifier
                .fillMaxSize()
                .testTag("playlist_detail_screen")
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSelectPlaylist(null) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraTheme.colors.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedPlaylist.name,
                        color = AuraTheme.colors.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playlistTracks.size} tracks",
                        color = AuraTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                // Rename Button
                IconButton(onClick = {
                    playlistNameInput = selectedPlaylist.name
                    showRenameDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Rename",
                        tint = AuraTheme.colors.onSurfaceVariant
                    )
                }

                // Delete Button
                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            // Play All Action Button
            if (playlistTracks.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Button(
                        onClick = { onPlayTrackInPlaylist(playlistTracks.first(), playlistTracks) },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("play_all_playlist_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = AuraTheme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Play All (${playlistTracks.size})",
                            color = AuraTheme.colors.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Playlist Tracks with Reorder and Remove
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                itemsIndexed(playlistTracks) { index, track ->
                    val isCurrent = track.id == currentTrack?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayTrackInPlaylist(track, playlistTracks) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlbumArtView(
                            albumArtUri = track.albumArtUri,
                            title = track.title,
                            size = 42.dp,
                            cornerRadius = 6.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = track.title,
                                    color = if (isCurrent) AuraTheme.colors.primary else AuraTheme.colors.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AudioQualityBadgeRow(
                                    formatInfo = track.formatInfo,
                                    isCompact = true
                                )
                            }
                            Text(
                                text = "${track.artist} · ${track.album}",
                                color = AuraTheme.colors.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Reorder up
                        if (index > 0) {
                            IconButton(
                                onClick = { onMoveTrack(selectedPlaylist.id, track.id, index - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Move Up",
                                    tint = AuraTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Reorder down
                        if (index < playlistTracks.lastIndex) {
                            IconButton(
                                onClick = { onMoveTrack(selectedPlaylist.id, track.id, index + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Move Down",
                                    tint = AuraTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Remove from playlist
                        IconButton(
                            onClick = { onRemoveTrackFromPlaylist(selectedPlaylist.id, track.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircleOutline,
                                contentDescription = "Remove",
                                tint = AuraTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create Playlist
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Playlist", color = AuraTheme.colors.onSurface) },
            text = {
                Column {
                    OutlinedTextField(
                        value = playlistNameInput,
                        onValueChange = { playlistNameInput = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraTheme.colors.primary,
                            unfocusedBorderColor = AuraTheme.colors.surfaceVariant,
                            focusedTextColor = AuraTheme.colors.onSurface,
                            unfocusedTextColor = AuraTheme.colors.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = playlistDescInput,
                        onValueChange = { playlistDescInput = it },
                        label = { Text("Description (Optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuraTheme.colors.primary,
                            unfocusedBorderColor = AuraTheme.colors.surfaceVariant,
                            focusedTextColor = AuraTheme.colors.onSurface,
                            unfocusedTextColor = AuraTheme.colors.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistNameInput.isNotBlank()) {
                            onCreatePlaylist(playlistNameInput.trim(), playlistDescInput.trim())
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.primary)
                ) {
                    Text("Create", color = AuraTheme.colors.onPrimary)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.surfaceVariant)
                ) {
                    Text("Cancel", color = AuraTheme.colors.onSurfaceVariant)
                }
            },
            containerColor = AuraTheme.colors.surface
        )
    }

    // Dialog: Rename Playlist
    if (showRenameDialog && selectedPlaylist != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Playlist", color = AuraTheme.colors.onSurface) },
            text = {
                OutlinedTextField(
                    value = playlistNameInput,
                    onValueChange = { playlistNameInput = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraTheme.colors.primary,
                        unfocusedBorderColor = AuraTheme.colors.surfaceVariant,
                        focusedTextColor = AuraTheme.colors.onSurface,
                        unfocusedTextColor = AuraTheme.colors.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistNameInput.isNotBlank()) {
                            onRenamePlaylist(selectedPlaylist.id, playlistNameInput.trim())
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.primary)
                ) {
                    Text("Rename", color = AuraTheme.colors.onPrimary)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showRenameDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.surfaceVariant)
                ) {
                    Text("Cancel", color = AuraTheme.colors.onSurfaceVariant)
                }
            },
            containerColor = AuraTheme.colors.surface
        )
    }

    // Dialog: Confirm Delete Playlist
    if (showDeleteConfirmDialog && selectedPlaylist != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Playlist?", color = AuraTheme.colors.onSurface) },
            text = {
                Text(
                    "Are you sure you want to delete '${selectedPlaylist.name}'? Audio files on disk will not be deleted.",
                    color = AuraTheme.colors.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaylist(selectedPlaylist.id)
                        showDeleteConfirmDialog = false
                        onSelectPlaylist(null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraTheme.colors.surfaceVariant)
                ) {
                    Text("Cancel", color = AuraTheme.colors.onSurfaceVariant)
                }
            },
            containerColor = AuraTheme.colors.surface
        )
    }
}
