package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun expandQueryWithAI(query: String): List<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val promptText = """
                    You are a Pinterest 8K Wallpaper Search Assistant.
                    Translate and expand the user query into 3 distinct, high quality search phrases optimized for finding 8K Pinterest wallpapers.
                    User Query: "$cleanQuery"
                    Respond ONLY with a JSON array of 3 string search terms, e.g. ["anime aesthetic 8k wallpaper", "cyberpunk neon street 4k", "dark aesthetic wallpaper"]. No markdown codeblocks or extra text.
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", promptText)
                                })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""

                if (response.isSuccessful && bodyString.isNotBlank()) {
                    val parsed = JSONObject(bodyString)
                    val text = parsed.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text", "") ?: ""

                    val cleanJson = text.replace("```json", "").replace("```", "").trim()
                    if (cleanJson.startsWith("[")) {
                        val array = JSONArray(cleanJson)
                        val list = mutableListOf<String>()
                        for (i in 0 until array.length()) {
                            val term = array.optString(i, "")
                            if (term.isNotBlank()) list.add(term)
                        }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return@withContext fallbackExpansion(cleanQuery)
    }

    private fun fallbackExpansion(query: String): List<String> {
        val q = query.lowercase().trim()
        val list = mutableListOf<String>()
        list.add(q)
        when {
            q.contains("انمي") || q.contains("أنمي") || q.contains("anime") -> {
                list.add("anime aesthetic 8k wallpaper")
                list.add("cyberpunk anime scenery 4k")
            }
            q.contains("سيارات") || q.contains("سيارة") || q.contains("car") -> {
                list.add("supercars amoled neon 8k")
                list.add("porsche ferrari hypercar 4k")
            }
            q.contains("طبيعة") || q.contains("جبال") || q.contains("nature") -> {
                list.add("nature alpine lake 8k sunset")
                list.add("kyoto bamboo forest misty 4k")
            }
            q.contains("فضاء") || q.contains("مجرة") || q.contains("space") -> {
                list.add("cosmic galaxy nebula 8k wallpaper")
                list.add("deep space amoled star eclipse")
            }
            q.contains("سوداء") || q.contains("داكن") || q.contains("dark") || q.contains("amoled") -> {
                list.add("amoled pitch black neon prism 8k")
                list.add("dark aesthetic obsidian wallpaper")
            }
            else -> {
                list.add("$query 8k aesthetic wallpaper")
                list.add("$query pinterest pin high quality")
            }
        }
        return list
    }
}
