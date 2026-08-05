package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrismCyan
import com.example.ui.theme.PrismGold
import com.example.ui.theme.PrismPurple
import com.example.ui.util.HapticFeedbackUtil
import com.example.viewmodel.AiGenerationUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiGeneratorScreen(
    aiState: AiGenerationUiState,
    onGenerateClick: (prompt: String, style: String) -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var promptInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk") }

    val stylesList = listOf(
        "Cyberpunk", "3D Glassmorphism", "Amoled Black", "Anime Ultra",
        "Oil Painting", "Minimalist Zen", "Cosmic Space", "Neon Futuristic"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(PrismPurple, PrismCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = "AI Studio", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isArabic) "استوديو الذكاء الاصطناعي 🤖" else "AI Wallpaper Studio 🤖",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) "ابتكر مفهوم خلفيتك الـ 8K بواسطة Gemini" else "Craft 8K concepts powered by Gemini AI",
                    color = PrismCyan,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Prompt Text Field
        Text(
            text = if (isArabic) "وصف الخلفية المطلوبة (Prompt)" else "Describe your dream wallpaper prompt",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            placeholder = {
                Text(
                    text = if (isArabic) "مثال: مدينة نيون عائمة في الفضاء مع انعكاسات مائية وحلقات زجاجية..."
                    else "E.g., Futuristic neon floating city with crystal water reflections and glass rings...",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            },
            modifier = Modifier
                .testTag("ai_prompt_input")
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161A24),
                unfocusedContainerColor = Color(0xFF161A24),
                focusedBorderColor = PrismCyan,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Style presets
        Text(
            text = if (isArabic) "اختر النمط الفني (Style Preset)" else "Select Art Style Preset",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            stylesList.forEach { style ->
                val isSelected = selectedStyle == style
                Box(
                    modifier = Modifier
                        .testTag("ai_style_$style")
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrismPurple else Color(0xFF1E2432))
                        .border(1.dp, if (isSelected) PrismCyan else Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .clickable {
                            HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                            selectedStyle = style
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = style,
                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Generate Action Button
        Button(
            onClick = {
                HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.HEAVY)
                onGenerateClick(
                    if (promptInput.isBlank()) "Futuristic Prism Light W" else promptInput,
                    selectedStyle
                )
            },
            enabled = aiState !is AiGenerationUiState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = PrismCyan),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .testTag("ai_generate_button")
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (aiState is AiGenerationUiState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = if (isArabic) "جاري التحليل بواسطة Gemini..." else "Generating with Gemini...", color = Color.Black)
            } else {
                Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isArabic) "توليد مفهوم الخلفية 8K ⚡" else "Generate 8K Concept ⚡",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result View
        if (aiState is AiGenerationUiState.Success) {
            val res = aiState.result
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF161A24))
                    .border(1.dp, PrismCyan, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = res.title,
                        color = PrismGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrismPurple)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "GEMINI 8K", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = res.description,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Extracted Palette Circles
                Text(
                    text = if (isArabic) "لوحة الألوان الموصى بها:" else "Suggested Color Palette:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    res.suggestedColors.forEach { hex ->
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Cyan }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White, CircleShape)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(hex))
                                    HapticFeedbackUtil.performHaptic(context, HapticFeedbackUtil.HapticType.LIGHT)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}
