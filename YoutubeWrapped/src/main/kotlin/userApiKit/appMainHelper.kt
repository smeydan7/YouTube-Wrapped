package userApiKit

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import models.Channel
import models.Video
import kotlinx.serialization.json.JsonElement

data class DashboardData(
    val username: String,
    val firstName: String,
    val videoCount: Long,
    val adsCount: Long,
    val channelsCount: Long,
    val topVideos: List<Video>,
    val topChannels: List<Channel>,
    val topVideo: Video?,
    val topChannel: Channel?,
    val monthFrequency: Map<String, Int>,
    val topVideoThumbnails: Map<Video, String?>?,
    val topChannelThumbnails: Map<Channel, String?>?
)

@Serializable
data class FriendUser(
    val id: String,
    val username: String,
    val email: String?,
    val first_name: String?,
    val last_name: String?
)


data class FriendsDashboardData(
    val friends: List<FriendUser> = emptyList(),
    val dashboardCache: Map<String, DashboardData> = emptyMap()
)

data class FriendsScreenData(
    val friends: List<FriendUser>,
    val incomingRequests: List<FriendUser>,
    val outgoingRequests: List<FriendUser>
)

data class AppState(
    val username: String?,
    val userId: String?
)


@Serializable
data class IdListRequest(
    val ids: List<String>
)

@Serializable
data class ThumbnailMapResponse(
    val thumbnails: Map<String, String>
)

suspend fun fetchUserIdByUsername(username: String): String? {
    val response: HttpResponse = HttpProvider.client.get("https://youtubewrapper-450406.uc.r.appspot.com/user-id/$username")
    return if (response.status == HttpStatusCode.OK) {
        val idJson: JsonElement = Json.parseToJsonElement(response.bodyAsText())
        idJson.jsonObject["user_id"]?.jsonPrimitive?.content
    } else null
}

suspend fun fetchUsernameAndUserId(accessToken: String, client: HttpClient = HttpProvider.client): Pair<String?, String?> {
    return try {
        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/api/username") {
            header("Authorization", "Bearer $accessToken")
        }
        if (response.status.isSuccess()) {

            val responseBody = response.bodyAsText()
            val json = Json.parseToJsonElement(responseBody).jsonObject
            val fetchedUsername = json["username"]?.jsonPrimitive?.content

            if (fetchedUsername != null) {
                val userIdResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/user-id/$fetchedUsername")
                println("getting user id: $userIdResponse")
                val idJson = Json.parseToJsonElement(userIdResponse.bodyAsText()).jsonObject
                println("getting user id: $idJson")
                val fetchedUserId = idJson["user_id"]?.jsonPrimitive?.content
                println(fetchedUserId)
                return Pair(fetchedUsername, fetchedUserId)
            }
        }
        null to null
    } catch (e: Exception) {
        println("Error: ${e.message}")
        null to null
    }
}