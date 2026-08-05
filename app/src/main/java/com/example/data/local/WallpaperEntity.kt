package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Wallpaper

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val highResUrl: String,
    val resolution: String,
    val aspectRatio: String,
    val fileSize: String,
    val format: String,
    val dominantColorsString: String, // Comma-separated hex values
    val photographer: String,
    val license: String,
    val views: Int,
    val downloads: Int,
    val likes: Int,
    val isFavorite: Boolean,
    val isDownloaded: Boolean,
    val isPremium: Boolean,
    val isEditorChoice: Boolean,
    val isTrending: Boolean,
    val tagsString: String // Comma-separated tags
) {
    fun toDomain(): Wallpaper {
        return Wallpaper(
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
            dominantColors = dominantColorsString.split(",").filter { it.isNotBlank() },
            photographer = photographer,
            license = license,
            views = views,
            downloads = downloads,
            likes = likes,
            isFavorite = isFavorite,
            isDownloaded = isDownloaded,
            isPremium = isPremium,
            isEditorChoice = isEditorChoice,
            isTrending = isTrending,
            tags = tagsString.split(",").filter { it.isNotBlank() }
        )
    }

    companion object {
        fun fromDomain(wallpaper: Wallpaper): WallpaperEntity {
            return WallpaperEntity(
                id = wallpaper.id,
                title = wallpaper.title,
                description = wallpaper.description,
                category = wallpaper.category,
                imageUrl = wallpaper.imageUrl,
                highResUrl = wallpaper.highResUrl,
                resolution = wallpaper.resolution,
                aspectRatio = wallpaper.aspectRatio,
                fileSize = wallpaper.fileSize,
                format = wallpaper.format,
                dominantColorsString = wallpaper.dominantColors.joinToString(","),
                photographer = wallpaper.photographer,
                license = wallpaper.license,
                views = wallpaper.views,
                downloads = wallpaper.downloads,
                likes = wallpaper.likes,
                isFavorite = wallpaper.isFavorite,
                isDownloaded = wallpaper.isDownloaded,
                isPremium = wallpaper.isPremium,
                isEditorChoice = wallpaper.isEditorChoice,
                isTrending = wallpaper.isTrending,
                tagsString = wallpaper.tags.joinToString(",")
            )
        }
    }
}
