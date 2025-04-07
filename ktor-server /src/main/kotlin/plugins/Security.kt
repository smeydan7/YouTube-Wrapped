package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import model.JWTConfig
import model.OAuthConfig
import java.util.*

fun Application.configureSecurity(
    jwtConfig: JWTConfig,
    oauthConfig: OAuthConfig,
    httpClient: HttpClient
) {
    authentication {
        // JWT Authentication
        jwt(jwtConfig.name) {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired token.")
            }
        }

        // OAuth2 Authentication
        oauth(oauthConfig.name) {
            urlProvider = { oauthConfig.redirectUrl }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = oauthConfig.name,
                    authorizeUrl = oauthConfig.authorizeUrl,
                    accessTokenUrl = oauthConfig.accessTokenUrl,
                    requestMethod = HttpMethod.Post,
                    clientId = oauthConfig.clientId,
                    clientSecret = oauthConfig.clientSecret,
                    defaultScopes = oauthConfig.defaultScopes
                )
            }
            client = httpClient
        }
    }
}