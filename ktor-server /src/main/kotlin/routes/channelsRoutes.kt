//package routes
//
//import io.ktor.server.routing.*
//import io.ktor.server.application.*
//import io.ktor.server.response.*
//import org.jetbrains.exposed.sql.*
//import org.jetbrains.exposed.sql.transactions.transaction
//import io.ktor.server.application.*
//import io.ktor.server.response.*
//import database.tables.ChannelsTable
//import io.ktor.server.routing.*
//import org.jetbrains.exposed.sql.*
//import org.jetbrains.exposed.sql.transactions.transaction
//import database.tables.UsersTable
//import database.tables.VideosTable
//import java.util.UUID
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.json.Json
//import models.Channel
//
//fun Route.channelCount() {
//    get("/channel-count/{username}") {
//        val username = call.parameters["username"]
//            ?: return@get call.respond("No username provided.")
//
//        // from username get id (which is a foreign key in channel db)
//        val userId: UUID? = transaction {
//            UsersTable
//                .select { UsersTable.username eq username }
//                .singleOrNull()
//                ?.get(UsersTable.id)
//        }
//
//        // if user is not in db
//        if (userId == null) {
//            return@get call.respond("User with username '$username' not found.")
//        }
//
//        //channel count
//        val numChannels: Long? = transaction {
//            ChannelsTable
//                .select { ChannelsTable.user_id eq userId }
//                .singleOrNull()
//                ?.get(ChannelsTable.num_channels)
//        }
//
//        if (numChannels == null) {
//            return@get call.respond("No channels found for user '$username'.")
//        }
//
//        call.respond(numChannels)
//    }
//}
//
//// top 10 channels
//fun Route.top10ChannelsRoute() {
//    get("/top-10-channels/{username}") {
//        val username = call.parameters["username"]
//            ?: return@get call.respond("Username not given.")
//
//
//        val userId: UUID? = transaction {
//            UsersTable.select { UsersTable.username eq username }
//                .singleOrNull()
//                ?.get(UsersTable.id)
//        }
//
//
//        if (userId == null) {
//            return@get call.respond("User '$username' not found.")
//        }
//
//        val topTenChannelsJson: String? = transaction {
//            ChannelsTable.select { ChannelsTable.user_id eq userId }
//                .singleOrNull()
//                ?.get(ChannelsTable.top_ten_channels)
//        }
//
//        if (topTenChannelsJson == null) {
//            call.respond("Top 10 channels not found for '$username'.")
//        } else {
//            val jsonParser = Json { ignoreUnknownKeys = true }
//            val channels: List<Channel> = jsonParser.decodeFromString(topTenChannelsJson)
//
//
//            val channelSummaries = channels.map { Channel(it.name, it.url, it.count) }
//            call.respond(channelSummaries)
//        }
//    }
//}