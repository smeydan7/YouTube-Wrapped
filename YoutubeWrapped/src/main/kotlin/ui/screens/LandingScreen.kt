package ui.screens

import java.awt.Desktop
import java.net.URI
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

import ui.auth.*

@Composable
fun LandingScreen(onTokenReceived: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            bitmap = useResource("images/youtube_logo.png") { loadImageBitmap(it) },
            contentDescription = "YouTube Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Title
        Text(
            text = "YouTube Wrapped",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "View stats about your YouTube watch time!",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign-In Button
        OutlinedButton(
            onClick = {openOAuthLogin(onTokenReceived)},
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .width(300.dp)
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Text(text = "Sign in with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}


