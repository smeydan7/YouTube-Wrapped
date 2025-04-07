
import com.example.youtubewrapped.ui.screens.storeFile
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadScreenStoreFileTest {

    private lateinit var testClient: HttpClient

    @Test
    fun `storeFile returns success message on 200 OK`() = runBlocking {
        val mockEngine = MockEngine { request ->
            assertEquals("https://youtubewrapper-450406.uc.r.appspot.com/upload", request.url.toString())
            respond("Upload successful!", HttpStatusCode.OK)
        }

        testClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val result = storeFile(
            fileBytes = "dummy content".toByteArray(),
            filename = "watch_history.json",
            username = "testuser",
            client = testClient
        )

        assertEquals("Upload successful!", result)
    }

    @Test
    fun `storeFile returns failure message on non-200 status`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond("Error", HttpStatusCode.BadRequest)
        }

        testClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val result = storeFile(
            fileBytes = "bad content".toByteArray(),
            filename = "watch_history.json",
            username = "testuser",
            client = testClient
        )

        assertTrue(result.startsWith("Upload failed:"))
        assertTrue(result.contains("400"))
    }
}