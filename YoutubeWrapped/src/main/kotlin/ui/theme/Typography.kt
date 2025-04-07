package com.example.youtubewrapped.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.sp

val Typography = Typography(
    titleLarge = TextStyle(
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp
    ),
    displayLarge = TextStyle(
        fontSize = 28.sp,
        fontWeight = Bold
    )
)