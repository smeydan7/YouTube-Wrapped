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
//import routes.channelCount
//import routes.top10ChannelsRoute
//
//class ChannelsRoutesTest {
//
//    private fun Application.testModule() {
//        //listing routes that I am testing here
//        routing {
//            channelCount()
//            top10ChannelsRoute()
//        }
//    }
//
//    //username DNE
//    @Test
//    fun UsernameDNE() = testApplication {
//        application {
//            testModule()
//        }
//        val response = client.get("/channel-count/jeff")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("User with username 'jeff' not found."))
//    }
//
//    //same for top 10 channels function
//    @Test
//    fun top10UsernameDNE() = testApplication {
//        application {
//            testModule()
//        }
//
//        val response = client.get("/top-10-channels/jeff")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertTrue(response.bodyAsText().contains("User with username 'jeff' not found."))
//    }
//}