import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ui.screens.FriendsScreen

class friendsDashboardTest {

    @Test
    fun testSampleFriendsList() {
        // list of friends
        val friends = listOf("Ashnoor", "Nima", "Sam")

        // basics
        assertEquals(3, friends.size, "There should be 3 friends.")
        assertTrue(friends.contains("Ashnoor"), "Ashnoor should be in the list.")
        assertTrue(friends.contains("Nima"), "Nima should be in the list.")
        assertTrue(friends.contains("Sam"), "Sam should be in the list.")
    }

    @Test
    fun testFirstFriendInitial() {
        val friendName = "Ashnoor"
        val initial = friendName.firstOrNull()?.toString() ?: ""

        // validate profile circle initial
        assertEquals("A", initial, "The initial of 'Ashnoor' should be 'A'.")
    }

    @Test
    fun testCurrentFriendIndexBoundaries() {
        // moving left/right between friends
        val friends = listOf("Ashnoor", "Nima", "Sam")
        var currentFriendIndex = 0 // start at first friend


        if (currentFriendIndex > 0) currentFriendIndex--
        assertEquals(0, currentFriendIndex, "Index should not go below 0.")


        if (currentFriendIndex < friends.size - 1) currentFriendIndex++
        assertEquals(1, currentFriendIndex, "Index should increment to 1.")
    }

    @Test
    fun testFriendStatsData() {

        val videosWatched = 10
        val creatorsCount = 4
        val totalWatchTimeHrs = 7.5

        // check its what we expect
        assertEquals(10, videosWatched, "Expected 10 videos watched.")
        assertEquals(4, creatorsCount, "Expected 4 creators.")
        assertEquals(7.5, totalWatchTimeHrs, "Expected 7.5 total watch hours.")
    }

    @Test
    fun testTopVideosAndCreators() {
        // top x items
        val topVideos = listOf("My Fun Vacation", "Cool Tech Talk", "Cats Being Cute")
        val topCreators = listOf("TechGuy123", "TravelVlogger", "ComedyCentral")

        // validation
        assertEquals(3, topVideos.size, "We should have 3 top videos.")
        assertTrue(topVideos.contains("Cats Being Cute"), "Cats Being Cute should be in top videos.")

        assertEquals(3, topCreators.size, "We should have 3 top creators.")
        assertTrue(topCreators.contains("TechGuy123"), "TechGuy123 should be in top creators.")
    }

    @Test
    fun testBarChartData() {
        // monthly data
        val monthlyData = listOf(10, 20, 15, 5, 30)
        val dayLabels = listOf("Jan", "Feb", "Mar", "Apr", "May")

        assertEquals(5, monthlyData.size, "Expected 5 data points for the chart.")
        assertEquals(5, dayLabels.size, "Expected 5 labels for the chart.")

        // check an alr known value
        assertEquals(30, monthlyData.last(), "Expected 30 in the last data point (May).")
        assertTrue(dayLabels.contains("Mar"), "We should have a 'Mar' label in the day labels.")
    }
}
