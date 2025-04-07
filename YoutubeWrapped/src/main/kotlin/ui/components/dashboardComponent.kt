package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Channel
import models.Video
import ui.functions.ImgViewer
import userApiKit.DashboardData
import java.awt.Desktop
import java.net.URI

@Composable
fun DashboardComponent(
    dashboardData: DashboardData?
){

    val orderedMonthLabels = listOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )

    dashboardData?.let { data ->
        val scrollState = rememberScrollState()

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxHeight = this.maxHeight
            Column(
                modifier = Modifier
                    .heightIn(max = maxHeight)
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Stats Cards
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatsCard("Videos Watched", "${data.videoCount}", Modifier.weight(1f))
                    StatsCard("Creators", "${data.channelsCount}", Modifier.weight(1f))
                    StatsCard("Ads Watched", "${data.adsCount}", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bar Chart
                Column(modifier = Modifier.fillMaxWidth()) {
                    BarChartComponent(
                        title = "Most Active Month",
                        data = orderedMonthLabels.map { data.monthFrequency[it] ?: 0 },
                        labels = orderedMonthLabels.map { it.take(3).capitalize() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        orderedMonthLabels.forEach { label ->
                            Text(
                                text = label.take(3).capitalize(),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        data.topChannelThumbnails?.let {
                            TopCreatorsList(
                                channels = data.topChannels,
                                thumbnails = it
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {

                        data.topVideoThumbnails?.let {
                            TopVideosList(
                                videos = data.topVideos,
                                thumbnails = it
                            )
                        }
                    }
                }
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading dashboard data...")
    }
}

@Composable
fun TopCreatorRow(rank: Int, channel: Channel, thumbnailUrl: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 6.dp)
            .clickable {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI(channel.url))
                }
            }
    ) {
        Text("$rank.", modifier = Modifier.padding(end = 8.dp))

        if (thumbnailUrl != null) {
            ImgViewer(
                videoTitle = "",
                videoUrl = "",
                thumbnailUrl = thumbnailUrl,
                modifier = Modifier
                    .size(105.dp)
                    .padding(end = 8.dp)
            )
        }

        Column {
            Text(channel.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("Watched ${channel.count} videos", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}

@Composable
fun TopCreatorsList(
    channels: List<Channel>,
    thumbnails: Map<Channel, String?>
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Top Creators",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                val left = channels.take(5)
                val right = channels.drop(5)

                Column(modifier = Modifier.weight(1f)) {
                    left.forEachIndexed { index, channel ->
                        TopCreatorRow(index + 1, channel, thumbnails[channel])
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    right.forEachIndexed { index, channel ->
                        TopCreatorRow(index + 6, channel, thumbnails[channel])
                    }
                }
            }
        }
    }
}

@Composable
fun TopVideosList(
    videos: List<Video>,
    thumbnails: Map<Video, String?>
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Top Videos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            videos.forEach { video ->
                val thumbnailUrl = thumbnails[video]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().browse(URI(video.url))
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    thumbnailUrl?.let {
                        ImgViewer(
                            videoTitle = video.title,
                            videoUrl = video.url,
                            thumbnailUrl = it,
                            modifier = Modifier
                                .width(180.dp)
                                .aspectRatio(16f / 9f)
                                .padding(end = 12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Watched ${video.count} times",
                            color = androidx.compose.ui.graphics.Color(0xFFD32F2F),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}