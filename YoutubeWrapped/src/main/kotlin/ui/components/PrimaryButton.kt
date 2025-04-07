package com.example.youtubewrapped.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.youtubewrapped.ui.theme.ButtonRed
import com.example.youtubewrapped.ui.theme.TextWhite

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed)
    ) {
        Text(text = text, color = TextWhite, style = MaterialTheme.typography.labelLarge)
    }
}
