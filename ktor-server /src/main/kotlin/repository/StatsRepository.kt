package database.repository

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import database.tables.VideosTable
import database.tables.UsersTable
import database.tables.ChannelsTable
import models.VideoInfo
import models.ChannelInfo
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.Month

object StatsRepository {

    fun insertUserVideoStats(username: String, numVideos: Long, topFiveVideoStats: List<VideoInfo>, numAds: Long, monthCount: Map<Month, Int>) {
        println("Inserting ${username}'s stats...")

        transaction {
            val userUUID = UsersTable
                .select { UsersTable.username eq username }
                .singleOrNull()
                ?.get(UsersTable.id)
                ?: throw Exception("User not found")

            VideosTable.deleteWhere { VideosTable.user_id eq userUUID }

            val stringKeyMap = monthCount.mapKeys { (month, _) -> month.name }

            VideosTable.insert {
                it[id] = UUID.randomUUID()
                it[user_id] = userUUID
                it[num_videos] = numVideos
                it[top_five_titles] = Json.encodeToString(topFiveVideoStats)
                it[num_ads_viewed] = numAds
                it[month_frequency] = Json.encodeToString(stringKeyMap)
            }
        }
    }

    fun insertUserChannelStats(username: String, numChannels: Long, topTenChannelStats: List<ChannelInfo>) {
        println("Inserting channel stats for $username...")

        transaction {
            val userUUID = UsersTable
                .select { UsersTable.username eq username }
                .singleOrNull()
                ?.get(UsersTable.id)
                ?: throw Exception("User not found")

            ChannelsTable.deleteWhere { ChannelsTable.user_id eq userUUID }

            ChannelsTable.insert {
                it[id] = UUID.randomUUID()
                it[user_id] = userUUID
                it[num_channels] = numChannels
                it[top_ten_channels] = Json.encodeToString(topTenChannelStats)
            }
        }
    }
}
