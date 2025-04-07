import services.ActivityService
import models.YouTubeActivity
import models.Subtitle
import database.repository.StatsRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Month
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.mockk.*

class ActivityServiceTest {

    private val service = ActivityService()
    private val testUsername = "testuser"

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `getFilteredActivities filters ads and returns stats`(@TempDir tempDir: File) {
        val sampleJson = """
            [
                {
                    "title": "Watched Video 1",
                    "titleUrl": "http://youtube.com/video1",
                    "time": "2023-08-01T12:00:00Z",
                    "details": [{"name": "From Google Ads"}],
                    "subtitles": [{"name": "Channel 1", "url": "http://youtube.com/channel1"}]
                },
                {
                    "title": "Watched Video 2",
                    "titleUrl": "http://youtube.com/video2",
                    "time": "2023-08-02T13:00:00Z",
                    "details": [{"name": "Organic View"}],
                    "subtitles": [{"name": "Channel 2", "url": "http://youtube.com/channel2"}]
                }
            ]
        """.trimIndent()

        val testFile = File(tempDir, "watch-history.json")
        testFile.writeText(sampleJson)

        mockkObject(StatsRepository)
        every { StatsRepository.insertUserVideoStats(any(), any(), any(), any(), any()) } just Runs
        every { StatsRepository.insertUserChannelStats(any(), any(), any()) } just Runs

        val result = service.getFilteredActivities(testFile.absolutePath, "testuser")

        assertEquals(1, result.size)
        assertEquals("Watched Video 2", result[0].title)

        verify {
            StatsRepository.insertUserVideoStats(
                username = "testuser",
                numVideos = 1L,
                topFiveVideoStats = any(),
                numAds = 1L,
                monthCount = match { it[Month.AUGUST] == 1 }
            )

            StatsRepository.insertUserChannelStats(
                username = "testuser",
                numChannels = 1L,
                topTenChannelStats = any()
            )
        }

        unmockkAll()
    }

    @Test
    fun `getFilteredActivities returns empty list for missing file`() {
        val result = service.getFilteredActivities("nonexistent_file.json", testUsername)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getFilteredActivities handles invalid JSON`() {
        val tempFile = File(tempDir, "invalid.json")
        tempFile.writeText("invalid json")

        val result = service.getFilteredActivities(tempFile.absolutePath, testUsername)
        assertTrue(result.isEmpty())

        tempFile.delete()
    }
}