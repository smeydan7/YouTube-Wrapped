package userApiKit

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val email: String,
    val first_name: String,
    val lastname: String
)

suspend fun fetchAllUsers(username: String, client: HttpClient = HttpProvider.client): List<User> {
    println("Starting user fetch...")
    return try {
        println("Before response")
        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/users")
        println("After response")
        if (response.status == HttpStatusCode.OK) {
            response.body<List<User>>().filter { it.username != username }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun sendFriendRequest(sender: String, receiver: String, client: HttpClient = HttpProvider.client): Boolean {
    println("Sending friend request from $sender to $receiver")
    return try {
        val response: HttpResponse = client.post("https://youtubewrapper-450406.uc.r.appspot.com/friendrequest/${sender}/to/${receiver}") {
            contentType(ContentType.Application.Json)
        }
        if (response.status == HttpStatusCode.OK) {
            println("Friend request sent successfully")
            true
        } else {
            println("Failed to send friend request. Status: ${response.status}")
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


suspend fun fetchConnections(username: String,  client: HttpClient = HttpProvider.client): Map<String, Set<String>> {
    val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/connections/$username")
    return response.body()
}