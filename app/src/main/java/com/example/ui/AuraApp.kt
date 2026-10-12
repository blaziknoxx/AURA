package com.example.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.example.playback.AuraPlaybackService
import com.example.ui.components.AudioInspectorSheet
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PlaylistPickerSheet
import com.example.ui.components.QueueSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AuraAppTheme
import com.example.ui.theme.AuraTheme
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun AuraApp(viewModel: AuraViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    val allTracks by viewModel.allTracks.collectAsState()
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val hiResTracks by viewModel.hiResTracks.collectAsState()
    val cdQualityTracks by viewModel.cdQualityTracks.collectAsState()
    val losslessTracks by viewModel.losslessTracks.collectAsState()
    val albums by viewModel.distinctAlbums.collectAsState()
    val artists by viewModel.distinctArtists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val playlistTracks by viewModel.playlistTracks.collectAsState()

    val inspectingTrack by viewModel.inspectingTrack.collectAsState()
    val addingToPlaylistTrack by viewModel.addingToPlaylistTrack.collectAsState()
    val showNowPlaying by viewModel.showNowPlaying.collectAsState()
    val showQueueSheet by viewModel.showQueueSheet.collectAsState()

    var selectedNavTab by remember { mutableIntStateOf(0) }

    // SAF Document Picker for audio importing
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importUris(uris)
            scope.launch {
                snackbarHostState.showSnackbar("Importing ${uris.size} audio files into AURA")
            }
        }
    }

    // MediaStore Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanMediaStore()
            scope.launch {
                snackbarHostState.showSnackbar("Scanning device storage for lossless audio...")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Storage access required to index device music files")
            }
        }
    }

    // Start playback service for media session & background audio
    remember {
        try {
            val serviceIntent = Intent(context, AuraPlaybackService::class.java)
            context.startService(serviceIntent)
        } catch (_: Exception) {}
        true
    }

    // Handle system back navigation
    BackHandler(enabled = showNowPlaying || selectedPlaylist != null) {
        if (showNowPlaying) {
            viewModel.setShowNowPlaying(false)
        } else if (selectedPlaylist != null) {
            viewModel.selectPlaylist(null)
        }
    }

    AuraAppTheme(selectedTheme = currentTheme) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(AuraTheme.colors.background),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column {
                    // Floating MiniPlayer right above bottom navigation
                    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f
                    MiniPlayer(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        progress = progress,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onClick = { viewModel.setShowNowPlaying(true) }
                    )

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = AuraTheme.colors.surface,
                        contentColor = AuraTheme.colors.primary,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("bottom_nav_bar")
                    ) {
                        val navItems = listOf(
                            Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
                            Triple("Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
                            Triple("Search", Icons.Filled.Search, Icons.Outlined.Search),
                            Triple("Playlists", Icons.Filled.PlaylistPlay, Icons.Outlined.PlaylistPlay),
                            Triple("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
                        )

                        navItems.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
                            val isSelected = selectedNavTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    selectedNavTab = index
                                    if (index != 3) viewModel.selectPlaylist(null)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                        contentDescription = label,
                                        tint = if (isSelected) AuraTheme.colors.primary else AuraTheme.colors.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        color = if (isSelected) AuraTheme.colors.primary else AuraTheme.colors.onSurfaceVariant
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = AuraTheme.colors.primary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(AuraTheme.colors.background)
            ) {
                when (selectedNavTab) {
                    0 -> HomeScreen(
                        recentlyPlayed = recentlyPlayed,
                        favoriteTracks = favoriteTracks,
                        hiResTracks = hiResTracks,
                        cdQualityTracks = cdQualityTracks,
                        losslessTracks = losslessTracks,
                        albums = albums,
                        artists = artists,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                        onToggleFavorite = { track -> viewModel.toggleFavorite(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onAddToPlaylist = { track -> viewModel.setAddingToPlaylistTrack(track) },
                        onInspectSpecs = { track -> viewModel.setInspectingTrack(track) },
                        onImportFilesClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "audio/*",
                                    "application/ogg",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                        onScanStorageClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        },
                        onNavigateToLibrary = { selectedNavTab = 1 }
                    )

                    1 -> LibraryScreen(
                        allTracks = allTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                        onToggleFavorite = { track -> viewModel.toggleFavorite(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onAddToPlaylist = { track -> viewModel.setAddingToPlaylistTrack(track) },
                        onInspectSpecs = { track -> viewModel.setInspectingTrack(track) }
                    )

                    2 -> SearchScreen(
                        allTracks = allTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                        onToggleFavorite = { track -> viewModel.toggleFavorite(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onAddToPlaylist = { track -> viewModel.setAddingToPlaylistTrack(track) },
                        onInspectSpecs = { track -> viewModel.setInspectingTrack(track) }
                    )

                    3 -> PlaylistsScreen(
                        playlists = playlists,
                        selectedPlaylist = selectedPlaylist,
                        playlistTracks = playlistTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onSelectPlaylist = { pl -> viewModel.selectPlaylist(pl) },
                        onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                        onRenamePlaylist = { id, name -> viewModel.renamePlaylist(id, name) },
                        onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                        onPlayTrackInPlaylist = { track, list -> viewModel.playTrack(track, list) },
                        onRemoveTrackFromPlaylist = { plId, trId -> viewModel.removeTrackFromPlaylist(plId, trId) },
                        onMoveTrack = { plId, trId, newOrder -> viewModel.moveTrackInPlaylist(plId, trId, newOrder) },
                        onInspectSpecs = { track -> viewModel.setInspectingTrack(track) }
                    )

                    4 -> SettingsScreen(
                        currentTheme = currentTheme,
                        onSelectTheme = { theme -> viewModel.setTheme(theme) },
                        onImportFiles = {
                            filePickerLauncher.launch(
                                arrayOf("audio/*", "*/*")
                            )
                        },
                        onScanStorage = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        },
                        totalTrackCount = allTracks.size
                    )
                }
            }
        }

        // Fullscreen Now Playing Overlay
        AnimatedVisibility(
            visible = showNowPlaying && currentTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            currentTrack?.let { track ->
                NowPlayingScreen(
                    track = track,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    telemetry = telemetry,
                    shuffleEnabled = shuffleEnabled,
                    repeatMode = repeatMode,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeekTo = { pos -> viewModel.seekTo(pos) },
                    onSeekBack = { viewModel.seekBack() },
                    onSeekForward = { viewModel.seekForward() },
                    onNext = { viewModel.playNext() },
                    onPrevious = { viewModel.playPrevious() },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeat() },
                    onToggleFavorite = { viewModel.toggleFavorite(track) },
                    onOpenQueue = { viewModel.setShowQueueSheet(true) },
                    onInspectSpecs = { viewModel.setInspectingTrack(track) },
                    onDismiss = { viewModel.setShowNowPlaying(false) }
                )
            }
        }

        // Audio Inspector Bottom Sheet
        inspectingTrack?.let { track ->
            AudioInspectorSheet(
                track = track,
                telemetry = telemetry,
                onDismiss = { viewModel.setInspectingTrack(null) }
            )
        }

        // Playlist Picker Bottom Sheet
        addingToPlaylistTrack?.let { track ->
            PlaylistPickerSheet(
                track = track,
                playlists = playlists,
                onAddToPlaylist = { playlist ->
                    viewModel.addTrackToPlaylist(playlist.id, track.id)
                    scope.launch {
                        snackbarHostState.showSnackbar("Added '${track.title}' to '${playlist.name}'")
                    }
                },
                onCreatePlaylist = { newName ->
                    viewModel.createPlaylist(newName)
                },
                onDismiss = { viewModel.setAddingToPlaylistTrack(null) }
            )
        }

        // Playback Queue Bottom Sheet
        if (showQueueSheet) {
            QueueSheet(
                queue = queue,
                currentIndex = currentIndex,
                onTrackSelected = { index -> viewModel.playTrackFromQueue(index) },
                onRemoveFromQueue = { index -> viewModel.removeFromQueue(index) },
                onDismiss = { viewModel.setShowQueueSheet(false) }
            )
        }
    }
}
