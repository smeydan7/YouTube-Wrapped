package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import database.DatabaseFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.routing.*
import model.JWTConfig
import model.OAuthConfig
import com.example.plugins.configureSecurity
import com.example.plugins.configureRouting
import java.util.Properties
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import com.example.plugins.configureSerialization
import org.slf4j.LoggerFactory
import services.ActivityService
//import routes.top5VideosRoute
import routes.videoCount
//import routes.top10ChannelsRoute
//import routes.channelCount
//import routes.firstName
//import routes.lastName
import routes.activityRoutes
import routes.userId
import routes.apiRoutes
import routes.uploadRoute
//import routes.adsViewed
import routes.friendsRoutes
import routes.updateUserName
//import routes.monthFrequencyRoute
import routes.sendFriendRequest
import routes.getFriendRequests
import routes.getFriends
import repository.HttpBackendProvider
import routes.getConnections
import routes.userSummary
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

// Load auth.properties from the resources folder
private val props = Properties().apply {
    val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("auth.properties")
        ?: throw IllegalStateException("Could not load auth.properties")
    load(stream)
}

private val clientId = props.getProperty("GOOGLE_CLIENT_ID") ?: "MISSING_CLIENT_ID"
private val clientSecret = props.getProperty("GOOGLE_CLIENT_SECRET") ?: "MISSING_CLIENT_SECRET"

fun Application.module() {

    val client = HttpBackendProvider.client

    environment.monitor.subscribe(ApplicationStopped) {
        client.close()
    }

    val jwtConfig = JWTConfig(
        name = "jwt-auth",
        realm = "ktor-app",
        secret = "super_secret_key",
        audience = "ktor-users",
        issuer = "ktor-app",
        expirationSeconds = 3600
    )

    val oauthConfig = OAuthConfig(
        name = "google",
        clientId = clientId,
        clientSecret = clientSecret,
        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
        accessTokenUrl = "https://oauth2.googleapis.com/token",
        redirectUrl = "https://youtubewrapper-450406.uc.r.appspot.com/auth/callback",
        userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo",
        defaultScopes = listOf("openid", "email", "profile")
    )


    DatabaseFactory.init()

    routing {
        activityRoutes()
        configureSecurity(jwtConfig, oauthConfig, client)
        configureRouting(jwtConfig, oauthConfig)
        apiRoutes()
        userId()
        uploadRoute()
        friendsRoutes()
        updateUserName()
        sendFriendRequest()
        getFriendRequests()
        getFriends()
        getConnections()
        userSummary()
        videoCount()
    }


}