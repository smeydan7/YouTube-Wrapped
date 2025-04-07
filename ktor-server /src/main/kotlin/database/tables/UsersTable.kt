package database.tables

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id").uniqueIndex()
    val username = varchar("username", 255).uniqueIndex()
    val email = text("email").uniqueIndex()
    val first_name = text("first_name")
    val last_name = text("last_name")

    override val primaryKey = PrimaryKey(id)
}
