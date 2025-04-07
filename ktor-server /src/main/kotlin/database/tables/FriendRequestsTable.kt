package database.tables

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.ReferenceOption

object FriendRequestsTable : Table("friendrequests") {
    val id = uuid("id").uniqueIndex()
    val senderId = uuid("sender_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val receiverId = uuid("receiver_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(senderId, receiverId)
        check("sender_not_receiver") { senderId neq receiverId }
    }

    override val primaryKey = PrimaryKey(id)
}
