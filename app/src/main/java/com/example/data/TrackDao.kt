package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT 30")
    fun getRecentlyPlayedFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE playCount > 0 ORDER BY playCount DESC LIMIT 30")
    fun getMostPlayedFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE quality = 'HI_RES' ORDER BY sampleRate DESC, title ASC")
    fun getHiResTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE quality = 'CD_QUALITY' ORDER BY title ASC")
    fun getCdQualityTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE quality IN ('HI_RES', 'CD_QUALITY', 'LOSSLESS') ORDER BY title ASC")
    fun getAllLosslessTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT DISTINCT album FROM tracks WHERE album != '' ORDER BY album ASC")
    fun getDistinctAlbumsFlow(): Flow<List<String>>

    @Query("SELECT DISTINCT artist FROM tracks WHERE artist != '' ORDER BY artist ASC")
    fun getDistinctArtistsFlow(): Flow<List<String>>

    @Query("SELECT DISTINCT folderPath FROM tracks WHERE folderPath != '' ORDER BY folderPath ASC")
    fun getDistinctFoldersFlow(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY title ASC")
    fun getTracksByAlbumFlow(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY title ASC")
    fun getTracksByArtistFlow(artist: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE folderPath = :folderPath ORDER BY title ASC")
    fun getTracksByFolderFlow(folderPath: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchTracksFlow(query: String): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :trackId")
    suspend fun setFavorite(trackId: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :trackId")
    suspend fun recordPlayback(trackId: String, timestamp: Long)

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int
}
