//this will have YouTube stats endpoints

package routes
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import database.tables.UsersTable
import database.tables.VideosTable
import database.tables.ChannelsTable
import database.tables.FriendRequestsTable
import database.tables.FriendsTable
import java.util.UUID

fun Route.statsRoutes() {}

fun Route.testDatabaseRoute() {
    route("/test-db") {
        get {
            val username = transaction {
                UsersTable.selectAll()
                    .firstOrNull()
                    ?.get(UsersTable.username)
            }
            val userCount = transaction {
                UsersTable.selectAll().count()
            }
            val videoCount = transaction {
                VideosTable.selectAll().count()
            }
            val channelCount = transaction {
                ChannelsTable.selectAll().count()
            }

            call.respondText("Database is working! First User: $username \nUser count: $userCount, Video count: $videoCount, Channel count: $channelCount")
        }
    }
}

fun Route.addTestUserRoute() {
    route("/add-test-user") {
        get {
            val testName = "testUser_${System.currentTimeMillis()}"
            transaction {
                UsersTable.insert {
                    it[id] = UUID.randomUUID()
                    it[username] = testName
                    it[email] = testName + "@gmail.com"
                    it[first_name] = "testUser"
                    it[last_name] = "${System.currentTimeMillis()}"
                }
            }
            call.respondText("User $testName created")
        }
    }
}

fun Route.getAllUsersRoute() {
    route("/users") {
        get {
            val users = transaction {
                UsersTable.selectAll().map {
                    row -> mapOf(
                        "id" to row[UsersTable.id].toString(),
                        "username" to row[UsersTable.username],
                        "email" to row[UsersTable.email],
                        "first_name" to row[UsersTable.first_name],
                        "lastname" to row[UsersTable.last_name]
                    )
                }
            }
            call.respond(users)
        }
    }
}

fun Route.sendFriendRequest() {
    post("/friendrequest/{sender}/to/{receiver}") {
        val senderUsername = call.parameters["sender"]
            ?: return@post call.respond("No sender username provided.")
        val receiverUsername = call.parameters["receiver"]
            ?: return@post call.respond("No receiver username provided.")

        val senderId: UUID? = transaction {
            UsersTable.select { UsersTable.username eq senderUsername }
                .singleOrNull()?.get(UsersTable.id)
        }
        if (senderId == null) {
            return@post call.respond("Sender '$senderUsername' not found.")
        }

        val receiverId: UUID? = transaction {
            UsersTable.select { UsersTable.username eq receiverUsername }
                .singleOrNull()?.get(UsersTable.id)
        }
        if (receiverId == null) {
            return@post call.respond("Receiver '$receiverUsername' not found.")
        }

        transaction {
            FriendRequestsTable.insert { row ->
                row[FriendRequestsTable.id] = UUID.randomUUID()
                row[FriendRequestsTable.senderId] = senderId
                row[FriendRequestsTable.receiverId] = receiverId
            }
        }

        call.respond("Friend request sent from $senderUsername to $receiverUsername.")
    }
}

fun Route.getFriendRequests() {
    get("/friendrequests/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respondText("No username provided.", status = HttpStatusCode.BadRequest)

        val userId: UUID = transaction {
            UsersTable.select { UsersTable.username eq username }
                .singleOrNull()?.get(UsersTable.id)
                ?: return@transaction null
        } ?: return@get call.respondText("User '$username' not found.", status = HttpStatusCode.BadRequest)

        val friendUsernames: Set<String> = transaction {
            FriendRequestsTable.select { FriendRequestsTable.senderId eq userId }
                .mapNotNull { row ->
                    val receiverId = row[FriendRequestsTable.receiverId]
                    UsersTable.select { UsersTable.id eq receiverId }
                        .singleOrNull()?.get(UsersTable.username)
                }.toSet()
        }
        call.respond(friendUsernames)
    }
}

fun Route.getFriends() {
    get("/friends/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respondText("No username provided.", status = HttpStatusCode.BadRequest)

        // Get the current user's ID
        val userId: UUID = transaction {
            UsersTable.select { UsersTable.username eq username }
                .singleOrNull()?.get(UsersTable.id)
                ?: return@transaction null
        } ?: return@get call.respondText("User '$username' not found.", status = HttpStatusCode.BadRequest)

        // Retrieve friend usernames for relationships where the user is involved
        val friendUsernames: Set<String> = transaction {
            FriendsTable.select { (FriendsTable.user1_Id eq userId) or (FriendsTable.user2_Id eq userId) }
                .mapNotNull { row ->
                    val friendId = if (row[FriendsTable.user1_Id] == userId) row[FriendsTable.user2_Id] else row[FriendsTable.user1_Id]
                    UsersTable.select { UsersTable.id eq friendId }
                        .singleOrNull()?.get(UsersTable.username)
                }.toSet()
        }

        call.respond(friendUsernames)
    }
}
fun Route.getConnections() {
    get("/connections/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respondText("No username provided.", status = HttpStatusCode.BadRequest)

        val userId: UUID = transaction {
            UsersTable.select { UsersTable.username eq username }
                .singleOrNull()?.get(UsersTable.id)
                ?: return@transaction null
        } ?: return@get call.respondText("User '$username' not found.", status = HttpStatusCode.BadRequest)

        // Fetch sent friend requests
        val requested: Set<String> = transaction {
            FriendRequestsTable.select { FriendRequestsTable.senderId eq userId }
                .mapNotNull { row ->
                    val receiverId = row[FriendRequestsTable.receiverId]
                    UsersTable.select { UsersTable.id eq receiverId }
                        .singleOrNull()?.get(UsersTable.username)
                }.toSet()
        }

        // Fetch current friends
        val friends: Set<String> = transaction {
            FriendsTable.select {
                (FriendsTable.user1_Id eq userId) or (FriendsTable.user2_Id eq userId)
            }.mapNotNull { row ->
                val friendId = if (row[FriendsTable.user1_Id] == userId)
                    row[FriendsTable.user2_Id]
                else
                    row[FriendsTable.user1_Id]

                UsersTable.select { UsersTable.id eq friendId }
                    .singleOrNull()?.get(UsersTable.username)
            }.toSet()
        }

        call.respond(
            mapOf(
                "requested" to requested,
                "friends" to friends
            )
        )
    }
}