package com.example.data.remote

import android.util.Log
import com.example.data.local.WallpaperDao
import com.example.data.local.WallpaperEntity
import com.example.data.model.Wallpaper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseWallpaperSyncService(
    private val wallpaperDao: WallpaperDao
) {
    private val tag = "FirebaseSync"
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(tag, "Firebase Firestore unavailable: ${e.message}")
            null
        }
    }
    private val collectionName = "wallpapers"

    suspend fun syncCatalogToFirebase(wallpapers: List<Wallpaper>) {
        val firestore = db ?: return
        try {
            val batch = firestore.batch()
            for (wp in wallpapers) {
                val docRef = firestore.collection(collectionName).document(wp.id)
                val data = mapOf(
                    "id" to wp.id,
                    "title" to wp.title,
                    "description" to wp.description,
                    "category" to wp.category,
                    "imageUrl" to wp.imageUrl,
                    "highResUrl" to wp.highResUrl,
                    "resolution" to wp.resolution,
                    "aspectRatio" to wp.aspectRatio,
                    "fileSize" to wp.fileSize,
                    "format" to wp.format,
                    "dominantColors" to wp.dominantColors,
                    "photographer" to wp.photographer,
                    "license" to wp.license,
                    "views" to wp.views,
                    "downloads" to wp.downloads,
                    "likes" to wp.likes,
                    "isEditorChoice" to wp.isEditorChoice,
                    "isTrending" to wp.isTrending,
                    "isPremium" to wp.isPremium,
                    "tags" to wp.tags
                )
                batch.set(docRef, data)
            }
            batch.commit().await()
            Log.d(tag, "Successfully synced ${wallpapers.size} wallpapers to Firebase Firestore!")
        } catch (e: Throwable) {
            Log.e(tag, "Error syncing catalog to Firebase: ${e.message}")
        }
    }

    suspend fun fetchFromFirebaseAndSaveLocal() {
        val firestore = db ?: return
        try {
            val snapshot = firestore.collection(collectionName).get().await()
            val remoteEntities = mutableListOf<WallpaperEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Firebase 8K Wallpaper"
                val description = doc.getString("description") ?: ""
                val category = doc.getString("category") ?: "General"
                val imageUrl = doc.getString("imageUrl") ?: ""
                val highResUrl = doc.getString("highResUrl") ?: imageUrl
                val resolution = doc.getString("resolution") ?: "7680×4320"
                val aspectRatio = doc.getString("aspectRatio") ?: "16:9"
                val fileSize = doc.getString("fileSize") ?: "18.0 MB"
                val format = doc.getString("format") ?: "PNG"
                @Suppress("UNCHECKED_CAST")
                val colors = (doc.get("dominantColors") as? List<String>) ?: listOf("#00F0FF")
                val photographer = doc.getString("photographer") ?: "Firebase Cloud"
                val license = doc.getString("license") ?: "Free for personal use"
                val views = doc.getLong("views")?.toInt() ?: 1000
                val downloads = doc.getLong("downloads")?.toInt() ?: 500
                val likes = doc.getLong("likes")?.toInt() ?: 300
                val isEditorChoice = doc.getBoolean("isEditorChoice") ?: false
                val isTrending = doc.getBoolean("isTrending") ?: true
                val isPremium = doc.getBoolean("isPremium") ?: false
                @Suppress("UNCHECKED_CAST")
                val tags = (doc.get("tags") as? List<String>) ?: listOf("Firebase", "8K")

                if (imageUrl.isNotBlank()) {
                    val wpDomain = Wallpaper(
                        id = id,
                        title = title,
                        description = description,
                        category = category,
                        imageUrl = imageUrl,
                        highResUrl = highResUrl,
                        resolution = resolution,
                        aspectRatio = aspectRatio,
                        fileSize = fileSize,
                        format = format,
                        dominantColors = colors,
                        photographer = photographer,
                        license = license,
                        views = views,
                        downloads = downloads,
                        likes = likes,
                        isFavorite = false,
                        isDownloaded = false,
                        isPremium = isPremium,
                        isEditorChoice = isEditorChoice,
                        isTrending = isTrending,
                        tags = tags
                    )
                    remoteEntities.add(WallpaperEntity.fromDomain(wpDomain))
                }
            }
            if (remoteEntities.isNotEmpty()) {
                wallpaperDao.insertAll(remoteEntities)
                Log.d(tag, "Loaded ${remoteEntities.size} wallpapers from Firebase Firestore into local Room DB!")
            }
        } catch (e: Throwable) {
            Log.e(tag, "Firebase fetch failed (using local cache): ${e.message}")
        }
    }

    fun syncNewWallpaperToCloud(wallpaper: Wallpaper) {
        val firestore = db ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = mapOf(
                    "id" to wallpaper.id,
                    "title" to wallpaper.title,
                    "description" to wallpaper.description,
                    "category" to wallpaper.category,
                    "imageUrl" to wallpaper.imageUrl,
                    "highResUrl" to wallpaper.highResUrl,
                    "resolution" to wallpaper.resolution,
                    "aspectRatio" to wallpaper.aspectRatio,
                    "fileSize" to wallpaper.fileSize,
                    "format" to wallpaper.format,
                    "dominantColors" to wallpaper.dominantColors,
                    "photographer" to wallpaper.photographer,
                    "license" to wallpaper.license,
                    "views" to wallpaper.views,
                    "downloads" to wallpaper.downloads,
                    "likes" to wallpaper.likes,
                    "isEditorChoice" to wallpaper.isEditorChoice,
                    "isTrending" to wallpaper.isTrending,
                    "isPremium" to wallpaper.isPremium,
                    "tags" to wallpaper.tags
                )
                firestore.collection(collectionName).document(wallpaper.id).set(data).await()
                Log.d(tag, "Pushed new wallpaper ${wallpaper.id} to Firebase Firestore!")
            } catch (e: Throwable) {
                Log.e(tag, "Failed to sync wallpaper to Firebase: ${e.message}")
            }
        }
    }
}
