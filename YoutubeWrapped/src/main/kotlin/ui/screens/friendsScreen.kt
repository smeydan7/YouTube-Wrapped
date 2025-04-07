package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.youtubewrapped.ui.functions.getChannelThumbnails
import com.example.youtubewrapped.ui.functions.getDashboardSummary
import com.example.youtubewrapped.ui.functions.getVideoThumbnails
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.components.*
import ui.functions.extractChannelId
import ui.functions.extractVideoId
import userApiKit.DashboardData
import userApiKit.FriendUser
import userApiKit.HttpProvider

@Composable
fun FriendsScreen(
    userId: String,
    accessToken: String,
    onNavigateToDashboard: () -> Unit,
    onAddFriends: () -> Unit,
    onMyFriends: () -> Unit,
    onSearch: (String) -> Unit,

    // These allow for caching
    existingFriends: List<FriendUser>? = null,
    existingDashboardCache: Map<String, DashboardData>? = null,

    // Callbacks when data is fetched
    onFriendsFetched: (List<FriendUser>) -> Unit = {},
    onDashboardDataFetched: (String, DashboardData) -> Unit = { _, _ -> },

    initialFriendId: String? = null
) {
    println("FriendsScreen: Entered with userId=$userId, initialFriendId=$initialFriendId")

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Local search text
    var searchQuery by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf(existingFriends ?: emptyList()) }

    LaunchedEffect(existingFriends) {
        existingFriends?.let {
            friends = it
        }
    }

    LaunchedEffect(userId) {
        try {
            val initialFriends = fetchFriends(userId)
            if (friends != initialFriends) {
                friends = initialFriends
                onFriendsFetched(initialFriends)
            }
        } catch (e: Exception) {
            println("FriendsScreen: Error during initial fetch -> ${e.message}")
        }

        while (true) {
            delay(10000)
            try {
                val updatedFriends = fetchFriends(userId)
                if (friends != updatedFriends) {
                    friends = updatedFriends
                    onFriendsFetched(updatedFriends)
                }
            } catch (e: Exception) {
                println("FriendsScreen: Error fetching updated friends -> ${e.message}")
            }
        }
    }

    // index of the currently selected friend
    var currentFriendIndex by remember { mutableStateOf(0) }

    // if we have initialFriendId, jump to that friend
    LaunchedEffect(friends) {
        if (initialFriendId != null && friends.isNotEmpty()) {
            val idx = friends.indexOfFirst { it.id == initialFriendId }
            if (idx != -1) {
                currentFriendIndex = idx
            } else {
                println("FriendsScreen: initialFriendId=$initialFriendId NOT found in friends list.")
            }
        }
    }

    val dashboardCache = remember { mutableStateMapOf<String, DashboardData>() }

    LaunchedEffect(existingDashboardCache) {
        existingDashboardCache?.let {
            dashboardCache.putAll(it)
        }
    }

    LaunchedEffect(friends) {
        val staleKeys = dashboardCache.keys.filter { key ->
            friends.none { it.username == key }
        }
        staleKeys.forEach { key ->
            dashboardCache.remove(key)
            println("FriendsScreen: Removed stale dashboard cache for $key")
        }
    }

    if (currentFriendIndex >= friends.size && friends.isNotEmpty()) {
        currentFriendIndex = 0
    }

    val selectedFriend = friends.getOrNull(currentFriendIndex)

    LaunchedEffect(selectedFriend) {
        println("FriendsScreen: currentFriendIndex=$currentFriendIndex")
        if (selectedFriend == null) {
            println("FriendsScreen: selectedFriend is null (no friends?)")
        } else {
            println("FriendsScreen: selectedFriend.username=${selectedFriend.username}")

            if (!dashboardCache.containsKey(selectedFriend.username)) {
                println("FriendsScreen: Not cached -> fetching dashboard for ${selectedFriend.username}")
                scope.launch {
                    val dash = fetchDashboardData(selectedFriend.username, accessToken)
                    dashboardCache[selectedFriend.username] = dash
                    onDashboardDataFetched(selectedFriend.username, dash)
                }
            } else {
                println("FriendsScreen: Already have ${selectedFriend.username} in cache.")
            }

            // Preload neighbor dashboards
            val preloadIndices = (currentFriendIndex - 2..currentFriendIndex + 3).map {
                if (friends.isNotEmpty()) (it + friends.size) % friends.size else it
            }.distinct()
            println("FriendsScreen: Preloading indices: $preloadIndices")
            preloadIndices.forEach { idx ->
                val neighbor = friends[idx]
                if (!dashboardCache.containsKey(neighbor.username)) {
                    scope.launch {
                        println("FriendsScreen: Preloading dash for ${neighbor.username}")
                        val dash = fetchDashboardData(neighbor.username, accessToken)
                        dashboardCache[neighbor.username] = dash
                        onDashboardDataFetched(neighbor.username, dash)
                    }
                }
            }
        }
    }

    val filteredFriends = remember(searchQuery, friends) {
        if (searchQuery.isBlank()) emptyList()
        else {
            friends.filter { friend ->
                friend.first_name!!.contains(searchQuery, ignoreCase = true) ||
                        friend.last_name!!.contains(searchQuery, ignoreCase = true) ||
                        friend.username.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (friends.isEmpty()) return@onPreviewKeyEvent false
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            println("FriendsScreen: Left arrow pressed")
                            currentFriendIndex = (currentFriendIndex - 1 + friends.size) % friends.size
                            return@onPreviewKeyEvent true
                        }
                        Key.DirectionRight -> {
                            println("FriendsScreen: Right arrow pressed")
                            currentFriendIndex = (currentFriendIndex + 1) % friends.size
                            return@onPreviewKeyEvent true
                        }
                    }
                }
                false
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    TabButton(
                        title = "Dashboard",
                        selected = false,
                        onClick = onNavigateToDashboard,
                        modifier = Modifier
                            .width(160.dp)
                            .height(50.dp)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TabButton(
                        title = "Friends",
                        selected = true,
                        onClick = {},
                        modifier = Modifier
                            .width(160.dp)
                            .height(50.dp)
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Our search bar in the center
                Box(
                    Modifier
                        .width(300.dp)
                        .height(70.dp)
                        .align(Alignment.Center)
                ) {
                    SearchBar(onSearch = { query ->
                        searchQuery = query
                        onSearch(query)
                    })
                }

                Row(Modifier.align(Alignment.CenterEnd)) {
                    ActionButton(
                        title = "Add Friends",
                        onClick = onAddFriends,
                        modifier = Modifier
                            .width(300.dp)
                            .height(70.dp)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    ActionButton(
                        title = "My Friends",
                        onClick = onMyFriends,
                        modifier = Modifier
                            .width(300.dp)
                            .height(70.dp)
                            .padding(8.dp)
                    )
                }
            }

            // show "dropdown" of matches
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (searchQuery.isNotBlank()) {
                    FriendsDropdown(
                        matches = filteredFriends,
                        onFriendSelected = { friend ->
                            val idx = friends.indexOf(friend)
                            if (idx != -1) currentFriendIndex = idx
                            searchQuery = ""
                        },
                        onDismiss = {
                            searchQuery = ""
                        },
                        modifier = Modifier.width(350.dp)
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedFriend?.username?.firstOrNull()?.toString() ?: "",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                IconButton(
                    onClick = {
                        if (friends.isNotEmpty()) {
                            currentFriendIndex = (currentFriendIndex - 1 + friends.size) % friends.size
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                }

                Text(
                    text = selectedFriend?.username ?: if (friends.isEmpty()) "You have no friends" else "Loading...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        if (friends.isNotEmpty()) {
                            currentFriendIndex = (currentFriendIndex + 1) % friends.size
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (friends.isEmpty()) {
                // Text("You have no friends", fontSize = 18.sp)
            } else {

                    selectedFriend?.let { friend ->
                        val dash = dashboardCache[friend.username]
                        if (dash != null) {
                            println("FriendsScreen: Found dashboard in cache for ${friend.username}")
                            DashboardComponent(dashboardData = dash)
                        } else {
                            println("FriendsScreen: Dashboard not loaded yet for ${friend.username}")
                            Text("Loading friends dashboard...do not leave the screen until loaded")
                        }
                    }
                }

                // Vertical scrollbar
//        VerticalScrollbar(
//            adapter = rememberScrollbarAdapter(scrollState),
//            modifier = Modifier
//                .align(Alignment.CenterEnd)
//                .fillMaxHeight()
//        )

        }

    }
}
suspend fun fetchDashboardData(username: String, accessToken: String): DashboardData {
    println("fetchDashboardData: Starting for username=$username using bundled endpoint")
    val compositeData = getDashboardSummary(username)
    return if (compositeData != null) {
        val videoIds = compositeData.topVideos.mapNotNull { extractVideoId(it.url) }

        val videoThumbnailsMap = getVideoThumbnails(videoIds, accessToken) ?: emptyMap()
        val channelIds = compositeData.topChannels.mapNotNull { extractChannelId(it.url) }
        val channelThumbnailsMap = getChannelThumbnails(channelIds, accessToken) ?: emptyMap()


        val topVideoThumbnails = compositeData.topVideos.associate { video ->
            val videoId = extractVideoId(video.url)
            video to (if (videoId != null) videoThumbnailsMap[videoId] else null)
        }

        val topChannelThumbnails = compositeData.topChannels.associate { channel ->
            val channelId = extractChannelId(channel.url)
            channel to (if (channelId != null) channelThumbnailsMap[channelId] else null)
        }
        compositeData.copy(
            topVideo = compositeData.topVideos.firstOrNull(),
            topChannel = compositeData.topChannels.firstOrNull(),
            topVideoThumbnails = topVideoThumbnails,
            topChannelThumbnails = topChannelThumbnails
        )
    } else {
        println("fetchDashboardData: Bundled endpoint failed for $username, returning fallback data")
        DashboardData(
            username = username,
            firstName = "",
            videoCount = 0,
            adsCount = 0,
            channelsCount = 0,
            topVideos = emptyList(),
            topChannels = emptyList(),
            topVideo = null,
            topChannel = null,
            topVideoThumbnails = emptyMap(),
            topChannelThumbnails = emptyMap(),
            monthFrequency = emptyMap()
        )
    }
}

suspend fun fetchFriends(userId: String): List<FriendUser> {
    println("fetchFriends: Attempting to fetch friends for userId=$userId")
    val response = HttpProvider.client.get("https://youtubewrapper-450406.uc.r.appspot.com/friends/all/$userId")
    println("fetchFriends: Response status -> ${response.status}")
    return if (response.status == HttpStatusCode.OK) {
        val body = response.body<List<FriendUser>>()
        println("fetchFriends: Successfully got ${body.size} friends.")
        body
    } else {
        println("fetchFriends: Non-OK status => returning empty list")
        emptyList()
    }
}

val client = HttpProvider.client