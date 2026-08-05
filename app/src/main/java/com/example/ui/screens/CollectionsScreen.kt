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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCollection
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismGold
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil

@Composable
fun CollectionsScreen(
    collections: List<WallpaperCollection>,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteClick: (Wallpaper) -> Unit,
    onDownloadPackClick: (WallpaperCollection) -> Unit = {},
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column {
                    Text(
                        text = if (isArabic) "المجموعات المنسقة 🎨" else "Curated Collections 🎨",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "ألبومات فائقة الجودة تم اختيارها بعناية" else "Handpicked theme albums for your device",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            items(collections, key = { it.id }) { collection ->
                Column(
                    modifier = Modifier
                        .testTag("collection_section_${collection.id}")
                        .fillMaxWidth()
                ) {
                    // Album Header Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF161A24))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(collection.coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = collection.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x33000000),
                                            Color(0xDD000000)
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xAA1E2432))
                                .border(1.dp, PrismCyan, RoundedCornerShape(12.dp))
                                .clickable {
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                                    onDownloadPackClick(collection)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Download Pack",
                                    tint = PrismCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "تحميل الحزمة 📦" else "Download Pack 📦",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isArabic) collection.titleAr else collection.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (collection.isPremium) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.WorkspacePremium,
                                        contentDescription = "Premium",
                                        tint = PrismGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = collection.description,
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Recycler of Album Items
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(collection.wallpapers) { wp ->
                            Box(modifier = Modifier.width(160.dp)) {
                                WallpaperCard(
                                    wallpaper = wp,
                                    onClick = { onWallpaperClick(wp) },
                                    onFavoriteClick = { onFavoriteClick(wp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
