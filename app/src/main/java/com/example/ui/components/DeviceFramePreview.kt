package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Wallpaper
import com.example.ui.theme.PrismCyan
import com.example.ui.util.HapticFeedbackUtil

enum class PreviewMode {
    HOME, LOCK
}

@Composable
fun DeviceFramePreview(
    wallpaper: Wallpaper,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(PreviewMode.LOCK) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E2432))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .testTag("preview_tab_lock")
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (mode == PreviewMode.LOCK) PrismCyan else Color.Transparent)
                    .clickable {
                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                        mode = PreviewMode.LOCK
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🔒 شاشة القفل",
                    color = if (mode == PreviewMode.LOCK) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .testTag("preview_tab_home")
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (mode == PreviewMode.HOME) PrismCyan else Color.Transparent)
                    .clickable {
                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                        mode = PreviewMode.HOME
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "📱 الشاشة الرئيسية",
                    color = if (mode == PreviewMode.HOME) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phone Frame Container
        Box(
            modifier = Modifier
                .width(260.dp)
                .aspectRatio(9f / 19.5f)
                .clip(RoundedCornerShape(38.dp))
                .border(4.dp, Color(0xFF334155), RoundedCornerShape(38.dp))
                .background(Color.Black)
        ) {
            // Wallpaper Background
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaper.highResUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Preview Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Notch Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .width(80.dp)
                    .height(20.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )

            // Status bar time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "10:42",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = "Wifi",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Lock Screen UI Overlay
            if (mode == PreviewMode.LOCK) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الأربعاء، ٥ أغسطس",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "10:42",
                            color = Color.White,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Light
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Lock",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Camera,
                                contentDescription = "Camera",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Home Screen UI Overlay (Mock App Grid)
            if (mode == PreviewMode.HOME) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 40.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Search Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x66000000))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WallpaperX 8K",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Bottom Dock Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0x44000000))
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val dockIcons = listOf(Icons.Filled.Phone, Icons.Filled.Camera, Icons.Filled.Settings)
                        dockIcons.forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x80FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Home gesture indicator bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .width(90.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    }
}
