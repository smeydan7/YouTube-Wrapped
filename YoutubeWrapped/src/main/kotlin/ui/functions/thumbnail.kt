package com.example.youtubewrapped.ui.functions

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import userApiKit.IdListRequest
import userApiKit.ThumbnailMapResponse

suspend fun getVideoThumbnails(videoIds: List<String>, accessToken: String): Map<String, String>? {
    return try {
        val response: HttpResponse = client.post("https://youtubewrapper-450406.uc.r.appspot.com/api/video-thumbnails") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(IdListRequest(ids = videoIds))
        }
        if (response.status.value in 200..299) {
            val result: ThumbnailMapResponse = response.body()
            result.thumbnails
        } else {
            println("Failed to fetch batched video thumbnails: ${response.status}")
            null
        }
    } catch (e: Exception) {
        println("Error fetching batched video thumbnails: ${e.message}")
        null
    }
}

suspend fun getChannelThumbnails(channelIds: List<String>, accessToken: String): Map<String, String>? {
    return try {
        val response: HttpResponse = client.post("https://youtubewrapper-450406.uc.r.appspot.com/api/channel-thumbnails") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $accessToken")
            setBody(IdListRequest(ids = channelIds))
        }
        if (response.status.value in 200..299) {
            val result: ThumbnailMapResponse = response.body()
            result.thumbnails
        } else {
            println("Failed to fetch batched channel thumbnails: ${response.status}")
            null
        }
    } catch (e: Exception) {
        println("Error fetching batched channel thumbnails: ${e.message}")
        null
    }
}