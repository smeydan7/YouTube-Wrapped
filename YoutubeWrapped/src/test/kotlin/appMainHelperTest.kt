
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertEquals
import userApiKit.HttpProvider
import userApiKit.fetchUsernameAndUserId
import kotlin.test.Test
import kotlin.test.assertNull

class AppMainHelperTest {

    private fun withMockClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
        testBody: suspend () -> Unit
    ) = runBlocking {
        val originalClient = HttpProvider.client

        HttpProvider.client = HttpClient(MockEngine { handler(it) }) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        try {
            testBody()
        } finally {
            HttpProvider.client = originalClient
        }
    }

    @Test
    fun `returns nulls when username API fails`() = withMockClient({
        respond("Unauthorized", HttpStatusCode.Unauthorized)
    }) {
        val (username, userId) = fetchUsernameAndUserId("invalid-token")
        assertNull(username)
        assertNull(userId)
    }

    @Test
    fun `returns nulls when username is missing in response`() = withMockClient({
        respond("{}", HttpStatusCode.OK) // No "username"
    }) {
        val (username, userId) = fetchUsernameAndUserId("token")
        assertNull(username)
        assertNull(userId)
    }

    @Test
    fun `returns nulls if userId lookup fails`() = withMockClient({ request ->
        when (request.url.encodedPath) {
            "/api/username" -> {
                val jsonResponse = buildJsonObject { put("username", "nivriti") }.toString()
                respond(jsonResponse, HttpStatusCode.OK)
            }
            "/user-id/nivriti" -> {
                respond("Server error", HttpStatusCode.InternalServerError)
            }
            else -> respond("Not found", HttpStatusCode.NotFound)
        }
    }) {
        val (username, userId) = fetchUsernameAndUserId("valid-token")
        assertNull(username)
        assertNull(userId)
    }

    @Test
    fun `returns username and userId when both requests succeed`() = withMockClient({ request ->
        when (request.url.encodedPath) {
            "/api/username" -> {
                val jsonResponse = buildJsonObject { put("username", "nivriti") }.toString()
                respond(jsonResponse, HttpStatusCode.OK)
            }
            "/user-id/nivriti" -> {
                val jsonResponse = buildJsonObject { put("user_id", "12345") }.toString()
                respond(jsonResponse, HttpStatusCode.OK)
            }
            else -> respond("Not found", HttpStatusCode.NotFound)
        }
    }) {
        val (username, userId) = fetchUsernameAndUserId("dummy-token")
        assertEquals("nivriti", username)
        assertEquals("12345", userId)
    }
}