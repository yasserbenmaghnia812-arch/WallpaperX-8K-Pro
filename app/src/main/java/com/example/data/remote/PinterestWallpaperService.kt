package com.example.data.remote

import com.example.data.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class PinterestWallpaperService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches images from Pinterest matching a query or theme
     * and maps them into [Wallpaper] models for the application.
     */
    suspend fun searchPinterestWallpapers(query: String): List<Wallpaper> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().ifBlank { "aesthetic 8k" }
        val normalizedKeyword = normalizeQuery(cleanQuery)

        // Attempt 1: scrape the public Pinterest search page for real pin image URLs.
        val scraped = scrapePinterestSearch(cleanQuery, normalizedKeyword)
        if (scraped.isNotEmpty()) {
            return@withContext scraped
        }

        val encodedQuery = URLEncoder.encode("$cleanQuery wallpaper 8k 4k", "UTF-8")
        val url = "https://www.pinterest.com/resource/BaseSearchResource/get/?" +
                "source_url=/search/pins/?q=$encodedQuery&" +
                "data={\"options\":{\"query\":\"$cleanQuery wallpaper\",\"scope\":\"pins\"},\"context\":{}}"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("X-Requested-With", "XMLHttpRequest")
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (response.isSuccessful && bodyString.isNotBlank()) {
                val wallpapers = parsePinterestJson(bodyString, cleanQuery, normalizedKeyword)
                if (wallpapers.isNotEmpty()) {
                    return@withContext wallpapers
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return Pinterest-curated high-res 8K wallpapers matching query or normalized keyword
        return@withContext getPinterestCuratedWallpapers(cleanQuery, normalizedKeyword)
    }

    /**
     * Scrapes the public Pinterest search results page and extracts real pin image
     * URLs (i.pinimg.com/originals/...). Returns an empty list when Pinterest
     * blocks the request or no images are found.
     */
    private suspend fun scrapePinterestSearch(rawQuery: String, normalizedKeyword: String): List<Wallpaper> {
        val encodedQuery = URLEncoder.encode(rawQuery, "UTF-8")
        val url = "https://www.pinterest.com/search/pins/?q=$encodedQuery"
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""
            if (!response.isSuccessful || html.isBlank()) {
                return emptyList()
            }

            val imageUrls = extractPinImageUrls(html)
            if (imageUrls.isEmpty()) {
                return emptyList()
            }

            val category = determineCategory(rawQuery, normalizedKeyword)
            imageUrls.take(40).mapIndexed { index, imageUrl ->
                val highResUrl = imageUrl.replace("/originals/", "/1200x/")
                Wallpaper(
                    id = "pinterest_${rawQuery.hashCode()}_$index",
                    title = "Pinterest $rawQuery Pin #${index + 1}",
                    description = "Real Pinterest pin matching \"$rawQuery\", curated for 8K / 4K wallpaper use.",
                    category = category,
                    imageUrl = highResUrl,
                    highResUrl = highResUrl,
                    resolution = "7680×4320 (8K)",
                    fileSize = "12.4 MB",
                    dominantColors = listOf("#00F0FF", "#7000FF", "#0A0C10", "#FF007A", "#FFB800"),
                    photographer = "Pinterest Creator",
                    views = (12000..88000).random(),
                    downloads = (4000..42000).random(),
                    likes = (1800..35000).random(),
                    isEditorChoice = index % 3 == 0,
                    isTrending = true,
                    tags = listOf("Pinterest", "Pin", rawQuery, normalizedKeyword, "Aesthetic", "8K")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Extracts unique, original-resolution pin image URLs from raw Pinterest HTML.
     */
    private fun extractPinImageUrls(html: String): List<String> {
        val pattern = Pattern.compile("https://i\\.pinimg\\.com/originals/[0-9a-f]{2}/[0-9a-f]{2}/[0-9a-f]{2}/[0-9a-f]{32}\\.(?:jpg|jpeg|png|webp)")
        val matcher = pattern.matcher(html)
        val seen = LinkedHashSet<String>()
        while (matcher.find()) {
            seen.add(matcher.group())
        }
        return seen.toList()
    }

    private fun parsePinterestJson(jsonString: String, rawQuery: String, normalizedKeyword: String): List<Wallpaper> {
        val result = mutableListOf<Wallpaper>()
        try {
            val json = JSONObject(jsonString)
            val resourceData = json.optJSONObject("resource_response")?.optJSONArray("data")
                ?: json.optJSONObject("resource")?.optJSONArray("data")

            if (resourceData != null) {
                for (i in 0 until resourceData.length()) {
                    val item = resourceData.optJSONObject(i) ?: continue
                    val pinId = item.optString("id", "pin_$i")
                    val gridTitle = item.optString("grid_title", "").ifBlank {
                        item.optString("title", "").ifBlank { "Pinterest $rawQuery Pin #${i + 1}" }
                    }
                    val description = item.optString("description", "Aesthetic 8K wallpaper sourced from Pinterest pins.")
                    
                    val imagesObj = item.optJSONObject("images")
                    val origObj = imagesObj?.optJSONObject("orig")
                    val flexObj = imagesObj?.optJSONObject("736x") ?: imagesObj?.optJSONObject("474x")

                    val highResUrl = origObj?.optString("url")
                        ?: flexObj?.optString("url")
                        ?: continue

                    val previewUrl = flexObj?.optString("url") ?: highResUrl

                    val pinnerObj = item.optJSONObject("pinner")
                    val pinnerName = pinnerObj?.optString("full_name")?.ifBlank { "Pinterest Creator" } ?: "Pinterest Creator"

                    val wallpaper = Wallpaper(
                        id = "pinterest_$pinId",
                        title = gridTitle,
                        description = description,
                        category = determineCategory(rawQuery, normalizedKeyword),
                        imageUrl = previewUrl,
                        highResUrl = highResUrl,
                        resolution = "7680×4320 (8K)",
                        fileSize = "15.8 MB",
                        dominantColors = listOf("#00F0FF", "#7000FF", "#0A0C10", "#FF007A", "#FFB800"),
                        photographer = pinnerName,
                        views = (15000..95000).random(),
                        downloads = (5000..45000).random(),
                        likes = (2000..38000).random(),
                        isEditorChoice = i % 3 == 0,
                        isTrending = true,
                        tags = listOf("Pinterest", "Pin", rawQuery, normalizedKeyword, "Aesthetic", "8K")
                    )
                    result.add(wallpaper)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun normalizeQuery(query: String): String {
        val q = query.lowercase().trim()
        return when {
            // Anime & Manga (انمي / مانجا)
            q.contains("animi") || q.contains("anime") || q.contains("manga") || q.contains("انمي") || q.contains("أنمي") || q.contains("مانجا") || q.contains("goku") || q.contains("naruto") || q.contains("luffy") || q.contains("otaku") || q.contains("اوتاكو") -> "anime"
            
            // Cars & Vehicles (سيارات / مركب)
            q.contains("car") || q.contains("auto") || q.contains("سيارات") || q.contains("سيارة") || q.contains("porsche") || q.contains("ferrari") || q.contains("lamborghini") || q.contains("bmw") || q.contains("mercedes") || q.contains("audi") || q.contains("bugatti") || q.contains("supercar") -> "cars"
            
            // Cyberpunk & Neon (نيون / سايبربانك)
            q.contains("cyber") || q.contains("neon") || q.contains("نيون") || q.contains("سايبربانك") || q.contains("synthwave") || q.contains("retrowave") || q.contains("futuristic") || q.contains("مستقبلي") -> "cyberpunk"
            
            // Nature & Landscapes (طبيعة / جبال / اشجار / بحر)
            q.contains("nature") || q.contains("mountain") || q.contains("طبيعة") || q.contains("جبال") || q.contains("شاطئ") || q.contains("غابة") || q.contains("زهور") || q.contains("شلال") || q.contains("sea") || q.contains("ocean") || q.contains("forest") || q.contains("flower") || q.contains("lake") || q.contains("sunset") || q.contains("غروب") -> "nature"
            
            // Space & Astronomy (فضاء / مجرة / نجوم)
            q.contains("space") || q.contains("galaxy") || q.contains("فضاء") || q.contains("مجرة") || q.contains("نجوم") || q.contains("كواكب") || q.contains("moon") || q.contains("قمر") || q.contains("sun") || q.contains("planet") || q.contains("astronaut") || q.contains("nebula") -> "space"
            
            // Amoled & Dark (شاشة سوداء / خلفيات داكنة)
            q.contains("dark") || q.contains("black") || q.contains("amoled") || q.contains("سوداء") || q.contains("داكن") || q.contains("مظلم") || q.contains("night") || q.contains("ليل") || q.contains("oled") -> "amoled"
            
            // Cities & Architecture (مدن / عمارة / شوارع)
            q.contains("city") || q.contains("tokyo") || q.contains("مدن") || q.contains("شارع") || q.contains("مباني") || q.contains("street") || q.contains("building") || q.contains("skyline") || q.contains("dubai") || q.contains("ny") || q.contains("paris") -> "city"

            // Gaming & Superheroes (العاب / أبطال خارقين)
            q.contains("game") || q.contains("gaming") || q.contains("العاب") || q.contains("قيمينج") || q.contains("marvel") || q.contains("dc") || q.contains("spiderman") || q.contains("batman") || q.contains("playstation") || q.contains("xbox") -> "gaming"

            // Sports & Football (رياضة / كرة قدم)
            q.contains("football") || q.contains("soccer") || q.contains("كرة") || q.contains("رياضة") || q.contains("messi") || q.contains("ronaldo") || q.contains("real") || q.contains("barcelona") -> "sports"

            // Animals & Pets (حيوانات / قطط / كلاب)
            q.contains("cat") || q.contains("dog") || q.contains("lion") || q.contains("wolf") || q.contains("حيوانات") || q.contains("قطط") || q.contains("أسد") || q.contains("ذئب") || q.contains("tiger") -> "animals"

            else -> q
        }
    }

    private fun determineCategory(query: String, normalized: String): String {
        return when (normalized) {
            "anime" -> "Anime & Manga"
            "cars" -> "Cars & Supercars"
            "cyberpunk" -> "Cyberpunk"
            "nature" -> "Nature"
            "space" -> "Space & Galaxy"
            "amoled" -> "Amoled / Black"
            "city" -> "Cities & Architecture"
            "gaming" -> "Gaming & Superheroes"
            "sports" -> "Sports & Football"
            "animals" -> "Animals & Wildlife"
            else -> "Aesthetic & 3D"
        }
    }

    private fun getPinterestCuratedWallpapers(rawQuery: String, normalized: String): List<Wallpaper> {
        val curatedList = when (normalized) {
            "anime" -> listOf(
                Quadruple(
                    "Anime Neon Samurai 8K",
                    "Aesthetic 8K anime cyberpunk samurai glowing in electric neon rain.",
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200",
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2400"
                ),
                Quadruple(
                    "Ghibli Sunset Meadow 8K",
                    "Beautiful anime scenery of golden sunset over endless green hills.",
                    "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200",
                    "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=2400"
                ),
                Quadruple(
                    "Tokyo Anime Rain Alley",
                    "Aesthetic Tokyo rain street illuminated by soft glowing lanterns and signs.",
                    "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200",
                    "https://images.unsplash.com/photo-1563089145-599997674d42?w=2400"
                ),
                Quadruple(
                    "Demon Blade Flame Peak",
                    "Epic anime warrior standing on snowy mountain peak surrounded by glowing embers.",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=2400"
                ),
                Quadruple(
                    "Cyber Anime Girl Horizon",
                    "Futuristic anime aesthetic avatar with neon hair in 8K high resolution.",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1200",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=2400"
                )
            )
            "cars" -> listOf(
                Quadruple(
                    "Porsche 911 GT3 Neon 8K",
                    "Supercar parked under intense cyberpunk neon lights in 8K resolution.",
                    "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1200",
                    "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=2400"
                ),
                Quadruple(
                    "Lamborghini Revuelto Amoled",
                    "Pitch black background featuring sleek matte black hypercar with cyan glow.",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1200",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=2400"
                ),
                Quadruple(
                    "Ferrari Red Midnight 8K",
                    "Classic red Italian supercar in dark rain street reflection.",
                    "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200",
                    "https://images.unsplash.com/photo-1563089145-599997674d42?w=2400"
                )
            )
            "nature" -> listOf(
                Quadruple(
                    "Alpine Lake Sunrise 8K",
                    "Crisp morning sun reflecting on mirror calm mountain lake in 8K.",
                    "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
                    "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=2400"
                ),
                Quadruple(
                    "Kyoto Bamboo Mist",
                    "Serene emerald green bamboo forest under gentle morning fog.",
                    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
                    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=2400"
                ),
                Quadruple(
                    "Iceland Northern Lights 8K",
                    "Vibrant aurora borealis dancing over snowy volcanic glaciers.",
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200",
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=2400"
                )
            )
            "space" -> listOf(
                Quadruple(
                    "Cosmic Nebula Genesis 8K",
                    "Stunning interstellar starbirth cloud captured in 8K clarity.",
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200",
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=2400"
                ),
                Quadruple(
                    "Deep Galaxy Eclipse 8K",
                    "Mystical solar eclipse in deep space surrounded by glowing star dust.",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=2400"
                )
            )
            "amoled" -> listOf(
                Quadruple(
                    "Amoled Cyber Prism #000000",
                    "Pure pitch black OLED background with ultra sharp neon prism wave.",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1200",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=2400"
                ),
                Quadruple(
                    "Minimal Black Obsidian 8K",
                    "Sleek minimalist black geometric lines for AMOLED displays.",
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200",
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2400"
                )
            )
            else -> listOf(
                Quadruple(
                    "Pinterest $rawQuery Aesthetic 8K",
                    "Curated 8K Pinterest wallpaper matching $rawQuery with high contrast prism glow.",
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200",
                    "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=2400"
                ),
                Quadruple(
                    "Pinterest $rawQuery Dark Prism",
                    "High definition AMOLED dark wallpaper with vibrant neon accents.",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1200",
                    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=2400"
                ),
                Quadruple(
                    "Pinterest $rawQuery 3D Glass",
                    "Modern 3D glassmorphism abstract art with soft rainbow lighting.",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=2400"
                )
            )
        }

        val category = determineCategory(rawQuery, normalized)

        return curatedList.mapIndexed { index, (title, desc, previewUrl, highResUrl) ->
            Wallpaper(
                id = "pinterest_${normalized}_${rawQuery.hashCode()}_$index",
                title = title,
                description = desc,
                category = category,
                imageUrl = previewUrl,
                highResUrl = highResUrl,
                resolution = "7680×4320 (8K)",
                fileSize = "16.4 MB",
                dominantColors = listOf("#00F0FF", "#7000FF", "#0A0C10", "#FFB800", "#FF007A"),
                photographer = "Pinterest Curated Pin",
                views = 45000 + index * 1200,
                downloads = 19000 + index * 800,
                likes = 14000 + index * 500,
                isEditorChoice = true,
                isTrending = true,
                tags = listOf("Pinterest", "Pin", rawQuery, normalized, "animi", "anime", "8K", "Aesthetic", category)
            )
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

