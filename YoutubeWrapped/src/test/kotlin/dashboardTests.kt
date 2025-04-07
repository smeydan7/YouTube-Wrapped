import kotlin.test.Test
import kotlin.test.assertEquals
import userApiKit.DashboardData

class DashboardTests {

    @Test
    fun testFields() {
        val data = DashboardData(
            username = "nimaarfeen",
            firstName = "Nima",
            videoCount = 100000,
            adsCount = 5000,
            channelsCount = 3000,
            topVideos = emptyList(),
            topChannels = emptyList(),
            topVideo = null,
            topChannel = null,
            topVideoThumbnails = emptyMap(),
            topChannelThumbnails = emptyMap(),
            monthFrequency = mapOf("Jan" to 1000, "Feb" to 500, "March" to 1000)
        )

        assertEquals("Nima", data.firstName)
        assertEquals(100000, data.videoCount)
        assertEquals(3, data.monthFrequency.size)
    }

    @Test
    fun testMonths() {
        val months = listOf(
            "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        )

        assertEquals(12, months.size)
        assertEquals("JANUARY", months.first())
        assertEquals("DECEMBER", months.last())
    }
}
