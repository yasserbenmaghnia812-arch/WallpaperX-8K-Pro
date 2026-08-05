package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Wallpaper
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: List<Wallpaper>,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper) -> Unit,
    searchHistory: List<String> = emptyList(),
    onRemoveHistoryItem: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},
    isAiSearching: Boolean = false,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val trendingSearches = listOf("Cyberpunk", "OLED Black", "8K Nature", "3D Glass", "Anime", "Tokyo", "Nebula", "Neon")
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.SUCCESS)
            onQueryChange("cyberpunk neon 8k wallpaper")
        }
    }

    val aiPrompts = if (isArabic) listOf(
        "🤖 أنمي نيون سايبر 8K",
        "⚡ سيارات سوبركار أوموليد",
        "🌌 فضاء ومجرات عميقة",
        "🖤 خلفيات سوداء OLED",
        "🌿 طبيعة وشلالات خلابة"
    ) else listOf(
        "🤖 Anime Cyberpunk 8K",
        "⚡ Supercars Amoled Neon",
        "🌌 Deep Space Galaxy",
        "🖤 Amoled Obsidian Black",
        "🌿 Scenic Nature Sunrise"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                onBackClick()
                            },
                            modifier = Modifier.testTag("search_back_button")
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            placeholder = {
                                Text(
                                    text = if (isArabic) "بحث بالذكاء الاصطناعي أو كلمات مفتاحية..." else "Search with AI or keywords...",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear", tint = Color.White)
                                    }
                                } else {
                                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = PrismCyan)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF161A24),
                                unfocusedContainerColor = Color(0xFF161A24),
                                focusedBorderColor = PrismCyan,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .testTag("search_text_input")
                                .weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Search by Image button
                        Box(
                            modifier = Modifier
                                .testTag("search_by_image_button")
                                .size(50.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1E2436))
                                .border(1.dp, PrismCyan, RoundedCornerShape(18.dp))
                                .clickable {
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                                    photoPickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddPhotoAlternate,
                                contentDescription = "Search by Image",
                                tint = PrismCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Searching Active Banner
                    AnimatedVisibility(
                        visible = isAiSearching,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1E1035), Color(0xFF0F2B3E))
                                    )
                                )
                                .border(1.dp, PrismCyan, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PrismCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "🤖 جاري تحليل البحث وجلب أحدث صور Pinterest عبر الذكاء الاصطناعي..."
                                    else "🤖 Analyzing query & fetching Pinterest 8K wallpapers via AI...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // AI Prompt Presets Banner
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141824))
                            .border(1.dp, Color(0x2200F0FF), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "AI",
                                tint = PrismCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "بحث ذكي مع اقتراحات Gemini AI ✨" else "Smart AI Suggestions ✨",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            aiPrompts.forEach { prompt ->
                                val cleanTerm = prompt.substringAfter(" ").trim()
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF1E2436))
                                        .border(1.dp, Color(0x447000FF), RoundedCornerShape(14.dp))
                                        .clickable {
                                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                            onQueryChange(cleanTerm)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = prompt, color = Color(0xFFE2E8F0), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Smart Palette Matching Color Spectrum
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141824))
                            .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = "Palette",
                                tint = PrismPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "مطابقة الألوان الذكية (Smart Palette) 🎨" else "Smart Palette Matching 🎨",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val colorFilters = listOf(
                                "Cyan" to "#00F0FF",
                                "Neon Pink" to "#FF007A",
                                "Electric Purple" to "#7000FF",
                                "Cyber Gold" to "#FFB800",
                                "Emerald" to "#00E676",
                                "Amoled Black" to "#0A0C10"
                            )
                            colorFilters.forEach { (name, hex) ->
                                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Cyan }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                        onQueryChange(name.lowercase())
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = name, color = Color(0xFF94A3B8), fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    if (searchQuery.isBlank()) {
                        if (searchHistory.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "سجل البحث 🕒" else "Recent Searches 🕒",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isArabic) "مسح الكل" else "Clear All",
                                    color = Color(0xFFFF4757),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable {
                                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                            onClearHistory()
                                        }
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                searchHistory.forEach { historyItem ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFF1E2432))
                                            .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(16.dp))
                                            .clickable {
                                                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                                onQueryChange(historyItem)
                                            }
                                            .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = historyItem,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                                    onRemoveHistoryItem(historyItem)
                                                }
                                                .padding(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Delete",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = if (isArabic) "الأكثر بحثاً اليوم 🔥" else "Trending Searches Today 🔥",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            trendingSearches.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E2432))
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                        .clickable {
                                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                            onQueryChange(tag)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "# $tag", color = PrismCyan, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Text(
                            text = "${searchResults.size} ${if (isArabic) "نتيجة بحث لـ" else "results for"} \"$searchQuery\"",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            items(searchResults, key = { it.id }) { item ->
                WallpaperCard(
                    wallpaper = item,
                    onClick = { onWallpaperClick(item) },
                    onFavoriteClick = { onFavoriteClick(item) }
                )
            }
        }
    }
}
