package userApiKit

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import models.Channel

@kotlinx.serialization.Serializable
data class UpdateUserNameRequest(
    val firstName: String? = null,
    val lastName: String? = null
)
suspend fun updateUserInfo(
    username: String,
    newFirstName: String,
    newLastName: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    client: HttpClient = HttpProvider.client
) {
    try {
        val url = "https://youtubewrapper-450406.uc.r.appspot.com/update-user/$username"
        val requestBody = UpdateUserNameRequest(
            firstName = newFirstName.ifBlank { null },
            lastName = newLastName.ifBlank { null }
        )

        val response: HttpResponse = client.put(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        if (response.status == HttpStatusCode.OK) {
            onSuccess()
        } else {
            val errorText = response.bodyAsText()
            onError("Server error: ${response.status}, details: $errorText")
        }
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}
suspend fun fetchUserInfo(username: String, client: HttpClient = HttpProvider.client): UpdateUserNameRequest {
    return try {
        val response: HttpResponse = client.get("https://youtubewrapper-450406.uc.r.appspot.com/user-summary/$username")
        if (response.status == HttpStatusCode.OK) {
            val responseBody = response.bodyAsText()
            val json = Json { ignoreUnknownKeys = true }
            val jsonElement = json.decodeFromString<JsonElement>(responseBody)
            val userJson = jsonElement.jsonObject["user"]?.jsonObject
            val firstName = userJson?.get("first_name")?.jsonPrimitive?.contentOrNull
            val lastName = userJson?.get("last_name")?.jsonPrimitive?.contentOrNull
            UpdateUserNameRequest(firstName, lastName)
        } else {
            UpdateUserNameRequest(null, null)
        }
    } catch (e: Exception) {
        println("Error in fetchProfileInfo: ${e.message}")
        UpdateUserNameRequest(null, null)
    }
}