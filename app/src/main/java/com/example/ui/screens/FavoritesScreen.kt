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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Wallpaper
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.util.HapticFeedbackUtil

enum class FavTab { FAVORITES, DOWNLOADED }

@Composable
fun FavoritesScreen(
    favoriteWallpapers: List<Wallpaper>,
    downloadedWallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper) -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(FavTab.FAVORITES) }

    val currentList = if (selectedTab == FavTab.FAVORITES) favoriteWallpapers else downloadedWallpapers

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
                    Text(
                        text = if (isArabic) "المكتبة الشخصية 💖" else "Personal Gallery 💖",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "الخلفيات المفضلة والمحملة على جهازك" else "Saved & downloaded wallpapers offline",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Segmented Toggle Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E2432))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .testTag("fav_tab_favorites")
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTab == FavTab.FAVORITES) PrismCyan else Color.Transparent)
                                .clickable {
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                    selectedTab = FavTab.FAVORITES
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = if (selectedTab == FavTab.FAVORITES) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${if (isArabic) "المفضلة" else "Favorites"} (${favoriteWallpapers.size})",
                                    color = if (selectedTab == FavTab.FAVORITES) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .testTag("fav_tab_downloaded")
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTab == FavTab.DOWNLOADED) PrismCyan else Color.Transparent)
                                .clickable {
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                    selectedTab = FavTab.DOWNLOADED
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.DownloadDone,
                                    contentDescription = null,
                                    tint = if (selectedTab == FavTab.DOWNLOADED) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${if (isArabic) "المحملة" else "Downloaded"} (${downloadedWallpapers.size})",
                                    color = if (selectedTab == FavTab.DOWNLOADED) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (currentList.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder,
                            contentDescription = "Empty",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "لا توجد خلفيات محفظوة هنا بعد" else "No saved wallpapers yet",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "اضغط على أيقونة القلب على أي خلفية لحفظها في مكتبتك الخاصة." else "Tap the heart icon on any wallpaper to add it to your collection.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(currentList, key = { it.id }) { item ->
                    WallpaperCard(
                        wallpaper = item,
                        onClick = { onWallpaperClick(item) },
                        onFavoriteClick = { onFavoriteClick(item) }
                    )
                }
            }
        }
    }
}
