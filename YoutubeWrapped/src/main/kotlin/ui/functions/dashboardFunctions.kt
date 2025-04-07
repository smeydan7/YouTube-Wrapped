package ui.functions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Desktop
import java.net.URI
import java.net.URL
import javax.imageio.ImageIO



@Composable
fun NumberedGridCard(title: String, items: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    for (i in 0 until 5) {
                        if (i < items.size) {
                            Text(
                                text = "${i + 1}. ${items[i]}",
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    for (i in 5 until 10) {
                        if (i < items.size) {
                            Text(
                                text = "${i + 1}. ${items[i]}",
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun extractVideoId(videoUrl: String): String? {
    return videoUrl.substringAfter("v=", missingDelimiterValue = "")
}
fun extractChannelId(channelUrl: String): String? {
    return channelUrl.substringAfter("channel/", missingDelimiterValue = "")
}


// Put this in a shared place (like a file-level variable)
val thumbnailCache = mutableMapOf<String, ImageBitmap?>()

@Composable
fun ImgViewer(
    videoTitle: String,
    videoUrl: String,
    thumbnailUrl: String,
    width: Dp = 200.dp,
    aspectRatio: Float = 16f / 9f,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember(thumbnailUrl) {
        mutableStateOf<ImageBitmap?>(thumbnailCache[thumbnailUrl])
    }

    LaunchedEffect(thumbnailUrl) {
        if (imageBitmap == null && thumbnailUrl.isNotBlank()) {
            try {
                val url = URL(thumbnailUrl)
                val bufferedImage = ImageIO.read(url)
                val bitmap = bufferedImage?.toComposeImageBitmap()
                thumbnailCache[thumbnailUrl] = bitmap
                imageBitmap = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .aspectRatio(aspectRatio)
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Loading...")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = videoTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI(videoUrl))
                }
            }
        )
    }
}