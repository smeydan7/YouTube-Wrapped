
package routes
import io.ktor.server.application.*
import io.ktor.server.response.*
import database.tables.ChannelsTable
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import database.tables.UsersTable
import database.tables.VideosTable
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.VideoInfo


fun Route.videoCount() {
    // GET
    get("/video-count/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respond("No username provided.")

        // from username get id (which is a foreign key in VideosTable)
        val userId: UUID? = transaction {
            UsersTable
                .select { UsersTable.username eq username }
                .singleOrNull()
                ?.get(UsersTable.id)
        }

        // if the user is not in db
        if (userId == null) {
            return@get call.respond("User with username '$username' not found.")
        }

        //get video count from VideosTable using id
        val numVideos: Long? = transaction {
            VideosTable
                .select { VideosTable.user_id eq userId }
                .singleOrNull()
                ?.get(VideosTable.num_videos)
        }

        // if the user is not in db
        if (numVideos == null) {
            return@get call.respond("No videos found for user '$username'.")
        }

        // return video count
        call.respond(numVideos)
    }
}

////top 5 videos
//fun Route.top5VideosRoute() {
//    get("/top-5-videos/{username}") {
//        val username = call.parameters["username"]
//            ?: return@get call.respond("Username not given.")
//        val userId: UUID? = transaction {
//            UsersTable.select { UsersTable.username eq username }
//                .singleOrNull()
//                ?.get(UsersTable.id)
//        }
//
//        if (userId == null) {
//            return@get call.respond("User '$username' not found.")
//        }
//
//        val topFiveVideosJson: String? = transaction {
//            VideosTable.select { VideosTable.user_id eq userId }
//                .singleOrNull()
//                ?.get(VideosTable.top_five_titles)
//        }
//        if (topFiveVideosJson == null) {
//            call.respond("Top 5 videos not found for '$username'.")
//        } else {
//            //list
//            val jsonParser = Json { ignoreUnknownKeys = true }
//            val videos: List<VideoInfo> = jsonParser.decodeFromString(topFiveVideosJson)
//
//            //mapping
//            val videoSummaries = videos.map { VideoInfo(it.title, it.url, it.count) }
//            call.respond(videoSummaries)
//        }
//    }
//}
//
////number of ads watched
//fun Route.adsViewed() {
//    get("/ads-count/{username}") {
//        val username = call.parameters["username"]
//            ?: return@get call.respond("No username provided.")
//
//        val userId: UUID? = transaction {
//            UsersTable.select { UsersTable.username eq username }
//                .singleOrNull()
//                ?.get(UsersTable.id)
//        }
//
//        if (userId == null) {
//            return@get call.respond("User '$username' not found.")
//        }
//
//        val adsViewed: Long? = transaction {
//            VideosTable.select { VideosTable.user_id eq userId }
//                .singleOrNull()
//                ?.get(VideosTable.num_ads_viewed)
//        }
//
//        if (adsViewed == null) {
//            return@get call.respond("Ads count not found '$username'.")
//        }
//
//        call.respond(adsViewed)
//    }
//}
//
////month-frequency endpoint
//fun Route.monthFrequencyRoute() {
//    get("/month-frequency/{username}") {
//        val username = call.parameters["username"]
//            ?: return@get call.respond("No username provided.")
//
//        val userId: UUID? = transaction {
//            UsersTable.select { UsersTable.username eq username }
//                .singleOrNull()
//                ?.get(UsersTable.id)
//        }
//
//        if (userId == null) {
//            return@get call.respond("User '$username' not found.")
//        }
//
//        val monthFreqJson: String? = transaction {
//            VideosTable.select { VideosTable.user_id eq userId }
//                .singleOrNull()
//                ?.get(VideosTable.month_frequency)
//        }
//
//        if (monthFreqJson == null) {
//            return@get call.respond("Month frequency not found for '$username'.")
//        }
//
//        val jsonParser = Json { ignoreUnknownKeys = true }
//        val monthFreqMap: Map<String, Int> = jsonParser.decodeFromString(monthFreqJson)
//
//        call.respond(monthFreqMap)
//    }
//}