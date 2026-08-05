package com.example.data.model

data class Wallpaper(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String,
    val imageUrl: String,
    val highResUrl: String,
    val resolution: String = "7680×4320",
    val aspectRatio: String = "9:16",
    val fileSize: String = "16.8 MB",
    val format: String = "8K Ultra HD",
    val dominantColors: List<String> = listOf("#00F0FF", "#7000FF", "#0A0C10", "#FFB800", "#FF007A"),
    val photographer: String = "WallpaperX Studio",
    val photographerAvatar: String = "",
    val license: String = "Royalty Free (Personal & Commercial)",
    val views: Int = 14200,
    val downloads: Int = 5820,
    val likes: Int = 3410,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isPremium: Boolean = false,
    val isEditorChoice: Boolean = false,
    val isTrending: Boolean = false,
    val tags: List<String> = listOf("8K", "UltraHD", "OLED", "Abstract")
)

data class WallpaperCategory(
    val id: String,
    val name: String,
    val nameAr: String,
    val iconName: String,
    val count: Int,
    val coverUrl: String,
    val isHot: Boolean = false
)

data class WallpaperCollection(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val coverUrl: String,
    val itemCount: Int,
    val isPremium: Boolean = false,
    val wallpapers: List<Wallpaper> = emptyList()
)

data class ColorFilter(
    val name: String,
    val hex: String
)
