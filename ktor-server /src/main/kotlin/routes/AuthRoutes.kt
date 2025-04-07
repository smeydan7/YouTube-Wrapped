package routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.JWTConfig
import model.OAuthConfig
import services.exchangeCodeForToken
import services.generateJWT
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import services.getGoogleUserInfo
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.insert
import database.tables.UsersTable
import java.util.UUID

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String
)

fun Route.authRoutes(jwtConfig: JWTConfig, oauthConfig: OAuthConfig) {
    route("/auth") {
        // Redirect User to Google OAutha
        get("/google-login") {
            val googleAuthUrl = "${oauthConfig.authorizeUrl}?" +
                    "client_id=${oauthConfig.clientId}&" +
                    "redirect_uri=${oauthConfig.redirectUrl}&" +
                    "response_type=code&" +
                    "scope=${oauthConfig.defaultScopes.joinToString("%20")}&" +
                    "access_type=offline"
            call.respondRedirect(googleAuthUrl)
        }

        // Handle OAuth Callback & Generate JWT
        get("/callback") {
            println("Received GET /auth/callback request")
            val authCode = call.request.queryParameters["code"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing authorization code")

            try {
                val tokenResponse = exchangeCodeForToken(
                    authCode,
                    oauthConfig.clientId,
                    oauthConfig.clientSecret,
                    oauthConfig.redirectUrl
                )

                val accessToken = tokenResponse.access_token
                val googleUser = getGoogleUserInfo(accessToken)
                val user = User(
                    firstName = googleUser.givenName ?: "Unknown",
                    lastName = googleUser.familyName ?: "Unknown",
                    email = googleUser.email,
                    username = googleUser.email.substringBefore("@")
                )

                val existingUserRow = transaction {
                    UsersTable
                        .select { UsersTable.email eq user.email }
                        .singleOrNull()
                }

                val userId = if (existingUserRow == null) {
                    println("User not found. Creating new user in the DB.")
                    transaction {
                        UsersTable.insert { row ->
                            row[UsersTable.id] = UUID.randomUUID()
                            row[UsersTable.username] = user.username
                            row[UsersTable.email] = user.email
                            row[UsersTable.first_name] = user.firstName
                            row[UsersTable.last_name] = user.lastName
                        } get UsersTable.id
                    }
                } else {
                    println("User with email ${user.email} already exists in DB.")
                    existingUserRow[UsersTable.id]
                }

                val jwtToken = generateJWT(jwtConfig, accessToken)
                val frontendRedirectUrl = "http://localhost:8081/callback?token=${URLEncoder.encode(jwtToken, StandardCharsets.UTF_8.toString())}"

                call.respondText(
                    """
                    <html>
                    <body>
                        <h2>Login Successful!</h2>
                        <p>You can close this window now.</p>
                        <script>
                            window.location.href = "$frontendRedirectUrl"; 
                        </script>
                    </body>
                    </html>
                    """.trimIndent(),
                    ContentType.Text.Html
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to exchange token: ${e.message}")
            }
        }

        // Protect Routes with JWT
        authenticate(jwtConfig.name) {
            get("/protected") {
                val principal = call.principal<JWTPrincipal>()
                val googleAccessToken = principal?.payload?.getClaim("google_access_token")?.asString()
                call.respondText("Google Access Token: $googleAccessToken")
            }
        }
    }
}