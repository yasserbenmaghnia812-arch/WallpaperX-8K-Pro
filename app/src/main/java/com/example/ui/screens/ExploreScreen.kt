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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import com.example.data.model.WallpaperCategory
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismGold
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil

@Composable
fun ExploreScreen(
    categories: List<WallpaperCategory>,
    selectedCategory: String?,
    selectedResolution: String?,
    onCategoryClick: (String) -> Unit,
    onResolutionSelect: (String) -> Unit,
    onSearchClick: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resolutions = listOf("All", "8K Ultra HD", "4K", "2K", "1080p")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(2) }) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "استكشف العالم البصري" else "Explore Visual World",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "تصنيفات فائقة الجودة والدقة" else "Ultra high definition categories",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .testTag("explore_search_trigger")
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E2432))
                                .clickable {
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                    onSearchClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = PrismCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resolution Filter Pills
                    Text(
                        text = if (isArabic) "تصفية حسب دقة الشاشة" else "Filter by Resolution",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(resolutions.size) { i ->
                            val res = resolutions[i]
                            val isSelected = (selectedResolution == res) || (selectedResolution == null && res == "All")
                            Box(
                                modifier = Modifier
                                    .testTag("res_pill_$res")
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) PrismPurple else Color(0xFF1E2432))
                                    .border(1.dp, if (isSelected) PrismCyan else Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                    .clickable {
                                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                        onResolutionSelect(res)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = res,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Category Card Grid
            items(categories.size, key = { categories[it].id }) { index ->
                val cat = categories[index]
                Box(
                    modifier = Modifier
                        .testTag("category_card_${cat.id}")
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF161A24))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                        .clickable {
                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                            onCategoryClick(cat.name)
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(cat.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = cat.name,
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
                                        Color(0x22000000),
                                        Color(0xBB000000),
                                        Color(0xEE000000)
                                    )
                                )
                            )
                    )

                    if (cat.isHot) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFCC1100))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Hot",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(text = "HOT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isArabic) cat.nameAr else cat.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cat.count} ${if (isArabic) "خلفية" else "wallpapers"}",
                            color = PrismCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
