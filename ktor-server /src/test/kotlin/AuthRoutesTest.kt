package test

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class AuthRoutesTest {

    @Test // checks that google login redirects to the correct URL
    fun testGoogleLoginRedirect() = testApplication {
        val response = client.get("/auth/google-login")

        assertEquals(HttpStatusCode.Found, response.status)
        assertTrue(response.headers["Location"]!!.startsWith("https://accounts.google.com/o/oauth2/auth"))
    }

    @Test // JWT should be required for all protected routes
    fun testProtectedRouteWithoutJWT() = testApplication {
        val response = client.get("/auth/protected")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}