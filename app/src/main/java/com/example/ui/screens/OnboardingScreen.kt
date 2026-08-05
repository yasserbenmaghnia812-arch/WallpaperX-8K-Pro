package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismGold
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    isArabic: Boolean = true
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }
    val selectedInterests = remember { mutableStateListOf("Cyberpunk", "Amoled / Black", "Nature", "Abstract & 3D") }

    val categoriesList = listOf(
        "Cyberpunk", "Amoled / Black", "Nature", "Abstract & 3D",
        "Anime & Manga", "Space & Galaxy", "Minimalist", "Cars & Supercars",
        "Cities & Architecture", "Neon Glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        // Skip Button top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1, 2, 3).forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (step == index) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (step == index) PrismCyan else Color(0xFF334155))
                    )
                }
            }

            Text(
                text = if (isArabic) "تخطي" else "Skip",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .testTag("onboarding_skip_button")
                    .clickable {
                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                        onFinishOnboarding()
                    }
                    .padding(8.dp)
            )
        }

        // STEP 1: Brand Splash
        AnimatedVisibility(
            visible = step == 1,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrismPurple, PrismCyan, PrismGold)
                            )
                        )
                        .padding(3.dp),
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
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "WallpaperX 8K Pro",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your Screen, Your Canvas, Your 8K Reality",
                    color = PrismCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isArabic) "اكتشف أكثر من ١٠٠ ألف خلفية فائقة الدقة 8K فائقة الوضوح مع توصيات ذكية وتغيير تلقائي."
                    else "Discover 100,000+ ultra HD 8K wallpapers with smart AI curation and auto scheduler.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // STEP 2: Permissions Request UI
        AnimatedVisibility(
            visible = step == 2,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Wallpaper,
                    contentDescription = "Permissions",
                    tint = PrismCyan,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isArabic) "إعداد الصلاحيات والتجربة" else "Permissions & Experience",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Permission item 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2432))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Wallpaper, contentDescription = null, tint = PrismCyan)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "صلاحية تعيين الخلفيات" else "Set Wallpaper Access",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isArabic) "لتطبيق الخلفيات على الشاشة الرئيسية وشاشة القفل مباشرة." else "To set wallpapers on Home & Lock screen directly.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Permission item 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2432))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = null, tint = PrismGold)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "إشعارات خلفية اليوم" else "Daily Wallpaper Notifications",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isArabic) "لتصلك أجمل خلفية مختارة يومياً في الساعة 8 صباحاً." else "To receive daily featured 8K wallpaper drops.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // STEP 3: Category Interests
        AnimatedVisibility(
            visible = step == 3,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "Interests",
                    tint = PrismGold,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isArabic) "اختر تصنيفاتك المفضلة" else "Select Favorite Categories",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isArabic) "سنقوم بتجهيز خلاصة مخصصة لك بناءً على اهتماماتك" else "We will curate your personalized 8K feed immediately",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoriesList.forEach { cat ->
                        val isSelected = selectedInterests.contains(cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PrismCyan.copy(alpha = 0.2f) else Color(0xFF1E2432))
                                .border(1.dp, if (isSelected) PrismCyan else Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                                .clickable {
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                    if (isSelected) selectedInterests.remove(cat) else selectedInterests.add(cat)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = PrismCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = cat,
                                    color = if (isSelected) PrismCyan else Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Button
        Button(
            onClick = {
                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.MEDIUM)
                if (step < 3) {
                    step += 1
                } else {
                    onFinishOnboarding()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrismCyan),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .testTag("onboarding_next_button")
                .fillMaxWidth()
                .height(54.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = if (step == 3) (if (isArabic) "ابدأ الاستخدام الان 🚀" else "Get Started Now 🚀")
                else (if (isArabic) "المتابعة →" else "Continue →"),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
