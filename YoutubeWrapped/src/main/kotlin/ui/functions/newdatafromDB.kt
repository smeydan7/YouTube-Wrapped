package com.example.youtubewrapped.ui.functions

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import models.Channel
import models.Video
import userApiKit.DashboardData
import userApiKit.HttpProvider

val client = HttpProvider.client

@Serializable
data class DashboardResponse(
    val user: UserInfo,
    val channels: ChannelsData,
    val videos: VideosData
)

@Serializable
data class UserInfo(
    val id: String,
    val username: String,
    val first_name: String,
    val last_name: String
)

@Serializable
data class ChannelsData(
    val num_channels: Long,
    val top_ten_channels: List<Channel>
)

@Serializable
data class VideosData(
    val num_videos: Long,
    val top_five_titles: List<Video>,
    val num_ads_viewed: Long,
    val month_frequency: Map<String, Int>
)

suspend fun getDashboardSummary(username: String): DashboardData? {
    return try {
        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/user-summary/$username")
        println("Fetching dashboard summary for $username")
        if (response.status.isSuccess()) {
            val dashboardResponse: DashboardResponse = response.body()
            DashboardData(
                username = dashboardResponse.user.username,
                firstName = dashboardResponse.user.first_name,
                videoCount = dashboardResponse.videos.num_videos,
                adsCount = dashboardResponse.videos.num_ads_viewed,
                channelsCount = dashboardResponse.channels.num_channels,
                topVideos = dashboardResponse.videos.top_five_titles,
                topChannels = dashboardResponse.channels.top_ten_channels,
                topVideo = dashboardResponse.videos.top_five_titles.firstOrNull(),
                topChannel = dashboardResponse.channels.top_ten_channels.firstOrNull(),
                topVideoThumbnails = null,
                topChannelThumbnails = null,
                monthFrequency = dashboardResponse.videos.month_frequency
            )
        } else {
            println("Failed to get dashboard summary: ${response.status}")
            null
        }
    } catch (e: Exception) {
        println("Error getting dashboard summary: ${e.message}")
        null
    }
}