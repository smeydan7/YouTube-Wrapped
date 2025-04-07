package com.example.youtubewrapped.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import ui.components.SearchBar
import ui.components.TabButton
import userApiKit.FriendUser
import userApiKit.FriendsScreenData
import userApiKit.HttpProvider
import userApiKit.fetchUserIdByUsername

@Composable
fun FriendsScreenRoot(
    userId: String,
    username: String,
    existingData: FriendsScreenData?,
    onDataFetched: (FriendsScreenData) -> Unit,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onSearch: (String) -> Unit,
    onNavigateToFriendDashboard: (FriendUser) -> Unit
) {
    LaunchedEffect(userId) {
        if (existingData == null) {
            val friends = fetchFriends(userId)
            val incoming = fetchIncomingRequests(userId)
            val outgoing = fetchOutgoingRequests(userId)
            onDataFetched(FriendsScreenData(friends, incoming, outgoing))
        }
    }

    // If we have existing data, show the screen. Otherwise, show "Loading"
    if (existingData != null) {
        FriendsScreen(
            friendsList = existingData.friends,
            incomingList = existingData.incomingRequests,
            outgoingList = existingData.outgoingRequests,
            onBack = onBack,
            onProfileClick = onProfileClick,
            userId = userId,
            username = username,
            onSearch = onSearch,
            onDataChanged = onDataFetched,
            onNavigateToFriendDashboard = onNavigateToFriendDashboard
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading Friends...")
        }
    }
}

