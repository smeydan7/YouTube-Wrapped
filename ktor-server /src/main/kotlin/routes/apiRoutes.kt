package routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import services.getGoogleUserInfo
import java.time.Duration
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import database.DatabaseFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import model.JWTConfig
import model.OAuthConfig
import com.example.plugins.configureSecurity
import com.example.plugins.configureRouting
import java.util.Properties
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.example.plugins.configureSerialization
import repository.HttpBackendProvider

@Serializable
data class YouTubeVideoResponse(
    val items: List<YouTubeVideoItem>
)

@Serializable
data class YouTubeVideoItem(
    val id: String,
    val contentDetails: ContentDetails? = null,
    val snippet: Snippet? = null
)
@Serializable
data class IdListRequest(
    val ids: List<String>
)

@Serializable
data class ContentDetails(val duration: String)
@Serializable
data class Snippet(val thumbnails: Thumbnails)
@Serializable
data class Thumbnails(val high: Thumbnail)
@Serializable
data class Thumbnail(val url: String)

@Serializable
data class YouTubeChannelResponse(
    val items: List<YouTubeChannelItem>
)
@Serializable
data class YouTubeChannelItem(
    val id: String,
    val snippet: Snippet
)
val client = HttpBackendProvider.client
val json = Json { ignoreUnknownKeys = true }

fun Route.apiRoutes() {

    val props = Properties().apply {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("auth.properties")
            ?: throw IllegalStateException("Could not load database.properties")
        load(stream)
    }
    val youtubeApiKey = props.getProperty("YOUTUBE_API_KEY") ?: "MISSING_KEY"

    authenticate("jwt-auth") {
        route("/api") {
            get("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val googleAccessToken = principal?.payload?.getClaim("google_access_token")?.asString()
                if (googleAccessToken == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Google access token not found")
                    return@get
                }

                val googleUser = try {
                    getGoogleUserInfo(googleAccessToken)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching Google user info: ${e.message}")
                    return@get
                }

                call.respond(googleUser)
            }

            get("/username") {
                val principal = call.principal<JWTPrincipal>()
                val googleAccessToken = principal?.payload?.getClaim("google_access_token")?.asString()
                if (googleAccessToken == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Google access token not found")
                    return@get
                }
                val googleUser = try {
                    getGoogleUserInfo(googleAccessToken)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching Google user info: ${e.message}")
                    return@get
                }


                val email = googleUser.email
                println("email: $email")
                val username = (email as String).substringBefore("@")
                print("username: $username")
                call.respond(mapOf("username" to username))
            }

            get("/video-duration") {
                val videoId = call.request.queryParameters["videoId"]
                if (videoId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing or empty 'videoId' parameter.")
                    return@get
                }

                try {
                    val url = "https://www.googleapis.com/youtube/v3/videos" +
                            "?part=contentDetails" +
                            "&id=$videoId" +
                            "&key=$youtubeApiKey"

                    // Make the request to the YouTube Data API
                    val response: HttpResponse = client.get(url)
                    if (response.status == HttpStatusCode.OK) {
                        val responseBody: YouTubeVideoResponse = response.body()
                        val contentDetails = responseBody.items.firstOrNull()?.contentDetails
                        if (contentDetails == null) {
                            call.respond(HttpStatusCode.NotFound, "Video not found or missing contentDetails.")
                            return@get
                        }

                        val durationStr = contentDetails.duration
                        val fullDuration = Duration.parse(durationStr)
                        val partialDuration = fullDuration.multipliedBy(7).dividedBy(10)

                        call.respond(mapOf("partialDuration" to partialDuration.toString()))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Error from YouTube API: ${response.status}")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Exception: ${e.message}")
                }
            }

            post("/video-thumbnails") {
                val request = call.receive<IdListRequest>()
                if (request.ids.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "List of video IDs is empty.")
                    return@post
                }
                // Join the IDs into a comma-separated string
                val videoIds = request.ids.joinToString(",")
                try {
                    val url = "https://www.googleapis.com/youtube/v3/videos" +
                            "?part=snippet" +
                            "&id=$videoIds" +
                            "&key=$youtubeApiKey"
                    val response: HttpResponse = client.get(url)
                    if (response.status == HttpStatusCode.OK) {
                        val responseBody: YouTubeVideoResponse = response.body()
                        // Specify the type for 'item' explicitly:
                        val thumbnailMap: Map<String, String> = responseBody.items.associate { item: YouTubeVideoItem ->
                            item.id to (item.snippet?.thumbnails?.high?.url ?: "")
                        }
                        call.respond(mapOf("thumbnails" to thumbnailMap))
                    } else {
                        val errorBody = response.bodyAsText()
                        call.respond(HttpStatusCode.InternalServerError, "YouTube API error: ${response.status}: $errorBody")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Exception: ${e.message}")
                }
            }

            // Batch endpoint for channel thumbnails
            post("/channel-thumbnails") {
                val request = call.receive<IdListRequest>()
                if (request.ids.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "List of channel IDs is empty.")
                    return@post
                }
                val channelIds = request.ids.joinToString(",")
                try {
                    val url = "https://www.googleapis.com/youtube/v3/channels" +
                            "?part=snippet" +
                            "&id=$channelIds" +
                            "&key=$youtubeApiKey"
                    val response: HttpResponse = client.get(url)
                    if (response.status == HttpStatusCode.OK) {
                        val responseText = response.bodyAsText()
                        val responseBody = json.decodeFromString<YouTubeChannelResponse>(responseText)

                        val thumbnailMap: Map<String, String> = responseBody.items.associate { item ->
                            item.id to (item.snippet?.thumbnails?.high?.url ?: "")
                        }
                        call.respond(mapOf("thumbnails" to thumbnailMap))
                    } else {
                        val errorBody = response.bodyAsText()
                        call.respond(HttpStatusCode.InternalServerError, "YouTube API error: $errorBody")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Exception: ${e.message}")
                }
            }
        }

    }
}