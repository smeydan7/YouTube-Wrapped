//import io.ktor.client.*
//import io.ktor.client.engine.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.request.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import kotlinx.coroutines.runBlocking
//import kotlinx.serialization.json.Json
//import userApiKit.fetchUserInfo
//import userApiKit.updateUserInfo
//import kotlin.test.*
//
//class ProfileHelperTest {
//
////    private lateinit var mockHandler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
////
////    private val testClient = HttpClient(MockEngine { request -> mockHandler(request) }) {
////        install(ContentNegotiation) {
////            json(Json { ignoreUnknownKeys = true })
////        }
////    }
//
//    @Test
//    fun `fetchUserInfo returns correct name`() = runBlocking {
//        mockHandler = { request ->
//            when (request.url.encodedPath) {
//                "/first-name/testuser" -> respond("Nivriti", HttpStatusCode.OK)
//                "/last-name/testuser" -> respond("Bajwa", HttpStatusCode.OK)
//                else -> respond("Not found", HttpStatusCode.NotFound)
//            }
//        }
//
//        val result = fetchUserInfo("testuser", client = testClient)
//
//        assertEquals("Nivriti", result.firstName)
//        assertEquals("Bajwa", result.lastName)
//    }
//
//    @Test
//    fun `updateUserInfo triggers success callback on 200`() = runBlocking {
//        var successCalled = false
//        var errorMessage: String? = null
//
//        mockHandler = {
//            assertEquals(HttpMethod.Put, it.method)
//            respond("", HttpStatusCode.OK)
//        }
//
//        updateUserInfo(
//            username = "testuser",
//            newFirstName = "Nivriti",
//            newLastName = "Bajwa",
//            onSuccess = { successCalled = true },
//            onError = { errorMessage = it },
//            client = testClient
//        )
//
//        assertTrue(successCalled)
//        assertNull(errorMessage)
//    }
//
//    @Test
//    fun `updateUserInfo triggers error callback on failure`() = runBlocking {
//        var successCalled = false
//        var errorMessage: String? = null
//
//        mockHandler = {
//            respond("Something went wrong", HttpStatusCode.BadRequest)
//        }
//
//        updateUserInfo(
//            username = "testuser",
//            newFirstName = "Nivriti",
//            newLastName = "Bajwa",
//            onSuccess = { successCalled = true },
//            onError = { errorMessage = it },
//            client = testClient
//        )
//
//        assertFalse(successCalled)
//        assertNotNull(errorMessage)
//        assertTrue(errorMessage!!.contains("Server error"))
//    }
//}