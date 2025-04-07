package routes
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import database.tables.UsersTable
import database.tables.FriendsTable
import database.tables.FriendRequestsTable

import java.util.UUID


fun Route.friendsRoutes() {
    route("/friends") {

        // all requests a user has
        get("/requests/{userId}") {
            val userIdParam = call.parameters["userId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId parameter"))

            val userId = try {
                UUID.fromString(userIdParam)
            } catch (e: IllegalArgumentException) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            }

            val requests = transaction {
                FriendRequestsTable
                    .select { FriendRequestsTable.receiverId eq userId }
                    .mapNotNull { row ->
                        val senderId = row[FriendRequestsTable.senderId]
                        UsersTable
                            .select { UsersTable.id eq senderId }
                            .map {
                                mapOf(
                                    "id" to it[UsersTable.id].toString(),
                                    "username" to it[UsersTable.username],
                                    "email" to it[UsersTable.email],
                                    "first_name" to it[UsersTable.first_name],
                                    "last_name" to it[UsersTable.last_name]
                                )
                            }.firstOrNull()
                    }
            }

            call.respond(requests)
        }

        //requests sent by *THIS* user
        get("/requested/{userId}") {
            val userIdParam = call.parameters["userId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId parameter"))

            val userId = try {
                UUID.fromString(userIdParam)
            } catch (e: IllegalArgumentException) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            }

            val outgoingRequests = transaction {
                FriendRequestsTable
                    .select { FriendRequestsTable.senderId eq userId }
                    .mapNotNull { row ->
                        val receiverId = row[FriendRequestsTable.receiverId]
                        UsersTable
                            .select { UsersTable.id eq receiverId }
                            .map {
                                mapOf(
                                    "id" to it[UsersTable.id].toString(),
                                    "username" to it[UsersTable.username],
                                    "email" to it[UsersTable.email],
                                    "first_name" to it[UsersTable.first_name],
                                    "last_name" to it[UsersTable.last_name]
                                )
                            }.firstOrNull()
                    }
            }

            call.respond(outgoingRequests)
        }

        // all friends a user has
        get("/all/{userId}") {
            val userId = call.parameters["userId"]?.let { UUID.fromString(it) }
                ?: return@get call.respond(mapOf("error" to "Missing or invalid userId"))

            val friends = transaction {
                FriendsTable
                    .select {
                        (FriendsTable.user1_Id eq userId) or (FriendsTable.user2_Id eq userId)
                    }
                    .mapNotNull { row ->
                        val friendId = if (row[FriendsTable.user1_Id] == userId) row[FriendsTable.user2_Id] else row[FriendsTable.user1_Id]
                        UsersTable
                            .select { UsersTable.id eq friendId }
                            .map {
                                mapOf(
                                    "id" to it[UsersTable.id].toString(),
                                    "username" to it[UsersTable.username],
                                    "email" to it[UsersTable.email],
                                    "first_name" to it[UsersTable.first_name],
                                    "last_name" to it[UsersTable.last_name]
                                )
                            }.firstOrNull()
                    }
            }

            call.respond(friends)
        }


        // send a friend request
        post("/requests/send/{senderId}/{receiverId}") {
            try {
                val senderId = call.parameters["senderId"]?.let { UUID.fromString(it) }
                val receiverId = call.parameters["receiverId"]?.let { UUID.fromString(it) }

                if (senderId == null || receiverId == null || senderId == receiverId) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid sender or receiver"))
                }

                val requestId = UUID.randomUUID()

                transaction {
                    FriendRequestsTable.insertIgnore {
                        it[id] = requestId
                        it[FriendRequestsTable.senderId] = senderId
                        it[FriendRequestsTable.receiverId] = receiverId
                    }
                }

                call.respond(mapOf("status" to "Friend request sent"))
            } catch (e: Exception) {
                println("Error sending friend request: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Something went wrong"))
            }
        }

        // accept a friend request
        post("/requests/accept/{senderId}/{receiverId}") {
            val senderId = call.parameters["senderId"]?.let { UUID.fromString(it) }
            val receiverId = call.parameters["receiverId"]?.let { UUID.fromString(it) }

            if (senderId == null || receiverId == null) {
                return@post call.respond(mapOf("error" to "Missing sender or receiver ID"))
            }

            transaction {
                val (user1, user2) = listOf(senderId, receiverId).sorted()

                FriendsTable.insertIgnore {
                    it[id] = UUID.randomUUID()
                    it[user1_Id] = user1
                    it[user2_Id] = user2
                }

                FriendRequestsTable.deleteWhere {
                    (FriendRequestsTable.senderId eq senderId) and (FriendRequestsTable.receiverId eq receiverId)
                }
            }

            call.respond(mapOf("status" to "Friend request accepted"))
        }

        // remove a friend (delete button on added friends)
        delete("/remove/{userId1}/{userId2}") {
            val userId1 = call.parameters["userId1"]?.let { UUID.fromString(it) }
            val userId2 = call.parameters["userId2"]?.let { UUID.fromString(it) }

            if (userId1 == null || userId2 == null) {
                return@delete call.respond(mapOf("error" to "Invalid user IDs"))
            }

            val (user1, user2) = listOf(userId1, userId2).sorted()

            transaction {
                FriendsTable.deleteWhere {
                    (FriendsTable.user1_Id eq user1) and (FriendsTable.user2_Id eq user2)
                }
            }

            call.respond(mapOf("status" to "Friend removed"))
        }

        // unsend a friend request (delete button on requested friends)
        delete("/requests/unsend/{senderId}/{receiverId}") {
            val senderId = try {
                UUID.fromString(call.parameters["senderId"])
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid sender ID"))
            }

            val receiverId = try {
                UUID.fromString(call.parameters["receiverId"])
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid receiver ID"))
            }

            val rowsDeleted = transaction {
                FriendRequestsTable.deleteWhere {
                    (FriendRequestsTable.senderId eq senderId) and (FriendRequestsTable.receiverId eq receiverId)
                }
            }

            if (rowsDeleted > 0) {
                call.respond(mapOf("status" to "Friend request unsent"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "No matching friend request found"))
            }
        }

        // delete a friend request
        delete("/requests/reject/{senderId}/{receiverId}") {
            val senderId = try {
                UUID.fromString(call.parameters["senderId"])
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid sender ID"))
            }

            val receiverId = try {
                UUID.fromString(call.parameters["receiverId"])
            } catch (e: Exception) {
                return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid receiver ID"))
            }

            val rowsDeleted = transaction {
                FriendRequestsTable.deleteWhere {
                    (FriendRequestsTable.senderId eq senderId) and (FriendRequestsTable.receiverId eq receiverId)
                }
            }

            if (rowsDeleted > 0) {
                call.respond(mapOf("status" to "Friend request rejected"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "No matching friend request found"))
            }
        }
    }
}