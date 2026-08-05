package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorFilter
import com.example.ui.util.HapticFeedbackUtil

@Composable
fun ColorFilterWheel(
    colorFilters: List<ColorFilter>,
    selectedColorHex: String?,
    onSelectColor: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // "All Colors" chip
            val isAllSelected = selectedColorHex == null
            Box(
                modifier = Modifier
                    .testTag("color_filter_all")
                    .clip(CircleShape)
                    .background(if (isAllSelected) Color(0xFF00F0FF) else Color(0xFF1E2432))
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                    .clickable {
                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                        onSelectColor(null)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌈 الكل",
                    color = if (isAllSelected) Color.Black else Color.White,
                    fontSize = 11.sp
                )
            }
        }

        items(colorFilters) { filter ->
            val color = try {
                Color(android.graphics.Color.parseColor(filter.hex))
            } catch (e: Exception) {
                Color.Cyan
            }
            val isSelected = selectedColorHex == filter.hex

            Box(
                modifier = Modifier
                    .testTag("color_filter_${filter.name}")
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else Color(0x44FFFFFF),
                        shape = CircleShape
                    )
                    .clickable {
                        HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                        onSelectColor(if (isSelected) null else filter.hex)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = if (filter.hex == "#F0F4F8" || filter.hex == "#FFFFFF") Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
