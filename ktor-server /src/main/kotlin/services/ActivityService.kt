package services

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.YouTubeActivity
import models.VideoInfo
import models.ChannelInfo
import java.io.File
import database.repository.StatsRepository
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.Month

class ActivityService {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun getFilteredActivities(filePath: String, username: String): List<YouTubeActivity> {
        try {
            println("Reading JSON file...")

            val file = File(filePath)

            if (!file.exists()) {
                println("ERROR: File not found at path: $filePath")
                return emptyList()
            }

            val fileContent = file.readText()

            val allActivities: List<YouTubeActivity> = jsonParser.decodeFromString(fileContent)

            val adsCount = allActivities.count { activity ->
                activity.details?.any { it.name == "From Google Ads" } ?: false
            }
            println("Found $adsCount ads in the watch history.")

            val filteredActivities = allActivities.filter { activity ->
                activity.details?.none { it.name == "From Google Ads" } ?: true
            }

            val numVideos = filteredActivities.size
            val videoCountMap = mutableMapOf<String, Int>()
            val urlToTitleMap = mutableMapOf<String, String>()

            val channelCountMap = mutableMapOf<String, Int>()
            val channelToUrlMap = mutableMapOf<String, String>()

            val monthCountMap = mutableMapOf<Month, Int>()

            for (activity in filteredActivities) {
                val videoTitleUrl = activity.titleUrl ?: continue
                val rawTitle = activity.title ?: "Unknown Title"
                val videoTitle = rawTitle.removePrefix("Watched ")

                val timeString = activity.time ?: continue

                val dateTime = ZonedDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
                val month = dateTime.month

                monthCountMap[month] = monthCountMap.getOrDefault(month, 0) + 1

                videoCountMap[videoTitleUrl] = videoCountMap.getOrDefault(videoTitleUrl, 0) + 1
                urlToTitleMap.putIfAbsent(videoTitleUrl, videoTitle)

                val channel = activity.subtitles?.firstOrNull()
                val channelName = channel?.name
                val channelUrl = channel?.url

                if (!channelName.isNullOrBlank() && channelName != "Unknown Channel" && !channelUrl.isNullOrBlank()) {
                    channelCountMap[channelName] = channelCountMap.getOrDefault(channelName, 0) + 1
                    channelToUrlMap.putIfAbsent(channelName, channelUrl)
                }
            }

            val topFiveVideosUrls = videoCountMap.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }

            val topFiveVideoStats = topFiveVideosUrls.map { url ->
                VideoInfo(
                    title = urlToTitleMap[url] ?: "Unkown Title",
                    url = url,
                    count = videoCountMap[url] ?: 0
                )
            }

            topFiveVideoStats.forEachIndexed { index, videoInfo ->
                println("${index + 1}. ${videoInfo.title} (${videoInfo.url}) - Count: ${videoInfo.count}")
            }

            val topTenChannelNames = channelCountMap.entries
                .sortedByDescending { it.value }
                .take(10)
                .map { it.key }

            val topTenChannelStats = topTenChannelNames.map { channelName ->
                ChannelInfo(
                    name = channelName,
                    url = channelToUrlMap[channelName] ?: "#",
                    count = channelCountMap[channelName] ?: 0
                )
            }

            topTenChannelStats.forEachIndexed { index, channelInfo ->
                println("${index + 1}. ${channelInfo.name} (${channelInfo.url}) - Count: ${channelInfo.count}")
            }

            StatsRepository.insertUserVideoStats(username, numVideos.toLong(), topFiveVideoStats, adsCount.toLong(), monthCountMap)
            StatsRepository.insertUserChannelStats(username, channelCountMap.size.toLong(), topTenChannelStats)

            return filteredActivities
        } catch (e: Exception) {
            println("Error in getFilteredActivities: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

}