@Composable
fun FriendsScreen(
    friendsList: List<FriendUser>,
    incomingList: List<FriendUser>,
    outgoingList: List<FriendUser>,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    userId: String,
    username: String,
    onSearch: (String) -> Unit,
    onDataChanged: (FriendsScreenData) -> Unit,
    onNavigateToFriendDashboard: (FriendUser) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // For local searching
    var searchQuery by remember { mutableStateOf("") }
    var isFriendsLoaded by remember { mutableStateOf(false) }

    // Poll every 10s for updated friends
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val updatedFriends = fetchFriends(userId)
            val updatedIncoming = fetchIncomingRequests(userId)
            val updatedOutgoing = fetchOutgoingRequests(userId)
            onDataChanged(
                FriendsScreenData(
                    friends = updatedFriends,
                    incomingRequests = updatedIncoming,
                    outgoingRequests = updatedOutgoing
                )
            )
            isFriendsLoaded = true
        }
    }


    // Filter all 3 lists by searchQuery
    val filteredFriends = remember(searchQuery, friendsList) {
        if (searchQuery.isBlank()) friendsList
        else friendsList.filter { friendMatchesQuery(it, searchQuery) }
    }
    val filteredIncoming = remember(searchQuery, incomingList) {
        if (searchQuery.isBlank()) incomingList
        else incomingList.filter { friendMatchesQuery(it, searchQuery) }
    }
    val filteredOutgoing = remember(searchQuery, outgoingList) {
        if (searchQuery.isBlank()) outgoingList
        else outgoingList.filter { friendMatchesQuery(it, searchQuery) }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            // Top bar with "Back" arrow and "Friends" tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // back arrow
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.weight(1f))
                // Title
                TabButton(
                    title = "Friends",
                    selected = true,
                    onClick = { /* no-op */ },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(180.dp)
                        .height(60.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                // icon to go to *user's own* profile
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onProfileClick() }
                )
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // search bar
                Box(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SearchBar(onSearch = { query ->
                        searchQuery = query
                        onSearch(query)  // Let parent know if it wants
                    })
                }
                Spacer(modifier = Modifier.height(24.dp))
                // "Added friends" section
                Text(
                    text = "Added Friends",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Thin,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(modifier = Modifier.widthIn(max = 500.dp)) {
                    if (!isFriendsLoaded) {
                        // Show a loading indicator or nothing until data is loaded
                        Text(
                            "Loading friends...",
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    else if (friendsList.isEmpty()) {
                        Text(
                            "You have no friends yet.",
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyColumn {
                            items(filteredFriends) { user ->
                                // pass the new onProfileClick to let it navigate
                                FriendRow(
                                    user = user,
                                    onDelete = { friendToRemove ->
                                        scope.launch {
                                            removeFriend(userId, friendToRemove.id)
                                            val updatedFriends = fetchFriends(username)
                                            print("updated friends $updatedFriends");
                                            val updatedIncoming = fetchIncomingRequests(username)
                                            val updatedOutgoing = fetchOutgoingRequests(username)
                                            onDataChanged(
                                                FriendsScreenData(
                                                    updatedFriends,
                                                    updatedIncoming,
                                                    updatedOutgoing
                                                )
                                            )
                                        }
                                    },
                                    onProfileClick = { clickedUser ->
                                        onNavigateToFriendDashboard(clickedUser)
                                    },
                                    scaffoldState = scaffoldState,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                // "Requested friends" section
                Text(
                    text = "My requests",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Thin,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(modifier = Modifier.widthIn(max = 500.dp)) {
                    if (incomingList.isEmpty()) {
                        Text(
                            "You have no requests yet.",
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyColumn {
                            items(filteredIncoming) { user ->
                                FriendRow(
                                    user = user,
                                    onDelete = {
                                        scope.launch {
                                            rejectFriendRequest(user.id)
                                            val updatedIncoming = incomingList.filterNot { it.id == user.id }
                                            val updatedData = FriendsScreenData(
                                                friends = friendsList,
                                                incomingRequests = updatedIncoming,
                                                outgoingRequests = outgoingList
                                            )
                                            onDataChanged(updatedData)
                                        }
                                    },
                                    onAccept = { friendToAccept ->
                                        scope.launch {
                                            acceptFriendRequest(
                                                senderId = friendToAccept.id,
                                                receiverId = userId
                                            )
                                            val updatedIncoming =
                                                incomingList.filterNot { it.id == friendToAccept.id }
                                            val updatedFriends = friendsList + friendToAccept
                                            val updatedData = FriendsScreenData(
                                                friends = updatedFriends,
                                                incomingRequests = updatedIncoming,
                                                outgoingRequests = outgoingList
                                            )
                                            onDataChanged(updatedData)
                                        }
                                    },
                                    scaffoldState = scaffoldState,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                // "Sent requests" section
                Text(
                    text = "Sent requests",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Thin,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(modifier = Modifier.widthIn(max = 500.dp)) {
                    if (outgoingList.isEmpty()) {
                        Text(
                            "You have not sent any requests yet.",
                            fontSize = 20.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyColumn {
                            items(filteredOutgoing) { user ->
                                FriendRow(
                                    user = user,
                                    onDelete = {
                                        scope.launch {
                                            unsendFriendRequest(senderId = userId, receiverId = user.id)
                                            val updatedOutgoing =
                                                outgoingList.filterNot { it.id == user.id }
                                            val updatedData = FriendsScreenData(
                                                friends = friendsList,
                                                incomingRequests = incomingList,
                                                outgoingRequests = updatedOutgoing
                                            )
                                            onDataChanged(updatedData)
                                        }
                                    },
                                    scaffoldState = scaffoldState,
                                    // If you want the user to navigate from "outgoing" as well:
                                    // onProfileClick = { clickedUser ->
                                    //     onNavigateToFriendDashboard(clickedUser)
                                    // }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}


fun friendMatchesQuery(user: FriendUser, query: String): Boolean {
    return user.first_name?.contains(query, ignoreCase = true) == true ||
            user.last_name?.contains(query, ignoreCase = true) == true ||
            user.username.contains(query, ignoreCase = true)
}


@Composable
fun FriendRow(
    user: FriendUser,
    onDelete: (FriendUser) -> Unit,
    onAccept: ((FriendUser) -> Unit)? = null,
    onProfileClick: ((FriendUser) -> Unit)? = null,
    scaffoldState: ScaffoldState
) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            user.username,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        // Accept button (for incoming requests)
        if (onAccept != null) {
            IconButton(onClick = {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Accepting friend...")
                    onAccept(user)
                }
            }) {
                Icon(Icons.Default.Check, contentDescription = "Accept", modifier = Modifier.size(26.dp))
            }
        }
        // Delete icon
        IconButton(onClick = {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Deleting...")
            }
            onDelete(user)
        }) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(26.dp))
        }
        if (onProfileClick != null) {
            // Single Person icon to view friend’s dashboard
//            IconButton(
//                onClick = {
//                    onProfileClick.invoke(user)
//                }
//            ) {
//                Icon(
//                    Icons.Default.Person,
//                    contentDescription = "View Friend’s Dashboard",
//                    modifier = Modifier.size(26.dp)
//                )
//            }
        }
    }
}

val client = HttpProvider.client

suspend fun fetchFriends(userId: String): List<FriendUser> {
    println("Fetching friends for userId: $userId")
    val response = HttpProvider.client.get("https://youtubewrapper-450406.uc.r.appspot.com/friends/all/$userId")
    return if (response.status == HttpStatusCode.OK) {
        val responseBody = response.bodyAsText()
        println("fetchFriends response: $responseBody")
        Json.decodeFromString(responseBody) // Deserializes into List<FriendUser>
    } else {
        emptyList()
    }
}

suspend fun fetchIncomingRequests(userId: String): List<FriendUser> {
    println("Fetching incoming friend requests for userId: $userId")
    val response = HttpProvider.client.get("https://youtubewrapper-450406.uc.r.appspot.com/friends/requests/$userId")
    return if (response.status == HttpStatusCode.OK) {
        val responseBody = response.bodyAsText()
        println("fetchIncomingRequests response: $responseBody")
        Json.decodeFromString(responseBody)
    } else {
        emptyList()
    }
}

suspend fun fetchOutgoingRequests(userId: String): List<FriendUser> {
    println("Fetching outgoing friend requests for userId: $userId")
    val response = HttpProvider.client.get("https://youtubewrapper-450406.uc.r.appspot.com/friends/requested/$userId")
    return if (response.status == HttpStatusCode.OK) {
        val responseBody = response.bodyAsText()
        println("fetchOutgoingRequests response: $responseBody")
        Json.decodeFromString(responseBody)
    } else {
        emptyList()
    }
}

suspend fun removeFriend(userId: String, friendId: String) {
    print("removing friend with userId: $userId and friendId: $friendId")
    client.delete("https://youtubewrapper-450406.uc.r.appspot.com/friends/remove/$userId/$friendId")
}

suspend fun rejectFriendRequest(senderId: String) {
    client.delete("https://youtubewrapper-450406.uc.r.appspot.com/friends/requests/reject/$senderId")
}

suspend fun unsendFriendRequest(senderId: String, receiverId: String) {
    client.delete("https://youtubewrapper-450406.uc.r.appspot.com/friends/requests/unsend/$senderId/$receiverId")
}

suspend fun acceptFriendRequest(senderId: String, receiverId: String) {
    client.post("https://youtubewrapper-450406.uc.r.appspot.com/friends/requests/accept/$senderId/$receiverId")
}