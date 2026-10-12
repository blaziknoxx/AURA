package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.DocumentsContract
import android.util.Log
import com.example.audio.MetadataExtractor
import com.example.audio.ReferenceAudioGenerator
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AuraRepository(private val context: Context, private val database: AuraDatabase) {

    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()

    val allTracks: Flow<List<Track>> = trackDao.getAllTracksFlow()
        .map { entities -> entities.map { it.toTrack() } }
        .flowOn(Dispatchers.IO)

    val favoriteTracks: Flow<List<Track>> = trackDao.getFavoriteTracksFlow()
        .map { entities -> entities.map { it.toTrack() } }
        .flowOn(Dispatchers.IO)

    val recentlyPlayed: Flow<List<Track>> = trackDao.getRecentlyPlayedFlow()
        .map { entities -> entities.map { it.toTrack() } }
        .flowOn(Dispatchers.IO)

    val mostPlayed: Flow<List<Track>> = trackDao.getMostPlayedFlow()
        .map { entities -> entities.map { it.toTrack() } }
        .flowOn(Dispatchers.IO)

    val hiResTracks: Flow<List<Track>> = trackDao.getHiResTracksFlow()
        .map { entities -> entities.map { it.toTrack() } }
        .flowOn(Dispatchers.IO)

    val cdQualityTracks: Flow<List<Track>> = trackDao.getCdQualityTracksFlow()
        .map { entities -> entities.map { it.toTrack() } }
        .flowOn(Dispatchers.IO)

    val allLosslessTracks: Flow<List<Track>> = trackDao.getAllLosslessTracksFlow()
        .map { entities -> entities.map { it.toTrack() } }
        .flowOn(Dispatchers.IO)

    val distinctAlbums: Flow<List<String>> = trackDao.getDistinctAlbumsFlow().flowOn(Dispatchers.IO)
    val distinctArtists: Flow<List<String>> = trackDao.getDistinctArtistsFlow().flowOn(Dispatchers.IO)
    val distinctFolders: Flow<List<String>> = trackDao.getDistinctFoldersFlow().flowOn(Dispatchers.IO)

    val playlists: Flow<List<Playlist>> = playlistDao.getAllPlaylistsFlow()
        .map { entities ->
            entities.map { entity ->
                val count = playlistDao.getTrackCountForPlaylist(entity.id)
                entity.toPlaylist(count)
            }
        }.flowOn(Dispatchers.IO)

    suspend fun getTracksByAlbum(album: String): Flow<List<Track>> {
        return trackDao.getTracksByAlbumFlow(album).map { list -> list.map { it.toTrack() } }
    }

    suspend fun getTracksByArtist(artist: String): Flow<List<Track>> {
        return trackDao.getTracksByArtistFlow(artist).map { list -> list.map { it.toTrack() } }
    }

    suspend fun getTracksByFolder(folder: String): Flow<List<Track>> {
        return trackDao.getTracksByFolderFlow(folder).map { list -> list.map { it.toTrack() } }
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>> {
        return playlistDao.getTracksForPlaylistFlow(playlistId).map { list -> list.map { it.toTrack() } }
    }

    fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracksFlow(query).map { list -> list.map { it.toTrack() } }
    }

    suspend fun initializeLibraryIfNeeded() = withContext(Dispatchers.IO) {
        val count = trackDao.getTrackCount()
        if (count == 0) {
            val referenceTracks = ReferenceAudioGenerator.ensureReferenceTracksExist(context)
            if (referenceTracks.isNotEmpty()) {
                val entities = referenceTracks.map { TrackEntity.fromTrack(it) }
                trackDao.insertTracks(entities)

                // Create initial default playlist
                val defaultPlaylist = PlaylistEntity(
                    id = "pl_audiophile_showcase",
                    name = "Audiophile Showcase",
                    description = "Reference Hi-Res and CD Quality Master Tracks"
                )
                playlistDao.insertPlaylist(defaultPlaylist)
                referenceTracks.forEachIndexed { index, track ->
                    playlistDao.addTrackToPlaylist(
                        PlaylistTrackCrossRef(
                            playlistId = defaultPlaylist.id,
                            trackId = track.id,
                            sortOrder = index
                        )
                    )
                }
            }
        }
    }

    suspend fun toggleFavorite(trackId: String) = withContext(Dispatchers.IO) {
        val track = trackDao.getTrackById(trackId) ?: return@withContext
        trackDao.setFavorite(trackId, !track.isFavorite)
    }

    suspend fun recordPlayback(trackId: String) = withContext(Dispatchers.IO) {
        trackDao.recordPlayback(trackId, System.currentTimeMillis())
    }

    suspend fun createPlaylist(name: String, description: String = ""): String = withContext(Dispatchers.IO) {
        val id = "pl_${UUID.randomUUID()}"
        val playlist = PlaylistEntity(id = id, name = name, description = description)
        playlistDao.insertPlaylist(playlist)
        id
    }

    suspend fun renamePlaylist(playlistId: String, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(playlistId, newName)
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistAndTracks(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        val count = playlistDao.getTrackCountForPlaylist(playlistId)
        playlistDao.addTrackToPlaylist(
            PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId, sortOrder = count)
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun reorderTrackInPlaylist(playlistId: String, trackId: String, newOrder: Int) = withContext(Dispatchers.IO) {
        playlistDao.updateTrackOrder(playlistId, trackId, newOrder)
    }

    /**
     * Imports files selected via Storage Access Framework (SAF) document picker.
     */
    suspend fun importTracksFromUris(uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        var importedCount = 0
        for (uri in uris) {
            try {
                // Query file name and size
                var displayName: String? = null
                var fileSize = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) displayName = cursor.getString(nameIndex)
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                val metadata = MetadataExtractor.extract(context, uri, displayName, fileSize)
                val track = Track(
                    id = "imp_${UUID.randomUUID()}",
                    uri = uri.toString(),
                    title = metadata.title,
                    artist = metadata.artist,
                    album = metadata.album,
                    durationMs = metadata.durationMs,
                    sizeBytes = fileSize,
                    folderPath = "Imported Storage",
                    formatInfo = metadata.formatInfo,
                    albumArtUri = metadata.albumArtUri,
                    isFavorite = false,
                    playCount = 0,
                    lastPlayedTimestamp = 0L,
                    addedTimestamp = System.currentTimeMillis()
                )

                trackDao.insertTrack(TrackEntity.fromTrack(track))
                importedCount++
            } catch (e: Exception) {
                Log.e("AuraRepository", "Error importing URI $uri: ${e.message}")
            }
        }
        importedCount
    }

    /**
     * Scans MediaStore audio library on device.
     */
    suspend fun scanMediaStoreAudio(): Int = withContext(Dispatchers.IO) {
        var count = 0
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val name = cursor.getString(nameCol) ?: "Track $id"
                    val duration = cursor.getLong(durCol)
                    val size = cursor.getLong(sizeCol)
                    val dataPath = if (dataCol != -1) cursor.getString(dataCol) else ""
                    val folder = if (dataPath.isNotBlank()) File(dataPath).parent ?: "Device Audio" else "Device Audio"

                    val meta = MetadataExtractor.extract(context, contentUri, name, size)
                    val track = Track(
                        id = "media_$id",
                        uri = contentUri.toString(),
                        title = meta.title,
                        artist = meta.artist,
                        album = meta.album,
                        durationMs = if (meta.durationMs > 0) meta.durationMs else duration,
                        sizeBytes = size,
                        folderPath = folder,
                        formatInfo = meta.formatInfo,
                        albumArtUri = meta.albumArtUri,
                        isFavorite = false,
                        playCount = 0,
                        lastPlayedTimestamp = 0L,
                        addedTimestamp = System.currentTimeMillis()
                    )

                    trackDao.insertTrack(TrackEntity.fromTrack(track))
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error querying MediaStore: ${e.message}")
        }

        count
    }

    /**
     * Recursively imports audio files from a user-selected SAF folder.
     */
    suspend fun importFromDocumentTree(treeUri: Uri): Int = withContext(Dispatchers.IO) {
        val urisToImport = mutableListOf<Uri>()
        try {
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val childId = cursor.getString(idCol)
                    val name = cursor.getString(nameCol)?.lowercase() ?: ""
                    val ext = name.substringAfterLast('.', "")
                    if (ext in listOf("flac", "wav", "alac", "mp3", "m4a", "aac", "ogg", "opus")) {
                        val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                        urisToImport.add(fileUri)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AuraRepository", "Error querying tree: ${e.message}")
        }
        importTracksFromUris(urisToImport)
    }
}
