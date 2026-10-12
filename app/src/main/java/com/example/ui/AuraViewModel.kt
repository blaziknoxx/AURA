package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.example.data.AuraDatabase
import com.example.data.AuraRepository
import com.example.model.Playlist
import com.example.model.ThemeOption
import com.example.model.Track
import com.example.playback.AuraAudioPlayer
import com.example.playback.AuraPlayerHolder
import com.example.playback.DecodedAudioTelemetry
import com.example.playback.RepeatMode
import com.example.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(UnstableApi::class, ExperimentalCoroutinesApi::class)
class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuraRepository
    val player: AuraAudioPlayer

    private val _currentTheme = MutableStateFlow(ThemePreferences.getSelectedTheme(application))
    val currentTheme: StateFlow<ThemeOption> = _currentTheme.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _inspectingTrack = MutableStateFlow<Track?>(null)
    val inspectingTrack: StateFlow<Track?> = _inspectingTrack.asStateFlow()

    private val _addingToPlaylistTrack = MutableStateFlow<Track?>(null)
    val addingToPlaylistTrack: StateFlow<Track?> = _addingToPlaylistTrack.asStateFlow()

    private val _showNowPlaying = MutableStateFlow(false)
    val showNowPlaying: StateFlow<Boolean> = _showNowPlaying.asStateFlow()

    private val _showQueueSheet = MutableStateFlow(false)
    val showQueueSheet: StateFlow<Boolean> = _showQueueSheet.asStateFlow()

    val allTracks: StateFlow<List<Track>>
    val favoriteTracks: StateFlow<List<Track>>
    val recentlyPlayed: StateFlow<List<Track>>
    val hiResTracks: StateFlow<List<Track>>
    val cdQualityTracks: StateFlow<List<Track>>
    val losslessTracks: StateFlow<List<Track>>
    val distinctAlbums: StateFlow<List<String>>
    val distinctArtists: StateFlow<List<String>>
    val distinctFolders: StateFlow<List<String>>
    val playlists: StateFlow<List<Playlist>>
    val playlistTracks: StateFlow<List<Track>>

    // Audio player flows
    val currentTrack: StateFlow<Track?>
    val isPlaying: StateFlow<Boolean>
    val currentPositionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    val queue: StateFlow<List<Track>>
    val currentIndex: StateFlow<Int>
    val shuffleEnabled: StateFlow<Boolean>
    val repeatMode: StateFlow<RepeatMode>
    val telemetry: StateFlow<DecodedAudioTelemetry>

    init {
        val db = AuraDatabase.getInstance(application)
        repository = AuraRepository(application, db)
        player = AuraPlayerHolder.getPlayer(application)

        currentTrack = player.currentTrack
        isPlaying = player.isPlaying
        currentPositionMs = player.currentPositionMs
        durationMs = player.durationMs
        queue = player.queue
        currentIndex = player.currentIndex
        shuffleEnabled = player.shuffleEnabled
        repeatMode = player.repeatMode
        telemetry = player.telemetry

        allTracks = repository.allTracks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        favoriteTracks = repository.favoriteTracks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        recentlyPlayed = repository.recentlyPlayed.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        hiResTracks = repository.hiResTracks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        cdQualityTracks = repository.cdQualityTracks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        losslessTracks = repository.allLosslessTracks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        distinctAlbums = repository.distinctAlbums.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        distinctArtists = repository.distinctArtists.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        distinctFolders = repository.distinctFolders.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        playlists = repository.playlists.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        playlistTracks = _selectedPlaylist.flatMapLatest { pl ->
            if (pl != null) repository.getTracksForPlaylist(pl.id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        player.onTrackCompleted = { finishedTrack ->
            viewModelScope.launch {
                repository.recordPlayback(finishedTrack.id)
            }
        }

        // Initialize library with authentic reference tracks if clean install
        viewModelScope.launch {
            repository.initializeLibraryIfNeeded()
        }
    }

    fun playTrack(track: Track, fromList: List<Track>) {
        val index = fromList.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.playTrackList(fromList, index)
        viewModelScope.launch {
            repository.recordPlayback(track.id)
        }
    }

    fun playTrackFromQueue(index: Int) {
        val tracks = player.queue.value
        if (index in tracks.indices) {
            player.playTrackList(tracks, index)
            viewModelScope.launch {
                repository.recordPlayback(tracks[index].id)
            }
        }
    }

    fun togglePlayPause() = player.togglePlayPause()
    fun seekTo(posMs: Long) = player.seekTo(posMs)
    fun seekForward() = player.seekForward(10000L)
    fun seekBack() = player.seekBack(10000L)
    fun playNext() = player.playNext()
    fun playPrevious() = player.playPrevious()
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeat() = player.cycleRepeatMode()
    fun addToQueue(track: Track) = player.addToQueue(track)
    fun removeFromQueue(index: Int) = player.removeFromQueue(index)

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id)
        }
    }

    fun setTheme(theme: ThemeOption) {
        _currentTheme.value = theme
        ThemePreferences.setSelectedTheme(getApplication(), theme)
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun createPlaylist(name: String, desc: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, desc)
        }
    }

    fun renamePlaylist(id: String, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(id, newName)
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun moveTrackInPlaylist(playlistId: String, trackId: String, newOrder: Int) {
        viewModelScope.launch {
            repository.reorderTrackInPlaylist(playlistId, trackId, newOrder)
        }
    }

    fun importUris(uris: List<Uri>) {
        viewModelScope.launch {
            repository.importTracksFromUris(uris)
        }
    }

    fun scanMediaStore() {
        viewModelScope.launch {
            repository.scanMediaStoreAudio()
        }
    }

    fun setInspectingTrack(track: Track?) {
        _inspectingTrack.value = track
    }

    fun setAddingToPlaylistTrack(track: Track?) {
        _addingToPlaylistTrack.value = track
    }

    fun setShowNowPlaying(show: Boolean) {
        _showNowPlaying.value = show
    }

    fun setShowQueueSheet(show: Boolean) {
        _showQueueSheet.value = show
    }
}
