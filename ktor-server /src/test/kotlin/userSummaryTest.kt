package routes

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserSummaryRouteTest {

    @Test
    fun testUserSummaryRoute() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                userSummary()
            }
        }

        val username = "ashnoor697"

        val response: HttpResponse = client.get("/user-summary/$username")

        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody: String = response.bodyAsText()
        println("Response: $responseBody")

        assertTrue(responseBody.contains("user"))
    }
}