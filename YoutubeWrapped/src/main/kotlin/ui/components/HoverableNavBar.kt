package ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerMoveFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HoverableNavArea(enabled: Boolean, onClick: () -> Unit, icon: ImageVector, modifier: Modifier = Modifier) {
    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (isHovered) Color.LightGray.copy(alpha = 0.4f) else Color.Transparent)
            .pointerMoveFilter(
                onEnter = { isHovered = true; false },
                onExit = { isHovered = false; false }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = "Navigate", modifier = Modifier.size(70.dp)) // Bigger Arrows
    }
}
//
//@Preview
//@Composable
//fun PreviewHoverableNavArea() {
//    Row(modifier = Modifier.fillMaxWidth().height(400.dp)) {
//        HoverableNavArea(enabled = true, onClick = {}, icon = Icons.Default.ArrowBack)
//        Spacer(modifier = Modifier.width(16.dp))
//        HoverableNavArea(enabled = true, onClick = {}, icon = Icons.Default.ArrowForward)
//    }
//}
