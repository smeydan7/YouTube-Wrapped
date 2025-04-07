//package com.example.test
//
//import io.ktor.server.application.*
//import io.ktor.server.testing.*
//import io.ktor.http.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import kotlin.test.*
//import io.ktor.server.routing.*
//
//import routes.firstName
//import routes.userId
//import routes.updateUserName
//import routes.lastName
//
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.encodeToString
//import routes.UpdateUserNameRequest
//
//
//
//
//class UsersRoutesTest {
//
//    private fun Application.testModule() {
//        //listing routes that we have created and need testing
//        routing {
//            firstName()
//            lastName()
//            userId()
//            updateUserName()
//        }
//    }
//
//    //test if username DNE
//    @Test
//    fun usernameDNE() = testApplication {
//        application {
//            testModule()
//        }
//        val response = client.get("/first-name/jeff")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("First name not found"))
//    }
//
//    //same for last name function
//    @Test
//    fun usernameToLastNameDNE() = testApplication {
//        application {
//            testModule()
//        }
//        val response = client.get("/last-name/jeff")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("Last name not found"))
//    }
//    //for userID
//    @Test
//    fun userIdDNE() = testApplication {
//        application {
//            testModule()
//        }
//        val response = client.get("/user-id/jeff")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("ID not found"))
//
//    }
//
//    //for updateUser
//    @Test
//    fun updateUserDNE() = testApplication {
//        application { testModule() }
//
//        val requestBody = Json.encodeToString(
//            UpdateUserNameRequest(firstName = "nima")
//        )
//
//        val response = client.put("/update-user/jeff") {
//            contentType(ContentType.Application.Json)
//            setBody(requestBody)
//        }
//
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("User Not found - cant update"))
//    }
//
//
//}
//