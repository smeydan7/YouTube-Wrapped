package userApiKit

import Screen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.youtubewrapped.ui.screens.FriendsScreenRoot
import com.example.youtubewrapped.ui.screens.LoginPage
import com.example.youtubewrapped.ui.screens.ProfileScreen
import com.example.youtubewrapped.ui.screens.UploadDataScreen
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.screens.DashboardScreen
import ui.screens.FriendsScreen
import ui.screens.LandingScreen
import ui.screens.exploreScreen
import androidx.compose.runtime.*

@Composable
fun MainAppContent(
    currentScreen: Screen,
    setCurrentScreen: (Screen) -> Unit,
    accessToken: String?,
    setAccessToken: (String?) -> Unit,
    appState: AppState,
    setAppState: (AppState) -> Unit,
    dashboardData: DashboardData?,
    onDashboardDataChange: (DashboardData) -> Unit,
    friendsScreenData: FriendsScreenData?,
    onFriendsScreenDataChange: (FriendsScreenData) -> Unit,
    friendsDashboardData: FriendsDashboardData?,
    onFriendsDashboardDataChange: (FriendsDashboardData) -> Unit,
) {

    var needsRefresh by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    when (currentScreen) {
        is Screen.Landing -> {
            if (accessToken == null) {
                LandingScreen { token ->
                    setAccessToken(token)
                    println("Received Token: $token")
                    coroutineScope.launch(Dispatchers.IO) {
                        val (uname, uid) = fetchUsernameAndUserId(token)
                        println("got username: $uid")
                        setAppState(AppState(uname, uid))
                    }
                }
            } else if (appState.username == null || appState.userId == null) {
                // Show a loading indicator while user info is being fetched
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Frame.Text("Loading user data...")
                }
            } else {
                var showReminder by remember { mutableStateOf(false) }

                LaunchedEffect(appState.username) {
                    val noUploads = !hasUploadedData(appState.username)
                    if (noUploads) {
                        showReminder = true
                        kotlinx.coroutines.delay(15_000)
                        showReminder = false
                    } else {
                        showReminder = false
                    }
                }

                DashboardScreen(
                    accessToken,
                    appState.username,
                    onNavigateToFriends = { setCurrentScreen(Screen.Friends) },
                    onNavigateToProfile = { setCurrentScreen(Screen.Profile) },
                    existingData = dashboardData,
                    onDataFetched = { newData: DashboardData -> onDashboardDataChange(newData) },
                    onUploadData = { setCurrentScreen(Screen.UploadData) },
                    needsRefresh = needsRefresh,
                    onNeedsRefreshChange = { newValue -> needsRefresh = newValue },
                    shouldShowUploadReminder = showReminder

                )
            }
        }
        is Screen.FriendsWithSelected -> {
            // This screen is basically the same as Screen.Friends,
            // but we pass the friend’s ID so we can select them.
            FriendsScreen(
                userId = appState.userId!!,
                accessToken = accessToken!!,
                onNavigateToDashboard = { setCurrentScreen(Screen.Dashboard) },
                onAddFriends = { setCurrentScreen(Screen.Explore) },
                onMyFriends = { setCurrentScreen(Screen.ListOfFriends) },
                onSearch = { query -> println("Search query: cle$query") },
                existingFriends = friendsDashboardData?.friends,
                existingDashboardCache = friendsDashboardData?.dashboardCache,
                onFriendsFetched = { newlyFetchedFriends ->
                    val currentCache = friendsDashboardData?.dashboardCache ?: emptyMap()
                    val newData = FriendsDashboardData(
                        friends = newlyFetchedFriends,
                        dashboardCache = currentCache
                    )
                    onFriendsDashboardDataChange(newData)
                },
                onDashboardDataFetched = { username, dashboard ->
                    val oldFriends = friendsDashboardData?.friends ?: emptyList()
                    val oldCache = friendsDashboardData?.dashboardCache ?: emptyMap()
                    val updatedCache = oldCache.toMutableMap().apply {
                        this[username] = dashboard
                    }
                    val newData = FriendsDashboardData(
                        friends = oldFriends,
                        dashboardCache = updatedCache
                    )
                    onFriendsDashboardDataChange(newData)
                },

                initialFriendId = currentScreen.friendId
            )
        }
        is Screen.Dashboard -> {
            DashboardScreen(
                accessToken!!,
                appState.username!!,
                onNavigateToFriends = { setCurrentScreen(Screen.Friends) },
                onNavigateToProfile = { setCurrentScreen(Screen.Profile) },
                existingData = dashboardData,
                onDataFetched = { newData: DashboardData -> onDashboardDataChange(newData) },
                onUploadData = { setCurrentScreen(Screen.UploadData) },
                needsRefresh = needsRefresh,
                onNeedsRefreshChange = { needsRefresh = it }
            )
        }

        is Screen.Friends -> {
            FriendsScreen(
                userId = appState.userId!!,
                accessToken = accessToken!!,
                onNavigateToDashboard = { setCurrentScreen(Screen.Dashboard) },
                onAddFriends = { setCurrentScreen(Screen.Explore) },
                onMyFriends = { setCurrentScreen(Screen.ListOfFriends) },
                onSearch = { query -> println("Search query: $query") },

                // pass in whatever we have so far for caching
                existingFriends = friendsDashboardData?.friends,
                existingDashboardCache = friendsDashboardData?.dashboardCache,

                onFriendsFetched = { newlyFetchedFriends ->
                    val currentCache = friendsDashboardData?.dashboardCache ?: emptyMap()
                    val newData = FriendsDashboardData(
                        friends = newlyFetchedFriends,
                        dashboardCache = currentCache
                    )
                    onFriendsDashboardDataChange(newData)
                },

                onDashboardDataFetched = { username, dashboard ->
                    val oldFriends = friendsDashboardData?.friends ?: emptyList()
                    val oldCache = friendsDashboardData?.dashboardCache ?: emptyMap()
                    val updatedCache = oldCache.toMutableMap().apply {
                        this[username] = dashboard
                    }
                    val newData = FriendsDashboardData(
                        friends = oldFriends,
                        dashboardCache = updatedCache
                    )
                    onFriendsDashboardDataChange(newData)
                }
            )

        }
        is Screen.Explore -> {
            exploreScreen(
                username = appState.username!!,
                onBack = { setCurrentScreen(Screen.Friends) },
                onProfile = { setCurrentScreen(Screen.Profile) }
            )
        }
        is Screen.ListOfFriends -> {
            FriendsScreenRoot(
                appState.userId!!,
                appState.username!!,
                existingData = friendsScreenData,
                onDataFetched = { newData: FriendsScreenData -> onFriendsScreenDataChange(newData) },
                onBack = { setCurrentScreen(Screen.Friends) },
                onProfileClick = { setCurrentScreen(Screen.Profile) },
                onSearch = { query -> println("Searching for: $query") },
                onNavigateToFriendDashboard = { friend ->
                    setCurrentScreen(Screen.FriendsWithSelected(friend.id))
                }
            )
        }

        is Screen.Login -> {
            LoginPage {  }
        }
        is Screen.Profile -> {
            ProfileScreen(onHomeClick = {setCurrentScreen(Screen.Dashboard) },
                appState.username!!, onProfileUpdated = { updatedFirstName: String ->
                    dashboardData?.let {
                        onDashboardDataChange(it.copy(firstName = updatedFirstName))
                    }
                })
        }
        is Screen.UploadData -> {
            UploadDataScreen(
                appState.username!!,
                onBackUploadScreen = { setCurrentScreen(Screen.Dashboard) },
                editProfileUploadScreen = { setCurrentScreen(Screen.Profile) },
                onUploadSuccess = {
                    needsRefresh = true
                }
            )
        }
        else -> {}
    }
}

suspend fun hasUploadedData(username: String): Boolean {
    val response = HttpProvider.client.get("https://youtubewrapper-450406.uc.r.appspot.com/video-count/$username")
    return try {
        println("Video count response for $username: '$response'")
        val count = response.bodyAsText().toInt()
        println("Count $username: '$count'")
        count > 0
    } catch (e: Exception) {
        println("Error checking video count: ${e.message}")
        false
    }
}