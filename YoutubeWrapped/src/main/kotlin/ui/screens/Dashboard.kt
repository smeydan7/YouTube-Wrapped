package ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.youtubewrapped.ui.functions.getChannelThumbnails
import com.example.youtubewrapped.ui.functions.getDashboardSummary
import com.example.youtubewrapped.ui.functions.getVideoThumbnails
import ui.components.DashboardComponent
import ui.components.TabButton
import ui.functions.extractChannelId
import ui.functions.extractVideoId
import userApiKit.DashboardData

@Composable
fun DashboardScreen(
    accessToken: String,
    username: String,
    onNavigateToFriends: () -> Unit,
    onNavigateToProfile: () -> Unit,
    existingData: DashboardData?,
    onDataFetched: (DashboardData) -> Unit,
    onUploadData: () -> Unit,
    needsRefresh: Boolean,
    onNeedsRefreshChange: (Boolean) -> Unit,
    shouldShowUploadReminder: Boolean = false
) {
    val orderedMonthLabels = listOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )

    println("ON dashboard screen")
    var dashboardData by remember { mutableStateOf(existingData) }

    LaunchedEffect(Unit) {
        if (dashboardData == null || needsRefresh) {

            val compositeData = getDashboardSummary(username)
            println("fetching dashboard data: $compositeData")
            if (compositeData != null) {

                val videoIds = compositeData.topVideos.mapNotNull { extractVideoId(it.url) }
                val videoThumbnailsMap = getVideoThumbnails(videoIds, accessToken) ?: emptyMap()
                val channelIds = compositeData.topChannels.mapNotNull { extractChannelId(it.url) }
                val channelThumbnailsMap = getChannelThumbnails(channelIds, accessToken) ?: emptyMap()

                val topVideoThumbnails = compositeData.topVideos.associate { video ->
                    val videoId = extractVideoId(video.url)
                    video to (if (videoId != null) videoThumbnailsMap[videoId] else null)
                }

                // Map each channel to its thumbnail URL from the batched result
                val topChannelThumbnails = compositeData.topChannels.associate { channel ->
                    val channelId = extractChannelId(channel.url)
                    channel to (if (channelId != null) channelThumbnailsMap[channelId] else null)
                }

                val topVideo = compositeData.topVideos.firstOrNull()
                val topChannel = compositeData.topChannels.firstOrNull()
                // Create an updated DashboardData object with the extra thumbnail info.
                val newData = compositeData.copy(
                    topVideo = topVideo,
                    topChannel = topChannel,
                    topVideoThumbnails = topVideoThumbnails,
                    topChannelThumbnails = topChannelThumbnails
                )
                dashboardData = newData
                onDataFetched(newData)
            }
            onNeedsRefreshChange(false)
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    TabButton(
                        title = "Dashboard",
                        selected = true,
                        onClick = { /* already on dashboard */ },
                        modifier = Modifier
                            .padding(8.dp)
                            .width(160.dp)
                            .height(50.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TabButton(
                        title = "Friends",
                        selected = false,
                        onClick = onNavigateToFriends,
                        modifier = Modifier
                            .padding(8.dp)
                            .width(160.dp)
                            .height(50.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onNavigateToProfile() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shouldShowUploadReminder) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Go upload your data!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onUploadData,
                    modifier = Modifier.height(40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload new data")
                }
            }

            dashboardData?.let {
                Text(
                    text = "${it.firstName}'s YouTube Wrapped:",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                DashboardComponent(dashboardData = dashboardData)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                println("LOADING IN DASHBOARD")
                Text("Loading dashboard data...do not leave the screen until loaded")
            }
        }
    }
}