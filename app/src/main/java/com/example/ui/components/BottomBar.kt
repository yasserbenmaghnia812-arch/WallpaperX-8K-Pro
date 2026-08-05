package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil

enum class ScreenTab(
    val route: String,
    val labelEn: String,
    val labelAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    EXPLORE("explore", "Explore", "استكشاف", Icons.Filled.CompassCalibration, Icons.Outlined.CompassCalibration),
    COLLECTIONS("collections", "Collections", "مجموعات", Icons.Filled.Collections, Icons.Outlined.Collections),
    FAVORITES("favorites", "Favorites", "المفضلة", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    PROFILE("profile", "Profile & AI", "الملف والذكاء", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun WallpaperXBottomBar(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(16.dp, shape = RoundedCornerShape(34.dp), spotColor = PrismCyan.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE161A24),
                            Color(0xFA0D0F16)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PrismCyan.copy(alpha = 0.4f),
                            PrismPurple.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) PrismCyan else Color(0xFF94A3B8),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "iconColor"
                )

                Box(
                    modifier = Modifier
                        .testTag("nav_tab_${tab.route}")
                        .clip(CircleShape)
                        .clickable {
                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                            onTabSelected(tab)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = if (isArabic) tab.labelAr else tab.labelEn,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        if (isSelected) {
                            Text(
                                text = if (isArabic) tab.labelAr else tab.labelEn,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
