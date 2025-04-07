package ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(onHomeClick: () -> Unit) {
    TopAppBar(
        title = { Spacer(modifier = Modifier.height(0.dp)) }, // Empty title to satisfy required param
        navigationIcon = {
            IconButton(onClick = onHomeClick) {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(36.dp), // Bigger Home Icon
                    tint = Color.Black
                )
            }
        }
    )
}