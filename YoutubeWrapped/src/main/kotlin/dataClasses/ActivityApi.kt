package dataClasses

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.YouTubeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ActivityApi {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getActivities(): List<YouTubeActivity> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url("https://youtubewrapper-450406.uc.r.appspot.com/activities").build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: "[]"
            json.decodeFromString(body)
        }
    }
}
