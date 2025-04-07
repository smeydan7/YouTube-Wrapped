package routes
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import database.tables.UsersTable
import database.tables.VideosTable
import database.tables.ChannelsTable
import java.util.UUID
import io.ktor.server.request.receive

fun Route.firstName() {
    // GET
    get("/first-name/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respond("No username provided.")

        // from username get first name
        val firstName: String? = transaction {
            UsersTable
                .select { UsersTable.username eq username }
                .singleOrNull()
                ?.get(UsersTable.first_name)
        }

        // if the user is not in db
        if (firstName == null) {
            return@get call.respond("First name not found for user '$username'.")
        } else {
            call.respond(firstName)
        }
    }
}
fun Route.lastName() {
    // GET
    get("/last-name/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respond("No username provided.")

        // from username get last name
        val lastName: String? = transaction {
            UsersTable
                .select { UsersTable.username eq username }
                .singleOrNull()
                ?.get(UsersTable.last_name)
        }

        // if the user is not in db
        if (lastName == null) {
            return@get call.respond("Last name not found for user '$username'.")
        } else {
            call.respond(lastName)
        }
    }
}

fun Route.userId() {
    get("/user-id/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respond("No username provided.")

        val userId: UUID? = transaction {
            UsersTable
                .select { UsersTable.username eq username }
                .singleOrNull()
                ?.get(UsersTable.id)
        }

        if (userId == null) {
            return@get call.respond("User ID not found for username '$username'.")
        } else {
            call.respond(mapOf("user_id" to userId.toString()))
        }
    }
}
@kotlinx.serialization.Serializable
data class UpdateUserNameRequest(
    val firstName: String? = null,
    val lastName: String? = null
)

fun Route.updateUserName() {
    put("/update-user/{username}") {
        val usernameParam = call.parameters["username"]
            ?: return@put call.respond("No username in path.")

        val updateRequest = call.receive<UpdateUserNameRequest>()

        val rowCount = transaction {
            UsersTable.update(
                where = { UsersTable.username eq usernameParam }
            ) {
                if (updateRequest.firstName != null) {
                    it[first_name] = updateRequest.firstName
                }
                if (updateRequest.lastName != null) {
                    it[last_name] = updateRequest.lastName
                }
            }
        }

        if (rowCount == 0) {
            call.respond("No user found with username $usernameParam.")
        } else {
            call.respond("User updated successfully.")
        }
    }
}