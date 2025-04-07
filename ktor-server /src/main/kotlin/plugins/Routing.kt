package com.example.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import routes.testDatabaseRoute
import routes.addTestUserRoute
import routes.getAllUsersRoute
import model.JWTConfig
import model.OAuthConfig
import routes.authRoutes

fun Application.configureRouting(jwtConfig: JWTConfig, oauthConfig: OAuthConfig) {
    routing {
        authRoutes(jwtConfig, oauthConfig)
        get("/") {
            call.respondText("Hello World!")
        }
        testDatabaseRoute()
        addTestUserRoute()
        getAllUsersRoute()
    }
}
