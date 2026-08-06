package com.example.data.repository

import com.example.data.local.WallpaperDao
import com.example.data.local.WallpaperEntity
import com.example.data.model.ColorFilter
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperCollection
import com.example.data.remote.FirebaseWallpaperSyncService
import com.example.data.remote.GeminiSearchService
import com.example.data.remote.PinterestWallpaperService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class WallpaperRepository(
    private val wallpaperDao: WallpaperDao,
    private val pinterestService: PinterestWallpaperService = PinterestWallpaperService(),
    private val geminiSearchService: GeminiSearchService = GeminiSearchService(),
    private val firebaseSyncService: FirebaseWallpaperSyncService = FirebaseWallpaperSyncService(wallpaperDao)
) {

    val allWallpapers: Flow<List<Wallpaper>> = wallpaperDao.getAllWallpapers().map { list ->
        list.map { it.toDomain() }
    }

    val favoriteWallpapers: Flow<List<Wallpaper>> = wallpaperDao.getFavoriteWallpapers().map { list ->
        list.map { it.toDomain() }
    }

    val downloadedWallpapers: Flow<List<Wallpaper>> = wallpaperDao.getDownloadedWallpapers().map { list ->
        list.map { it.toDomain() }
    }

    fun getWallpapersByCategory(category: String): Flow<List<Wallpaper>> {
        return wallpaperDao.getWallpapersByCategory(category).map { list ->
            list.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchWallpapers(query: String): Flow<List<Wallpaper>> {
        val q = query.trim()
        if (q.isBlank()) {
            return wallpaperDao.getAllWallpapers().map { list -> list.map { it.toDomain() } }
        }
        val normalized = normalizeSearchQuery(q)
        return wallpaperDao.searchWallpapers(q).flatMapLatest { primaryList ->
            if (primaryList.isNotEmpty()) {
                flowOf(primaryList.map { it.toDomain() })
            } else {
                wallpaperDao.searchWallpapers(normalized).map { altList ->
                    altList.map { it.toDomain() }
                }
            }
        }
    }

    private fun normalizeSearchQuery(query: String): String {
        val q = query.lowercase().trim()
        return when {
            q.contains("animi") || q.contains("anime") || q.contains("manga") || q.contains("انمي") || q.contains("أنمي") -> "anime"
            q.contains("car") || q.contains("auto") || q.contains("سيارات") || q.contains("سيارة") -> "cars"
            q.contains("cyber") || q.contains("neon") || q.contains("نيون") || q.contains("سايبربانك") -> "cyberpunk"
            q.contains("nature") || q.contains("mountain") || q.contains("طبيعة") || q.contains("جبال") -> "nature"
            q.contains("space") || q.contains("galaxy") || q.contains("فضاء") || q.contains("مجرة") -> "space"
            q.contains("dark") || q.contains("black") || q.contains("amoled") || q.contains("سوداء") || q.contains("داكن") -> "amoled"
            else -> q
        }
    }

    suspend fun getWallpaperById(id: String): Wallpaper? {
        return wallpaperDao.getWallpaperById(id)?.toDomain()
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        wallpaperDao.updateFavorite(id, isFavorite)
    }

    suspend fun markAsDownloaded(id: String) {
        wallpaperDao.markAsDownloaded(id)
    }

    suspend fun insertCustomWallpaper(wallpaper: Wallpaper) {
        wallpaperDao.insert(WallpaperEntity.fromDomain(wallpaper))
        firebaseSyncService.syncNewWallpaperToCloud(wallpaper)
    }

    suspend fun seedInitialDataIfEmpty() {
        // First try pulling remote wallpapers from Firebase Cloud
        firebaseSyncService.fetchFromFirebaseAndSaveLocal()

        val initialCatalog = getInitialCatalog()
        wallpaperDao.insertAll(initialCatalog.map { WallpaperEntity.fromDomain(it) })
        
        // Sync catalog to Firebase Firestore
        firebaseSyncService.syncCatalogToFirebase(initialCatalog)

        // Fetch diverse initial Pinterest themes on app startup
        val initialQueries = listOf("anime 8k wallpaper", "cyberpunk neon city", "supercars amoled", "nature landscape 8k", "space galaxy 8k")
        for (q in initialQueries) {
            fetchPinterestWallpapers(q)
        }
    }

    suspend fun fetchPinterestWallpapers(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        val normalized = normalizeSearchQuery(q)
        try {
            val pinterestItems = pinterestService.searchPinterestWallpapers(q)
            if (pinterestItems.isNotEmpty()) {
                wallpaperDao.insertAll(pinterestItems.map { WallpaperEntity.fromDomain(it) })
            }
            if (normalized != q && normalized.isNotBlank()) {
                val normalizedItems = pinterestService.searchPinterestWallpapers(normalized)
                if (normalizedItems.isNotEmpty()) {
                    wallpaperDao.insertAll(normalizedItems.map { WallpaperEntity.fromDomain(it) })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchPinterestWallpapersWithAI(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        // First fetch regular Pinterest wallpapers
        fetchPinterestWallpapers(q)
        
        // Expand query with Gemini AI
        try {
            val aiExpandedTerms = geminiSearchService.expandQueryWithAI(q)
            for (term in aiExpandedTerms) {
                if (term.isNotBlank() && term != q) {
                    val pinterestItems = pinterestService.searchPinterestWallpapers(term)
                    if (pinterestItems.isNotEmpty()) {
                        wallpaperDao.insertAll(pinterestItems.map { WallpaperEntity.fromDomain(it) })
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCategories(): List<WallpaperCategory> {
        return listOf(
            WallpaperCategory("1", "Cyberpunk", "سايبربانك", "ic_cyberpunk", 342, "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800", isHot = true),
            WallpaperCategory("2", "Amoled / Black", "أمولة وشاشات سوداء", "ic_amoled", 580, "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=800", isHot = true),
            WallpaperCategory("3", "Nature", "الطبيعة والجمال", "ic_nature", 920, "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"),
            WallpaperCategory("4", "Abstract & 3D", "تجريدي ثلاثي الأبعاد", "ic_abstract", 740, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800", isHot = true),
            WallpaperCategory("5", "Anime & Manga", "أنمي ومانغا", "ic_anime", 810, "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800"),
            WallpaperCategory("6", "Space & Galaxy", "الفضاء والمجرات", "ic_space", 620, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800"),
            WallpaperCategory("7", "Minimalist", "تصميم بسيط", "ic_minimal", 490, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"),
            WallpaperCategory("8", "Cars & Supercars", "سيارات وسرعة", "ic_cars", 430, "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800"),
            WallpaperCategory("9", "Cities & Architecture", "مدن وعمارة", "ic_cities", 680, "https://images.unsplash.com/photo-1477959858617-67f30ac4ce78?w=800"),
            WallpaperCategory("10", "Neon Glow", "نيون متوهج", "ic_neon", 510, "https://images.unsplash.com/photo-1563089145-599997674d42?w=800")
        )
    }

    fun getCollections(): List<WallpaperCollection> {
        val catalog = getInitialCatalog()
        return listOf(
            WallpaperCollection(
                id = "c1",
                title = "Neon Nights 8K",
                titleAr = "ليالي النيون 8K",
                description = "Ultra high resolution futuristic metropolis nightscapes with iridescent glow.",
                coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=800",
                itemCount = 24,
                isPremium = false,
                wallpapers = catalog.filter { it.category == "Cyberpunk" || it.category == "Neon Glow" }
            ),
            WallpaperCollection(
                id = "c2",
                title = "Pure Amoled 0% Light",
                titleAr = "أمولة أسود خالص",
                description = "True pitch black #000000 wallpapers optimized to save battery on OLED screens.",
                coverUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=800",
                itemCount = 38,
                isPremium = false,
                wallpapers = catalog.filter { it.category == "Amoled / Black" }
            ),
            WallpaperCollection(
                id = "c3",
                title = "3D Glassmorphism Masterpieces",
                titleAr = "تحف الزجاج ثلاثي الأبعاد",
                description = "Prism light refractors, fluid wave gradients, and 8K glass elements.",
                coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                itemCount = 19,
                isPremium = true,
                wallpapers = catalog.filter { it.category == "Abstract & 3D" }
            ),
            WallpaperCollection(
                id = "c4",
                title = "Majestic Horizon 8K",
                titleAr = "أفق الطبيعة الساحر",
                description = "Breathtaking mountain ridges, aurora lights, and deep ocean reflections.",
                coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                itemCount = 42,
                isPremium = false,
                wallpapers = catalog.filter { it.category == "Nature" }
            )
        )
    }

    fun getColorFilters(): List<ColorFilter> {
        return listOf(
            ColorFilter("Cyan Neon", "#00F0FF"),
            ColorFilter("Electric Violet", "#7000FF"),
            ColorFilter("Obsidian Black", "#0A0C10"),
            ColorFilter("Cyber Gold", "#FFB800"),
            ColorFilter("Hot Pink", "#FF007A"),
            ColorFilter("Emerald Glow", "#00E676"),
            ColorFilter("Deep Blue", "#0066FF"),
            ColorFilter("Crimson Red", "#FF1744")
        )
    }

    private fun getInitialCatalog(): List<Wallpaper> {
        return listOf(
            Wallpaper(
                id = "wp_8k_1",
                title = "Prism Neon City 8K",
                description = "Futuristic metropolis skyline bathed in cyberpunk neon glow and volumetric rain reflections. Rendered in true 7680×4320 resolution.",
                category = "Cyberpunk",
                imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2400",
                resolution = "7680×4320",
                fileSize = "18.4 MB",
                dominantColors = listOf("#00F0FF", "#7000FF", "#0A0C10", "#FF007A", "#FFB800"),
                photographer = "Nexus Render Studio",
                views = 28400,
                downloads = 12900,
                likes = 8420,
                isEditorChoice = true,
                isTrending = true,
                tags = listOf("Cyberpunk", "8K", "Neon", "City", "OLED")
            ),
            Wallpaper(
                id = "wp_8k_2",
                title = "Amoled Fluid Prism Wave",
                description = "Deep pitch black background featuring iridescence liquid waves. Designed specifically for AMOLED displays to save power.",
                category = "Amoled / Black",
                imageUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=2400",
                resolution = "7680×4320",
                fileSize = "14.2 MB",
                dominantColors = listOf("#000000", "#00F0FF", "#121620", "#7000FF", "#94A3B8"),
                photographer = "OLED Lab",
                views = 34100,
                downloads = 18400,
                likes = 11200,
                isEditorChoice = true,
                isTrending = true,
                tags = listOf("Amoled", "Black", "8K", "Minimal", "Fluid")
            ),
            Wallpaper(
                id = "wp_8k_3",
                title = "Misty Alpine Peak Sunrise",
                description = "Epic high altitude mountain range glowing under warm morning sun rays with crystal clear lake reflection.",
                category = "Nature",
                imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=2400",
                resolution = "7680×4320",
                fileSize = "22.1 MB",
                dominantColors = listOf("#FFB800", "#0066FF", "#0A0C10", "#00E676", "#F0F4F8"),
                photographer = "Earth Lens Project",
                views = 19800,
                downloads = 8300,
                likes = 5400,
                isEditorChoice = false,
                isTrending = true,
                tags = listOf("Nature", "Mountains", "Sunrise", "8K", "Landscape")
            ),
            Wallpaper(
                id = "wp_8k_4",
                title = "Glassmorphism Iridescent Torus",
                description = "3D frosted glass geometry catching rainbow light spectrum rays. Ultra polished composition for modern screens.",
                category = "Abstract & 3D",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=2400",
                resolution = "7680×4320",
                fileSize = "19.6 MB",
                dominantColors = listOf("#7000FF", "#00F0FF", "#FF007A", "#0A0C10", "#FFFFFF"),
                photographer = "Visual Alchemy",
                views = 41200,
                downloads = 21000,
                likes = 14800,
                isEditorChoice = true,
                isTrending = true,
                isPremium = true,
                tags = listOf("Abstract", "3D", "Glass", "8K", "Prism")
            ),
            Wallpaper(
                id = "wp_8k_5",
                title = "Deep Space Nebula Genesis",
                description = "Captivating interstellar starbirth cloud captured in breathtaking 8K clarity with vibrant cosmic dust clusters.",
                category = "Space & Galaxy",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=2400",
                resolution = "7680×4320",
                fileSize = "25.0 MB",
                dominantColors = listOf("#0066FF", "#7000FF", "#0A0C10", "#00F0FF", "#FF007A"),
                photographer = "Cosmic Telescope Initiative",
                views = 23900,
                downloads = 10400,
                likes = 7100,
                isEditorChoice = false,
                isTrending = false,
                tags = listOf("Space", "Galaxy", "Nebula", "8K", "Stars")
            ),
            Wallpaper(
                id = "wp_8k_6",
                title = "Tokyo Neon Rain Alley",
                description = "Atmospheric night shot of a lantern-lit Tokyo alleyway after heavy rain with puddles reflecting brilliant neon signs.",
                category = "Neon Glow",
                imageUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=2400",
                resolution = "7680×4320",
                fileSize = "17.9 MB",
                dominantColors = listOf("#FF007A", "#00F0FF", "#0A0C10", "#FFB800", "#7000FF"),
                photographer = "Kaito Shutter",
                views = 31000,
                downloads = 15200,
                likes = 9800,
                isEditorChoice = true,
                isTrending = true,
                tags = listOf("Tokyo", "Neon", "Rain", "8K", "Japan")
            ),
            Wallpaper(
                id = "wp_8k_7",
                title = "Hypercar Prism Speedline",
                description = "Sleek matte black hypercar accelerating through a luminous laser tunnel with light trails.",
                category = "Cars & Supercars",
                imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=2400",
                resolution = "7680×4320",
                fileSize = "20.3 MB",
                dominantColors = listOf("#0A0C10", "#FF1744", "#00F0FF", "#94A3B8", "#000000"),
                photographer = "Velocity Optics",
                views = 18700,
                downloads = 7900,
                likes = 4900,
                isEditorChoice = false,
                isTrending = false,
                tags = listOf("Supercar", "Speed", "8K", "Hypercar", "Red")
            ),
            Wallpaper(
                id = "wp_8k_8",
                title = "Minimalist Geometric Horizon",
                description = "Clean, calming balance of subtle pastel gradients, architectural shadows, and soft ambient lighting.",
                category = "Minimalist",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=2400",
                resolution = "7680×4320",
                fileSize = "12.8 MB",
                dominantColors = listOf("#F0F4F8", "#94A3B8", "#00F0FF", "#FFB800", "#121620"),
                photographer = "Zenith Aesthetics",
                views = 15400,
                downloads = 6800,
                likes = 4100,
                isEditorChoice = false,
                isTrending = false,
                tags = listOf("Minimal", "Clean", "8K", "Pastel", "Zen")
            ),
            Wallpaper(
                id = "wp_8k_9",
                title = "Anime Cyberpunk Samurai 8K",
                description = "Aesthetic 8K anime warrior standing in neon electric rain in futuristic Neo Tokyo.",
                category = "Anime & Manga",
                imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2400",
                resolution = "7680×4320",
                fileSize = "19.8 MB",
                dominantColors = listOf("#00F0FF", "#7000FF", "#FF007A", "#0A0C10"),
                photographer = "OtakuArt Studio",
                views = 48900,
                downloads = 24500,
                likes = 19200,
                isEditorChoice = true,
                isTrending = true,
                tags = listOf("Anime", "Animi", "Manga", "Samurai", "Neon", "8K")
            ),
            Wallpaper(
                id = "wp_8k_10",
                title = "Ghibli Sunset Meadow 8K",
                description = "Serene anime countryside scenery with golden hour sunlight illuminating vast green hills.",
                category = "Anime & Manga",
                imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=2400",
                resolution = "7680×4320",
                fileSize = "17.4 MB",
                dominantColors = listOf("#FFB800", "#00E676", "#0066FF", "#FFFFFF"),
                photographer = "Studio Ghibli FanArt",
                views = 52100,
                downloads = 28900,
                likes = 21400,
                isEditorChoice = true,
                isTrending = true,
                tags = listOf("Anime", "Animi", "Ghibli", "Sunset", "Meadow", "8K")
            ),
            Wallpaper(
                id = "wp_8k_11",
                title = "Porsche 911 GT3 RS Neon 8K",
                description = "Track supercar resting under intense cyan and magenta neon light beams.",
                category = "Cars & Supercars",
                imageUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=2400",
                resolution = "7680×4320",
                fileSize = "21.5 MB",
                dominantColors = listOf("#0A0C10", "#00F0FF", "#FF007A", "#94A3B8"),
                photographer = "Stuttgart Motion",
                views = 36800,
                downloads = 17300,
                likes = 12900,
                isEditorChoice = true,
                isTrending = true,
                tags = listOf("Cars", "Supercar", "Porsche", "Neon", "8K", "Auto")
            ),
            Wallpaper(
                id = "wp_8k_12",
                title = "Emerald Kyoto Bamboo Sanctuary",
                description = "Tranquil path winding through misty tall bamboo shoots in historical Kyoto.",
                category = "Nature",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
                highResUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=2400",
                resolution = "7680×4320",
                fileSize = "18.9 MB",
                dominantColors = listOf("#00E676", "#0A0C10", "#F0F4F8", "#94A3B8"),
                photographer = "Nippon Scenery",
                views = 27400,
                downloads = 11800,
                likes = 8900,
                isEditorChoice = false,
                isTrending = true,
                tags = listOf("Nature", "Kyoto", "Bamboo", "Green", "8K")
            )
        )
    }
}
