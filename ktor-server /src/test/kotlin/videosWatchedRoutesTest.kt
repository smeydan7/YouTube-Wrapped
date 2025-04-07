//package com.example.test
//
//import io.ktor.server.application.*
//import io.ktor.server.testing.*
//import io.ktor.http.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.ktor.server.routing.*
//import kotlin.test.*
//
//import routes.top5VideosRoute
//import routes.videoCount
//
//class VideosRoutesTest {
//    private fun Application.testModule() {
//        //listing routes that I am testing here
//        routing {
//            top5VideosRoute()
//            videoCount()
//        }
//    }
//
//    //username DNE
//    @Test
//    fun UsernameDNE() = testApplication {
//        application {
//            testModule()
//        }
//        val response = client.get("/top-5-videos/jeff")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("User with username 'jeff' not found."))
//    }
//
//    //same for VideoCount
//    @Test
//    fun videoCountUsernameDNE() = testApplication {
//        application {
//            testModule()
//        }
//        val response = client.get("/video-count/jeff")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("User with username 'jeff' not found."))
//    }
//}