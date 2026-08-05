package com.example.ui.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperManagerUtil {

    enum class WallpaperTarget {
        HOME, LOCK, BOTH
    }

    suspend fun setWallpaperFromUrl(
        context: Context,
        imageUrl: String,
        target: WallpaperTarget
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Hardware bitmaps cannot be converted to Bitmap easily for WallpaperManager
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                if (drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    val wallpaperManager = WallpaperManager.getInstance(context)

                    val flag = when (target) {
                        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                        WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, flag)
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                    return@withContext Result.success(true)
                }
            }
            Result.failure(Exception("Could not load image bitmap"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
