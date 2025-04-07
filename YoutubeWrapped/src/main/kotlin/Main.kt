
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import userApiKit.*

var accessToken: String? = null

//screens stored here
sealed class Screen {
    object Friends : Screen()
    object Explore : Screen()
    object Profile : Screen()
    object Login : Screen()
    object ListOfFriends : Screen()
    object UploadData : Screen()
    object Dashboard : Screen()
    object Landing : Screen()
    data class FriendsWithSelected(val friendId: String) : Screen()
}


fun main() = application {
    Window(
        onCloseRequest = {
            HttpProvider.client.close()
            exitApplication()
        }, title = "YouTube Wrapped") {
        MaterialTheme {

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Landing) }
            var dashboardData by remember { mutableStateOf<DashboardData?>(null) }
            var friendsScreenData by remember { mutableStateOf<FriendsScreenData?>(null) }
            var appState by remember { mutableStateOf(AppState(null, null)) }
            var accessToken by remember { mutableStateOf<String?>(null) }
            var friendsDashboardData by remember { mutableStateOf<FriendsDashboardData?>(null) }

            MainAppContent(
                currentScreen = currentScreen,
                setCurrentScreen = { currentScreen = it },
                accessToken = accessToken,
                setAccessToken = { accessToken = it },
                appState = appState,
                setAppState = { appState = it },
                dashboardData = dashboardData,
                onDashboardDataChange = { dashboardData = it },
                friendsScreenData = friendsScreenData,
                onFriendsScreenDataChange = { friendsScreenData = it },
                friendsDashboardData = friendsDashboardData,
                onFriendsDashboardDataChange = { friendsDashboardData = it }
            )
        }
    }
}