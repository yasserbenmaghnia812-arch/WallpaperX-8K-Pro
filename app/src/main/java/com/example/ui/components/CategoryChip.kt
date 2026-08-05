package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.WallpaperCategory
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil

@Composable
fun CategoryChip(
    category: WallpaperCategory,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrismCyan.copy(alpha = 0.2f) else Color(0xFF1E2432),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrismCyan else Color(0x33FFFFFF),
        label = "chipBorder"
    )

    Box(
        modifier = modifier
            .testTag("category_chip_${category.id}")
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable {
                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                onSelect()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (category.isHot) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Hot",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = if (isArabic) category.nameAr else category.name,
                color = if (isSelected) PrismCyan else Color.White,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
