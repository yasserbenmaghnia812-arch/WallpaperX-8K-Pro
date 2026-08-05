package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY id DESC")
    fun getAllWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1")
    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isDownloaded = 1")
    fun getDownloadedWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE category = :category")
    fun getWallpapersByCategory(category: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: String): WallpaperEntity?

    @Query("""
        SELECT * FROM wallpapers 
        WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' 
           OR LOWER(category) LIKE '%' || LOWER(:query) || '%' 
           OR LOWER(tagsString) LIKE '%' || LOWER(:query) || '%' 
           OR LOWER(description) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(photographer) LIKE '%' || LOWER(:query) || '%'
        ORDER BY isTrending DESC, likes DESC
    """)
    fun searchWallpapers(query: String): Flow<List<WallpaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallpapers: List<WallpaperEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallpaper: WallpaperEntity)

    @Update
    suspend fun update(wallpaper: WallpaperEntity)

    @Query("UPDATE wallpapers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE wallpapers SET isDownloaded = 1 WHERE id = :id")
    suspend fun markAsDownloaded(id: String)
}
