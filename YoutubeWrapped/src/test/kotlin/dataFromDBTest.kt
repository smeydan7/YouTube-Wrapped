import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import models.Video

class FunctionsTest {

    @Test
    fun testTopVideo() {
        val videos = listOf(
            Video("first", "hello, im first", 500),
            Video("second", "not first", 300)
        )

        val topVideo = videos.firstOrNull()

        assertNotNull(topVideo)
        assertEquals("first", topVideo?.title)
    }

    @Test
    fun testMonthFrequency() {
        val months = mapOf(
            "Jan" to 100,
            "Feb" to 20000,
            "March" to 500
        )

        assertEquals(3, months.size)
        assertTrue(months["Feb"]!! > months["March"]!!)
    }
}
