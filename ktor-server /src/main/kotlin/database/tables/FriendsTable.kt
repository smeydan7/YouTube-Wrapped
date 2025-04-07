package database.tables

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.ReferenceOption

object FriendsTable : Table("friends") {
    val id = uuid("id").uniqueIndex()
    val user1_Id = uuid("user1_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val user2_Id = uuid("user2_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(user1_Id, user2_Id)
         check("user1_lt_user2") { user1_Id.less(user2_Id) }
    }

    override val primaryKey = PrimaryKey(id, user1_Id, user2_Id)
}
