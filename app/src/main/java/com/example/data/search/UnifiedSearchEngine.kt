package com.example.data.search

import com.example.data.local.WallpaperDao
import com.example.data.local.WallpaperEntity
import com.example.data.model.Wallpaper
import com.example.data.remote.GeminiSearchService
import com.example.data.remote.PinterestWallpaperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Unified (merged) search engine.
 *
 * Combines results from multiple independent sources into a single, de-duplicated,
 * ranked list of wallpapers:
 *
 *  1. Local Room database — instant, offline results already cached on the device.
 *  2. Pinterest live search — fresh remote images fetched from Pinterest.
 *  3. Gemini AI query expansion — broadens the user's query into related search terms
 *     so more relevant wallpapers are discovered from Pinterest.
 *
 * Every source is isolated with its own error handling so a failure in one provider
 * (for example an offline network or a missing Gemini API key) never breaks the
 * overall search.
 */
class UnifiedSearchEngine(
    private val wallpaperDao: WallpaperDao,
    private val pinterestService: PinterestWallpaperService = PinterestWallpaperService(),
    private val geminiSearchService: GeminiSearchService = GeminiSearchService()
) {

    /**
     * Searches the local database only. Fast and always available offline.
     */
    suspend fun searchLocal(query: String): List<Wallpaper> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        return wallpaperDao.searchWallpapers(q).first().map { it.toDomain() }
    }

    /**
     * Searches remote providers (Pinterest + AI expansion) and returns merged,
     * de-duplicated results WITHOUT persisting them.
     */
    suspend fun searchRemote(query: String): List<Wallpaper> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext emptyList()

        // Linked map keeps insertion order while de-duplicating by wallpaper id.
        val results = linkedMapOf<String, Wallpaper>()

        // 1. Direct Pinterest search using the raw query.
        runCatching { pinterestService.searchPinterestWallpapers(q) }
            .getOrDefault(emptyList())
            .forEach { results[it.id] = it }

        // 2. Pinterest search using the normalized keyword.
        val normalized = normalizeQuery(q)
        if (normalized.isNotBlank() && normalized != q) {
            runCatching { pinterestService.searchPinterestWallpapers(normalized) }
                .getOrDefault(emptyList())
                .forEach { results[it.id] = it }
        }

        // 3. Gemini AI query expansion -> fetch more Pinterest images per term.
        runCatching { geminiSearchService.expandQueryWithAI(q) }
            .getOrDefault(emptyList())
            .filter { it.isNotBlank() && !it.equals(q, ignoreCase = true) }
            .forEach { term ->
                runCatching { pinterestService.searchPinterestWallpapers(term) }
                    .getOrDefault(emptyList())
                    .forEach { results[it.id] = it }
            }

        results.values.toList()
    }

    /**
     * Runs the unified remote search and persists the results into the local
     * database so they become instantly available for subsequent offline searches.
     */
    suspend fun searchAndPersist(query: String): List<Wallpaper> {
        val remote = searchRemote(query)
        if (remote.isNotEmpty()) {
            runCatching { wallpaperDao.insertAll(remote.map { WallpaperEntity.fromDomain(it) }) }
        }
        return remote
    }

    companion object {
        /**
         * Maps a free-text query (English or Arabic) to a canonical category keyword
         * so Pinterest fetches can target the right visual theme.
         */
        fun normalizeQuery(query: String): String {
            val q = query.lowercase().trim()
            return when {
                q.contains("animi") || q.contains("anime") || q.contains("manga") ||
                    q.contains("انمي") || q.contains("أنمي") || q.contains("مانجا") ||
                    q.contains("goku") || q.contains("naruto") || q.contains("luffy") ||
                    q.contains("otaku") || q.contains("اوتاكو") -> "anime"

                q.contains("car") || q.contains("auto") || q.contains("سيارات") ||
                    q.contains("سيارة") || q.contains("porsche") || q.contains("ferrari") ||
                    q.contains("lamborghini") || q.contains("bmw") || q.contains("mercedes") ||
                    q.contains("audi") || q.contains("bugatti") || q.contains("supercar") -> "cars"

                q.contains("cyber") || q.contains("neon") || q.contains("نيون") ||
                    q.contains("سايبربانك") || q.contains("synthwave") || q.contains("retrowave") ||
                    q.contains("futuristic") || q.contains("مستقبلي") -> "cyberpunk"

                q.contains("nature") || q.contains("mountain") || q.contains("طبيعة") ||
                    q.contains("جبال") || q.contains("شاطئ") || q.contains("غابة") ||
                    q.contains("زهور") || q.contains("شلال") || q.contains("sea") ||
                    q.contains("ocean") || q.contains("forest") || q.contains("flower") ||
                    q.contains("lake") || q.contains("sunset") || q.contains("غروب") -> "nature"

                q.contains("space") || q.contains("galaxy") || q.contains("فضاء") ||
                    q.contains("مجرة") || q.contains("نجوم") || q.contains("كواكب") ||
                    q.contains("moon") || q.contains("قمر") || q.contains("sun") ||
                    q.contains("planet") || q.contains("astronaut") || q.contains("nebula") -> "space"

                q.contains("dark") || q.contains("black") || q.contains("amoled") ||
                    q.contains("سوداء") || q.contains("داكن") || q.contains("مظلم") ||
                    q.contains("night") || q.contains("ليل") || q.contains("oled") -> "amoled"

                q.contains("city") || q.contains("tokyo") || q.contains("مدن") ||
                    q.contains("شارع") || q.contains("مباني") || q.contains("street") ||
                    q.contains("building") || q.contains("skyline") || q.contains("dubai") ||
                    q.contains("ny") || q.contains("paris") -> "city"

                q.contains("game") || q.contains("gaming") || q.contains("العاب") ||
                    q.contains("قيمينج") || q.contains("marvel") || q.contains("dc") ||
                    q.contains("spiderman") || q.contains("batman") ||
                    q.contains("playstation") || q.contains("xbox") -> "gaming"

                q.contains("football") || q.contains("soccer") || q.contains("كرة") ||
                    q.contains("رياضة") || q.contains("messi") || q.contains("ronaldo") ||
                    q.contains("real") || q.contains("barcelona") -> "sports"

                q.contains("cat") || q.contains("dog") || q.contains("lion") ||
                    q.contains("wolf") || q.contains("حيوانات") || q.contains("قطط") ||
                    q.contains("أسد") || q.contains("ذئب") || q.contains("tiger") -> "animals"

                else -> q
            }
        }
    }
}
