package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import database.tables.ChannelsTable
import database.tables.UsersTable
import database.tables.VideosTable
import database.tables.FriendsTable
import database.tables.FriendRequestsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Properties

object DatabaseFactory {

    private val props = Properties().apply {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("database.properties")
            ?: throw IllegalStateException("Could not load database.properties")
        load(stream)
    }

    private val dbUsername = props.getProperty("db.username")
    private val dbPassword = props.getProperty("db.password")
    private val dbJdbcUrl = props.getProperty("db.jdbcUrl")


    fun init() {
        println("INITIALIZING DATABASE")

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = dbJdbcUrl
            username = dbUsername
            password = dbPassword
            addDataSourceProperty("prepareThreshold", "0")
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 40
            minimumIdle = 10
            connectionTimeout = 30000L
            idleTimeout = 300000L
            maxLifetime = 1800000L
            leakDetectionThreshold = 2000L
            isAutoCommit = false
        }
        val dataSource = HikariDataSource(hikariConfig)
        println("Active connections: ${dataSource.hikariPoolMXBean.activeConnections}")
        println("Idle connections: ${dataSource.hikariPoolMXBean.idleConnections}")
        println("Total connections: ${dataSource.hikariPoolMXBean.totalConnections}")
        Database.connect(dataSource)

        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down DB pool")
            dataSource.close()
        })
        try {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                ChannelsTable,
                VideosTable,
                FriendRequestsTable,
                FriendsTable
            )
        } catch (e: Exception) {
            println("Schema creation warning: ${e.message}")
        }
        println("Database connected and schema created successfully.")
    }

}