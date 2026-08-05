package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismGold
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil
import com.example.viewmodel.UserSettingsState

@Composable
fun ProfileScreen(
    userSettings: UserSettingsState,
    onToggleDarkMode: (Boolean) -> Unit,
    onSetLanguage: (String) -> Unit,
    onToggleAutoWallpaper: (Boolean) -> Unit,
    onAutoWallpaperIntervalSelect: (Int) -> Unit,
    onTriggerAutoWallpaperNow: () -> Unit,
    onOpenAiStudio: () -> Unit,
    onUnlockPremium: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E2432),
                            Color(0xFF161A24)
                        )
                    )
                )
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(PrismPurple, PrismCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WallpaperX Power User",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (userSettings.isPremiumUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Filled.WorkspacePremium, contentDescription = "Premium", tint = PrismGold, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(
                            text = if (userSettings.isPremiumUser) (if (isArabic) "عضوية 8K Pro ممتازة ⭐" else "8K Pro Member ⭐")
                            else (if (isArabic) "الحساب المجاني (8K / 4K)" else "Free Account (8K / 4K)"),
                            color = PrismCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "7680x4320", color = PrismGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = if (isArabic) "الدقة النشطة" else "Active Res", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "100%", color = PrismCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = if (isArabic) "نقاء OLED" else "OLED Quality", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "8K Ultra", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = if (isArabic) "المحرك" else "Engine", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // AI Studio Quick Banner
        Box(
            modifier = Modifier
                .testTag("profile_ai_studio_banner")
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(listOf(PrismPurple, PrismCyan))
                )
                .clickable {
                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                    onOpenAiStudio()
                }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "استوديو توليد الخلفيات بالذكاء الإصطناعي" else "AI Wallpaper Generator Studio",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isArabic) "صمم مفاهيم 8K مخصصة بواسطة Gemini" else "Create custom 8K concepts using Gemini AI",
                            color = Color.Black.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
                Text(text = "⚡", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Auto Wallpaper Changer Settings Section
        Text(
            text = if (isArabic) "محرك التغيير التلقائي للبدائل (Auto Change)" else "Auto Wallpaper Changer Engine",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161A24))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "تفعيل التدوير التلقائي" else "Enable Auto Wallpaper Rotation",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isArabic) "يغير خلفيتك تلقائياً من المفضلة في الخلفية." else "Automatically cycles wallpaper from your favorites.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = userSettings.isAutoWallpaperEnabled,
                    onCheckedChange = {
                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                        onToggleAutoWallpaper(it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PrismCyan),
                    modifier = Modifier.testTag("auto_wallpaper_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Manual Change Trigger
            Button(
                onClick = {
                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.HEAVY)
                    onTriggerAutoWallpaperNow()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2432)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("trigger_auto_change_now")
                    .fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = PrismCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "تغيير الخلفية الان 🔄" else "Change Wallpaper Now 🔄",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Settings Section
        Text(
            text = if (isArabic) "إعدادات التطبيق" else "App Preferences",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161A24))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            // Language selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val nextLang = if (isArabic) "en" else "ar"
                        onSetLanguage(nextLang)
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = PrismCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = if (isArabic) "اللغة (Language)" else "Language", color = Color.White, fontSize = 14.sp)
                }
                Text(
                    text = if (isArabic) "العربية (RTL)" else "English",
                    color = PrismCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dark Mode toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Nightlight, contentDescription = null, tint = PrismPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = if (isArabic) "الوضع الداكن الممتاز (Dark Mode)" else "Dark Premium Mode", color = Color.White, fontSize = 14.sp)
                }
                Switch(
                    checked = userSettings.isDarkMode,
                    onCheckedChange = { onToggleDarkMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PrismPurple)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Premium Pro Upgrade Banner
        if (!userSettings.isPremiumUser) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2E1065),
                                Color(0xFF1E1B4B)
                            )
                        )
                    )
                    .border(1.dp, PrismGold, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.WorkspacePremium, contentDescription = null, tint = PrismGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "WallpaperX 8K PRO VIP",
                            color = PrismGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "احصل على وصول غير محدود لجميع خلفيات 8K الحصرية بدون إعلانات وبسرعة تحميل مضاعفة."
                        else "Get unlimited 8K downloads, exclusive albums, no ads, and 2x faster downloads.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.SUCCESS)
                            onUnlockPremium()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrismGold),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("unlock_premium_button")
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = if (isArabic) "ترقية للحساب الممتاز 👑" else "Upgrade to PRO VIP 👑",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
