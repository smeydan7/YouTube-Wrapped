package database.tables

import org.jetbrains.exposed.sql.Table

object VideosTable : Table("videoswatched") {
    val id = uuid("id").uniqueIndex()
    val user_id = uuid("user_id").references(UsersTable.id)
    val num_videos = long("num_videos")
    val top_five_titles = text("top_five_titles")
    val num_ads_viewed = long("num_ads_viewed")
    val month_frequency = text("month_frequency")

    override val primaryKey = PrimaryKey(id)
}
