package com.example.data.worker

import android.content.Context
import com.example.data.local.WallpaperDatabase
import com.example.data.repository.WallpaperRepository
import com.example.ui.util.WallpaperManagerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AutoWallpaperManager(private val context: Context) {

    suspend fun changeWallpaperNow(): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = WallpaperDatabase.getDatabase(context)
            val repository = WallpaperRepository(database.wallpaperDao())
            
            // Get favorites or full catalog
            val favorites = repository.favoriteWallpapers.firstOrNull()
            val catalog = if (!favorites.isNullOrEmpty()) favorites else repository.allWallpapers.firstOrNull()

            if (!catalog.isNullOrEmpty()) {
                val randomWallpaper = catalog.random()
                val result = WallpaperManagerUtil.setWallpaperFromUrl(
                    context = context,
                    imageUrl = randomWallpaper.highResUrl,
                    target = WallpaperManagerUtil.WallpaperTarget.BOTH
                )
                return@withContext result.isSuccess
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
