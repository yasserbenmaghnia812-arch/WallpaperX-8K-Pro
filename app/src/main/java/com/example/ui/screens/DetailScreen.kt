package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Wallpaper
import com.example.ui.components.DeviceFramePreview
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismGold
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil
import com.example.ui.util.WallpaperManagerUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    wallpaper: Wallpaper,
    onBackClick: () -> Unit,
    onFavoriteClick: (Wallpaper) -> Unit,
    onDownloadClick: (Wallpaper) -> Unit,
    onApplyClick: (Wallpaper, WallpaperManagerUtil.WallpaperTarget) -> Unit,
    onColorSelect: (String) -> Unit = {},
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var showApplyBottomSheet by remember { mutableStateOf(false) }
    var showDeviceMockup by remember { mutableStateOf(false) }
    var qualitySliderValue by remember { mutableFloatStateOf(1f) } // 0 = 1080p, 0.5 = 4K, 1 = 8K

    fun shareWallpaper() {
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, wallpaper.title)
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                if (isArabic)
                    "✨ خلفية 8K خارقة: '${wallpaper.title}'\n🎨 الفنان: ${wallpaper.photographer}\n🔗 رابط مباشر للتحميل: https://prism8k.app/wallpaper/${wallpaper.id}\n📸 الدقة العالية: ${wallpaper.highResUrl}"
                else
                    "✨ Stunning 8K Wallpaper: '${wallpaper.title}' by ${wallpaper.photographer}!\n🔗 Direct Link: https://prism8k.app/wallpaper/${wallpaper.id}\n📸 High-Res Image: ${wallpaper.highResUrl}"
            )
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, if (isArabic) "مشاركة الخلفية 8K 🔗" else "Share 8K Wallpaper 🔗"))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // High-res Image display with gesture zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaper.highResUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .testTag("detail_back_button")
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x80000000))
                        .clickable {
                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Deep Link Share Button
                    Box(
                        modifier = Modifier
                            .testTag("detail_share_button")
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                            .clickable {
                                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                                shareWallpaper()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    // Frame Mockup Toggle
                    Box(
                        modifier = Modifier
                            .testTag("detail_frame_preview_toggle")
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (showDeviceMockup) PrismCyan else Color(0x80000000))
                            .clickable {
                                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                                showDeviceMockup = !showDeviceMockup
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = "Device Mockup",
                            tint = if (showDeviceMockup) Color.Black else Color.White
                        )
                    }

                    // Favorite Toggle
                    Box(
                        modifier = Modifier
                            .testTag("detail_favorite_toggle")
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                            .clickable {
                                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                                onFavoriteClick(wallpaper)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (wallpaper.isFavorite) Color(0xFFFF1744) else Color.White
                        )
                    }
                }
            }
        }

        // Overlay Phone Mockup View when toggled
        if (showDeviceMockup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xEE000000))
                    .padding(top = 80.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                DeviceFramePreview(wallpaper = wallpaper)
            }
        }

        // Bottom Info Sheet & Actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE161A24),
                            Color(0xFD0A0C10)
                        )
                    )
                )
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallpaper.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${wallpaper.category} • ${wallpaper.photographer}",
                        color = PrismCyan,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrismPurple)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (qualitySliderValue > 0.6f) "8K ULTRA HD" else if (qualitySliderValue > 0.3f) "4K HD" else "1080p",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resolution Comparison Slider (1080p vs 4K vs 8K)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Quality:", color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.width(50.dp))
                Slider(
                    value = qualitySliderValue,
                    onValueChange = { qualitySliderValue = it },
                    colors = SliderDefaults.colors(thumbColor = PrismCyan, activeTrackColor = PrismCyan),
                    modifier = Modifier.weight(1f)
                )
            }

            // Dominant Palette Circles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الألوان السائدة:" else "Dominant Palette:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                wallpaper.dominantColors.forEach { hex ->
                    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Cyan }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.White, CircleShape)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(hex))
                                Toast.makeText(context, if (isArabic) "تم نسخ اللون وتصفية الخلفيات المشابهة: $hex" else "Copied color & filtering: $hex", Toast.LENGTH_SHORT).show()
                                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                onColorSelect(hex)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share button
                Box(
                    modifier = Modifier
                        .testTag("detail_bottom_share_button")
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2432))
                        .clickable {
                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                            shareWallpaper()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", tint = PrismGold)
                }

                // Download 8K button
                Box(
                    modifier = Modifier
                        .testTag("detail_download_button")
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2432))
                        .clickable {
                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                            onDownloadClick(wallpaper)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Download, contentDescription = "Download", tint = PrismCyan)
                }

                // Apply Wallpaper Button
                Button(
                    onClick = {
                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.HEAVY)
                        showApplyBottomSheet = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrismCyan),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .testTag("detail_apply_wallpaper_button")
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Wallpaper, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تطبيق الخلفية 8K ⚡" else "Apply 8K Wallpaper ⚡",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Apply Bottom Sheet Dialog options
        AnimatedVisibility(
            visible = showApplyBottomSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = Color(0xFF161A24),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrismCyan, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isArabic) "اختر مكان تطبيق الخلفية" else "Choose Apply Location",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            showApplyBottomSheet = false
                            onApplyClick(wallpaper, WallpaperManagerUtil.WallpaperTarget.HOME)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2432)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("apply_home_screen_option")
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = if (isArabic) "الشاشة الرئيسية فقط 📱" else "Home Screen Only 📱", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showApplyBottomSheet = false
                            onApplyClick(wallpaper, WallpaperManagerUtil.WallpaperTarget.LOCK)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2432)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("apply_lock_screen_option")
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = if (isArabic) "شاشة القفل فقط 🔒" else "Lock Screen Only 🔒", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showApplyBottomSheet = false
                            onApplyClick(wallpaper, WallpaperManagerUtil.WallpaperTarget.BOTH)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrismCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("apply_both_screens_option")
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = if (isArabic) "الشاشتين معاً 🌟" else "Both Screens 🌟", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
