package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import services.ActivityService

fun Application.activityRoutes() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        val service = ActivityService()

        get("/activities") {
//            val filtered = service.getFilteredActivities()
//            call.respond(filtered) // Ensure this is inside routing { }
        }
    }
}