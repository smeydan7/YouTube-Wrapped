import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ui.functions.extractChannelId
import ui.functions.extractVideoId

class dashboardFunctionsTest {

    @Test
    fun testExtractId() {
        val url = "https://www.youtube.com/watch?v=nima_nima_nima"
        val videoId = extractVideoId(url)
        assertEquals("nima_nima_nima", videoId)
    }

    @Test
    fun testExtractIdAgain() {
        val url = "https://www.youtube.com/watch"
        val videoId = extractVideoId(url)
        assertEquals("", videoId)
    }

    @Test
    fun testExtractChannelId() {
        val url = "https://www.youtube.com/channel/nima"
        val channelId = extractChannelId(url)
        assertEquals("nima", channelId)
    }

    @Test
    fun testExtractChannelIdAgain() {
        val url = "https://www.youtube.com/user/username"
        val channelId = extractChannelId(url)
        assertEquals("", channelId)
    }
}
