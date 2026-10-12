package com.example.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.example.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class AuraAudioPlayer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val TAG = "AuraAudioPlayer"

    val exoPlayer: ExoPlayer

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _telemetry = MutableStateFlow(DecodedAudioTelemetry())
    val telemetry: StateFlow<DecodedAudioTelemetry> = _telemetry.asStateFlow()

    private var positionJob: Job? = null
    private var originalQueue = listOf<Track>()

    var onTrackCompleted: ((Track) -> Unit)? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        setupPlayerListeners()
    }

    private fun setupPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    val duration = exoPlayer.duration
                    if (duration > 0) {
                        _durationMs.value = duration
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    val finished = _currentTrack.value
                    if (finished != null) {
                        onTrackCompleted?.invoke(finished)
                    }
                    handleTrackEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                _currentIndex.value = index
                val tracks = _queue.value
                if (index in tracks.indices) {
                    val track = tracks[index]
                    _currentTrack.value = track
                    _durationMs.value = if (track.durationMs > 0) track.durationMs else 0L
                    _currentPositionMs.value = 0L
                }
            }
        })

        // Real-time telemetry tracking from ExoPlayer decoder pipeline
        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
            override fun onAudioDecoderInitialized(
                eventTime: AnalyticsListener.EventTime,
                decoderName: String,
                initializedTimestampMs: Long,
                initializationDurationMs: Long
            ) {
                _telemetry.value = _telemetry.value.copy(
                    decoderName = decoderName,
                    isHardwareAccelerated = !decoderName.contains("google", ignoreCase = true)
                )
            }

            override fun onAudioInputFormatChanged(
                eventTime: AnalyticsListener.EventTime,
                format: Format,
                decoderReuseEvaluation: DecoderReuseEvaluation?
            ) {
                val bitDepth = when (format.pcmEncoding) {
                    C.ENCODING_PCM_24BIT -> 24
                    C.ENCODING_PCM_32BIT -> 32
                    C.ENCODING_PCM_FLOAT -> 32
                    else -> 16
                }

                _telemetry.value = _telemetry.value.copy(
                    decodedSampleRate = if (format.sampleRate != Format.NO_VALUE) format.sampleRate else _telemetry.value.decodedSampleRate,
                    decodedChannelCount = if (format.channelCount != Format.NO_VALUE) format.channelCount else _telemetry.value.decodedChannelCount,
                    decodedBitDepth = bitDepth,
                    decodedBitrateKbps = if (format.bitrate != Format.NO_VALUE) format.bitrate / 1000 else _telemetry.value.decodedBitrateKbps
                )
            }
        })
    }

    fun playTrackList(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        originalQueue = tracks.toList()
        _queue.value = tracks.toList()

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setUri(Uri.parse(track.uri))
                .setMediaId(track.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, startIndex.coerceIn(0, tracks.lastIndex), 0L)
        exoPlayer.prepare()
        exoPlayer.play()

        _currentIndex.value = startIndex
        _currentTrack.value = tracks.getOrNull(startIndex)
        _durationMs.value = tracks.getOrNull(startIndex)?.durationMs ?: 0L
    }

    fun playSingleTrack(track: Track) {
        playTrackList(listOf(track), 0)
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.prepare()
            }
            exoPlayer.play()
        }
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
        exoPlayer.seekTo(bounded)
        _currentPositionMs.value = bounded
    }

    fun seekForward(ms: Long = 10000L) {
        seekTo(_currentPositionMs.value + ms)
    }

    fun seekBack(ms: Long = 10000L) {
        seekTo(_currentPositionMs.value - ms)
    }

    fun playNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.play()
        } else if (_repeatMode.value == RepeatMode.ALL && _queue.value.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
            exoPlayer.play()
        }
    }

    fun playPrevious() {
        if (exoPlayer.currentPosition > 3000L) {
            exoPlayer.seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            exoPlayer.play()
        } else {
            exoPlayer.seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_shuffleEnabled.value
        _shuffleEnabled.value = newShuffle
        val current = _currentTrack.value

        if (newShuffle) {
            val shuffled = originalQueue.shuffled()
            val reordered = if (current != null) {
                listOf(current) + shuffled.filter { it.id != current.id }
            } else {
                shuffled
            }
            updateQueuePreservingCurrent(reordered, current)
        } else {
            updateQueuePreservingCurrent(originalQueue, current)
        }
    }

    private fun updateQueuePreservingCurrent(newList: List<Track>, current: Track?) {
        val currentPos = exoPlayer.currentPosition
        val currentPlayWhenReady = exoPlayer.playWhenReady
        _queue.value = newList
        val newIndex = if (current != null) newList.indexOfFirst { it.id == current.id }.coerceAtLeast(0) else 0

        val mediaItems = newList.map { track ->
            MediaItem.Builder()
                .setUri(Uri.parse(track.uri))
                .setMediaId(track.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, newIndex, currentPos)
        if (currentPlayWhenReady) exoPlayer.play()
        _currentIndex.value = newIndex
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        exoPlayer.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun addToQueue(track: Track) {
        val updated = _queue.value + track
        _queue.value = updated
        originalQueue = originalQueue + track

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(track.uri))
            .setMediaId(track.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        exoPlayer.addMediaItem(mediaItem)
    }

    fun playNextInQueue(track: Track) {
        val currentIdx = _currentIndex.value
        val insertIndex = (currentIdx + 1).coerceAtMost(_queue.value.size)
        val updated = _queue.value.toMutableList().apply { add(insertIndex, track) }
        _queue.value = updated

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(track.uri))
            .setMediaId(track.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        exoPlayer.addMediaItem(insertIndex, mediaItem)
    }

    fun removeFromQueue(index: Int) {
        if (index !in _queue.value.indices) return
        val updated = _queue.value.toMutableList().apply { removeAt(index) }
        _queue.value = updated
        exoPlayer.removeMediaItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in _queue.value.indices || toIndex !in _queue.value.indices) return
        val list = _queue.value.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _queue.value = list
        exoPlayer.moveMediaItem(fromIndex, toIndex)
    }

    private fun handleTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                exoPlayer.seekTo(0L)
                exoPlayer.play()
            }
            RepeatMode.ALL -> {
                if (!exoPlayer.hasNextMediaItem()) {
                    exoPlayer.seekTo(0, 0L)
                    exoPlayer.play()
                }
            }
            RepeatMode.OFF -> {
                if (!exoPlayer.hasNextMediaItem()) {
                    _isPlaying.value = false
                    stopPositionUpdates()
                }
            }
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _currentPositionMs.value = exoPlayer.currentPosition
                    val dur = exoPlayer.duration
                    if (dur > 0) _durationMs.value = dur
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    fun release() {
        stopPositionUpdates()
        exoPlayer.release()
    }
}
