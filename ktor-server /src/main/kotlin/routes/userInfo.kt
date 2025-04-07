package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import database.tables.UsersTable
import database.tables.ChannelsTable
import database.tables.VideosTable
import database.tables.FriendRequestsTable
import database.tables.FriendsTable
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.Channel
import models.VideoInfo

// Define serializable response data classes.
@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val first_name: String,
    val last_name: String
)

@Serializable
data class ChannelsResponse(
    val num_channels: Long,
    val top_ten_channels: List<Channel>?
)

@Serializable
data class VideosResponse(
    val num_videos: Long,
    val top_five_titles: List<VideoInfo>?,
    val num_ads_viewed: Long,
    val month_frequency: Map<String, Int>?
)

@Serializable
data class FriendsResponse(
    val friend_requests: List<String>,
    val current_friends: List<String>
)

@Serializable
data class DashboardSummaryResponse(
    val user: UserResponse,
    val channels: ChannelsResponse?,
    val videos: VideosResponse?,
    val friends: FriendsResponse
)

fun Route.userSummary() {
    get("/user-summary/{username}") {
        val usernameParam = call.parameters["username"]
            ?: return@get call.respond("No username provided.")

        println("userSummary: Received request for username: $usernameParam")

        // Configure the JSON parser to ignore unknown keys.
        val jsonParser = Json { ignoreUnknownKeys = true }

        val result = transaction {
            println("userSummary: Starting transaction for user: $usernameParam")
            // Retrieve user info.
            val userRow = UsersTable.select { UsersTable.username eq usernameParam }
                .singleOrNull() ?: run {
                println("userSummary: No user found for username: $usernameParam")
                return@transaction null
            }
            val userId: UUID = userRow[UsersTable.id]
            println("userSummary: Found user with id: $userId")

            // Build user response.
            val userResponse = UserResponse(
                id = userId.toString(),
                username = userRow[UsersTable.username],
                first_name = userRow[UsersTable.first_name],
                last_name = userRow[UsersTable.last_name]
            )

            // Fetch channels data.
            val channelsRow = ChannelsTable.select { ChannelsTable.user_id eq userId }
                .singleOrNull()
            println("userSummary: channelsRow: $channelsRow")
            val channelsResponse = channelsRow?.let {
                val topTenChannelsJson = it[ChannelsTable.top_ten_channels]
                println("userSummary: topTenChannelsJson: $topTenChannelsJson")
                val topTenChannels: List<Channel>? = try {
                    jsonParser.decodeFromString(topTenChannelsJson)
                } catch (e: Exception) {
                    println("Error decoding topTenChannelsJson: ${e.message}")
                    null
                }
                ChannelsResponse(
                    num_channels = it[ChannelsTable.num_channels],
                    top_ten_channels = topTenChannels
                )
            }
            println("userSummary: channelsResponse: $channelsResponse")

            // Fetch videos data.
            val videosRow = VideosTable.select { VideosTable.user_id eq userId }
                .singleOrNull()
            println("userSummary: videosRow: $videosRow")
            val videosResponse = videosRow?.let {
                val topFiveTitlesJson = it[VideosTable.top_five_titles]
                println("userSummary: topFiveTitlesJson: $topFiveTitlesJson")
                val monthFrequencyJson = it[VideosTable.month_frequency]
                println("userSummary: monthFrequencyJson: $monthFrequencyJson")
                val topFiveTitles: List<VideoInfo>? = try {
                    jsonParser.decodeFromString(topFiveTitlesJson)
                } catch (e: Exception) {
                    println("Error decoding topFiveTitlesJson: ${e.message}")
                    null
                }
                val monthFrequency: Map<String, Int>? = try {
                    jsonParser.decodeFromString(monthFrequencyJson)
                } catch (e: Exception) {
                    println("Error decoding monthFrequencyJson: ${e.message}")
                    null
                }
                VideosResponse(
                    num_videos = it[VideosTable.num_videos],
                    top_five_titles = topFiveTitles,
                    num_ads_viewed = it[VideosTable.num_ads_viewed],
                    month_frequency = monthFrequency
                )
            }
            println("userSummary: videosResponse: $videosResponse")

            // Fetch friend requests.
            val friendRequests = FriendRequestsTable.select { FriendRequestsTable.receiverId eq userId }
                .mapNotNull { row ->
                    val senderId = row[FriendRequestsTable.senderId]
                    val senderUsername = UsersTable.select { UsersTable.id eq senderId }
                        .map { it[UsersTable.username] }
                        .firstOrNull()
                    println("userSummary: Friend request from: $senderUsername")
                    senderUsername
                }
            println("userSummary: friendRequests: $friendRequests")

            // Fetch current friends.
            val currentFriends = FriendsTable.select {
                (FriendsTable.user1_Id eq userId) or (FriendsTable.user2_Id eq userId)
            }.mapNotNull { row ->
                val friendId = if (row[FriendsTable.user1_Id] == userId)
                    row[FriendsTable.user2_Id]
                else
                    row[FriendsTable.user1_Id]
                val friendUsername = UsersTable.select { UsersTable.id eq friendId }
                    .map { it[UsersTable.username] }
                    .firstOrNull()
                println("userSummary: Found friend: $friendUsername")
                friendUsername
            }
            println("userSummary: currentFriends: $currentFriends")

            // Build friends response.
            val friendsResponse = FriendsResponse(
                friend_requests = friendRequests,
                current_friends = currentFriends
            )

            // Compose the full dashboard summary response.
            val responsePayload = DashboardSummaryResponse(
                user = userResponse,
                channels = channelsResponse,
                videos = videosResponse,
                friends = friendsResponse
            )
            println("userSummary: resultPayload: $responsePayload")
            responsePayload
        }

        if (result == null) {
            println("userSummary: result is null for username: $usernameParam")
            call.respond("User with username '$usernameParam' not found.")
        } else {
            println("userSummary: returning result for username: $usernameParam")
            call.respond(result)
        }
    }
}