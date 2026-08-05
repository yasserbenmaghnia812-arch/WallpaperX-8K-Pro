package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ColorFilter
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.ui.components.CategoryChip
import com.example.ui.components.ColorFilterWheel
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismGold
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil

@Composable
fun HomeScreen(
    wallpapers: List<Wallpaper>,
    categories: List<WallpaperCategory>,
    colorFilters: List<ColorFilter>,
    selectedCategory: String?,
    selectedColorHex: String?,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onColorSelect: (String?) -> Unit,
    onSearchClick: () -> Unit,
    onAutoWallpaperTrigger: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val heroWallpaper = wallpapers.firstOrNull { it.isEditorChoice } ?: wallpapers.firstOrNull()

    val filteredWallpapers = wallpapers.filter { wallpaper ->
        val categoryMatch = selectedCategory == null || wallpaper.category == selectedCategory
        val colorMatch = selectedColorHex == null || wallpaper.dominantColors.contains(selectedColorHex)
        categoryMatch && colorMatch
    }

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
            // Header: Top Bar with Logo & Search
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(PrismPurple, PrismCyan)
                                        )
                                    )
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "W",
                                        color = PrismCyan,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "WallpaperX",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "8K ULTRA HD PRO",
                                    color = PrismGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row {
                            // Quick Auto Wallpaper change trigger
                            Box(
                                modifier = Modifier
                                    .testTag("auto_wallpaper_trigger_button")
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2432))
                                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                                    .clickable {
                                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                                        onAutoWallpaperTrigger()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Auto Wallpaper",
                                    tint = PrismCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .testTag("search_trigger_button")
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2432))
                                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                                    .clickable {
                                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                        onSearchClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hero Banner: 8K Spotlight of the Day
                    heroWallpaper?.let { hero ->
                        Box(
                            modifier = Modifier
                                .testTag("hero_spotlight_card")
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color(0xFF161A24))
                                .border(
                                    1.dp,
                                    Brush.horizontalGradient(listOf(PrismCyan, PrismPurple)),
                                    RoundedCornerShape(26.dp)
                                )
                                .clickable {
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                    onWallpaperClick(hero)
                                }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(hero.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = hero.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0x99000000),
                                                Color(0xEE000000)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = PrismGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "خلفية اليوم المختارة 8K" else "DAILY 8K SPOTLIGHT",
                                        color = PrismGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = hero.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = hero.description,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Chips Bar
                    Text(
                        text = if (isArabic) "التصنيفات الرئيسية" else "Categories",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            CategoryChip(
                                category = cat,
                                isSelected = selectedCategory == cat.name,
                                onSelect = { onCategorySelect(cat.name) },
                                isArabic = isArabic
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Color Filter Wheel
                    Text(
                        text = if (isArabic) "البحث حسب اللون السائد 🎨" else "Filter by Dominant Color 🎨",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ColorFilterWheel(
                        colorFilters = colorFilters,
                        selectedColorHex = selectedColorHex,
                        onSelectColor = onColorSelect
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Section Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "لك خصيصاً (خلفيات 8K)" else "For You (8K Feed)",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${filteredWallpapers.size} ${if (isArabic) "خلفية" else "items"}",
                            color = PrismCyan,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Wallpaper Staggered Grid Items
            items(filteredWallpapers, key = { it.id }) { item ->
                WallpaperCard(
                    wallpaper = item,
                    onClick = { onWallpaperClick(item) },
                    onFavoriteClick = { onFavoriteClick(item) }
                )
            }
        }
    }
}
