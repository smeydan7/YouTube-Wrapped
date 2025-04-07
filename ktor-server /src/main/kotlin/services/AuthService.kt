package services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.JWTConfig
import java.util.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import repository.HttpBackendProvider

val client = HttpBackendProvider.client

@Serializable
data  class TokenResponse(
    val access_token: String,
    val id_token: String? = null,
    val refresh_token: String? = null,
    val expires_in: Int
)


// Exchange Authorization Code for Google Access Token
suspend fun exchangeCodeForToken(authCode: String, clientId: String, clientSecret: String, redirectUri: String): TokenResponse {
    val response: String = client.post("https://oauth2.googleapis.com/token") {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
            listOf(
                "code" to authCode,
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "redirect_uri" to redirectUri,
                "grant_type" to "authorization_code"
            ).formUrlEncode()
        )
    }.bodyAsText()

    val json = Json.parseToJsonElement(response).jsonObject

    return TokenResponse(
        access_token = json["access_token"]?.jsonPrimitive?.content ?: throw Exception("No access token found"),
        id_token = json["id_token"]?.jsonPrimitive?.content,
        refresh_token = json["refresh_token"]?.jsonPrimitive?.content,
        expires_in = json["expires_in"]?.jsonPrimitive?.content?.toInt() ?: 3600 // Default to 1 hour
    )
}

// generate JWT with Google Access Token Embedded
fun generateJWT(jwtConfig: JWTConfig, googleAccessToken: String): String {
    val algorithm = Algorithm.HMAC256(jwtConfig.secret)
    return JWT.create()
        .withIssuer(jwtConfig.issuer)
        .withAudience(jwtConfig.audience)
        .withClaim("google_access_token", googleAccessToken)  // Store Google token in JWT
        .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.expirationSeconds * 1000))
        .sign(algorithm)
}

@Serializable
data class GoogleUser(
    val sub: String,
    val name: String? = null,
    @SerialName("given_name") val givenName: String? = null,
    @SerialName("family_name") val familyName: String? = null,
    val picture: String? = null,
    val email: String,
    @SerialName("email_verified") val emailVerified: Boolean,
    val locale: String? = null
)

suspend fun getGoogleUserInfo(googleAccessToken: String): GoogleUser {
    val response: HttpResponse = client.get("https://openidconnect.googleapis.com/v1/userinfo") {
        header("Authorization", "Bearer $googleAccessToken")
    }
    val responseText = response.bodyAsText()
    return Json.decodeFromString(GoogleUser.serializer(), responseText)
}