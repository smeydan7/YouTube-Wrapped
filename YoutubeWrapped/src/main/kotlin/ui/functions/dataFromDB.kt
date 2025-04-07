//package com.example.youtubewrapped.ui.functions
//
//import io.ktor.client.call.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.ktor.http.*
//import models.Channel
//import models.Video
//import userApiKit.HttpProvider
//
//
//val client = HttpProvider.client
//
////from videosWatched db
//suspend fun getTop5Videos(username: String): List<Video>? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/top-5-videos/$username")
//        println("username in getTop5Videos: $username")
//        if (response.status.isSuccess()) {
//            val videos: List<Video> = response.body()
//           // println("Top 5 videos: $videos")
//            videos
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}
//
////from videosWatched db (taking first item in column top-5-videos)
//suspend fun getTopVideo(username: String): Video? {
//    val videos = getTop5Videos(username)
//    return if (!videos.isNullOrEmpty()) {
//        // println("Top video: ${videos.first()}")
//        videos.first()
//    } else {
//        println("No top video found for $username")
//        null
//    }
//}
//
////from videosWatched db
//suspend fun getVideoCount(username: String): Long? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/video-count/$username")
//        println("username in getVideoCount: $username")
//        if (response.status.isSuccess()) {
//            val responseBody = response.bodyAsText()
//            val videoCount = responseBody.toLongOrNull()
//            println("Video count: $videoCount")
//            videoCount
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}
//
////from videosWatched db
//suspend fun getAdsCount(username: String): Long? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/ads-count/$username")
//        println("username in getAdsCount: $username")
//        if (response.status.isSuccess()) {
//            val responseBody = response.bodyAsText()
//            val adsCount = responseBody.toLongOrNull()
//            println("Ads count: $adsCount")
//            adsCount
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}
//
////from users db
//suspend fun getFirstName(username: String): String? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/first-name/$username")
//        println("username in getFirstName: $username")
//        if (response.status.isSuccess()) {
//            val firstName = response.bodyAsText()
//            println("First name: $firstName")
//            firstName
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}
//
////from users db
//suspend fun getLastName(username: String): String? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/last-name/$username")
//        println("username in getLastName: $username")
//        if (response.status.isSuccess()) {
//            val lastName = response.bodyAsText()
//            println("Last name: $lastName")
//            lastName
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}
//
//// from channels db
//suspend fun getChannelCount(username: String): Long? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/channel-count/$username")
//        println("username in getChannelCount: $username")
//        if (response.status.isSuccess()) {
//            val responseBody = response.bodyAsText()
//            val channelCount = responseBody.toLongOrNull()
//            println("Channel count: $channelCount")
//            channelCount
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}
//
//// from channels db
//suspend fun getTop10Channels(username: String): List<Channel>? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/top-10-channels/$username")
//        println("username in getTop10Channels: $username")
//        if (response.status.isSuccess()) {
//            val channels: List<Channel> = response.body()
//            println("Top 10 channels: $channels")
//            channels
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}
//
//// from channels db (using our getTop10Channels())
//suspend fun getTopChannel(username: String): Channel? {
//    val channels = getTop10Channels(username)
//    return if (!channels.isNullOrEmpty()) {
//        println("Top channel: ${channels.first()}")
//        channels.first()
//    } else {
//        println("No top channel found for $username")
//        null
//    }
//}
//
//// from videosWatched db
//suspend fun getMonthFrequency(username: String): Map<String, Int>? {
//    return try {
//        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/month-frequency/$username")
//        println("username in getMonthFrequency: $username")
//        if (response.status.isSuccess()) {
//            val monthFrequency: Map<String, Int> = response.body()
//            println("Month frequency: $monthFrequency")
//            monthFrequency
//        } else {
//            println("Failed: ${response.status}")
//            null
//        }
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//        null
//    }
//}