package database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object ChannelsTable : Table("channels") {
    val id = uuid("id").uniqueIndex()
    val user_id = uuid("user_id").references(UsersTable.id)
    val num_channels = long("num_channels")
    val top_ten_channels = text("top_ten_channels")

    override val primaryKey = PrimaryKey(id)
}
