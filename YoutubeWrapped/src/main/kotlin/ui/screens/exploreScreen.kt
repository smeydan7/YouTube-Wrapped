package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import userApiKit.User
import userApiKit.fetchAllUsers
import userApiKit.fetchConnections
import userApiKit.sendFriendRequest
import ui.components.SearchBar

@Composable
fun exploreScreen(
    username: String,
    onBack: () -> Unit,
    onProfile: () -> Unit
) {
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var filteredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val requestedUsers = rememberSaveable { mutableStateOf(setOf<String>()) }
    val friends = rememberSaveable { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        val fetchedUsers = fetchAllUsers(username)
        allUsers = fetchedUsers
        filteredUsers = fetchedUsers

        val connections = fetchConnections(username)
        requestedUsers.value = connections["requested"] ?: emptySet()
        friends.value = connections["friends"] ?: emptySet()
    }

    fun updateSearch(query: String) {
        searchQuery = query
        filteredUsers = allUsers.filter {
            it.username.contains(query, ignoreCase = true) ||
                    it.first_name.contains(query, ignoreCase = true) ||
                    it.lastname.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onBack() }
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Explore",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onProfile() }
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
                Box(
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SearchBar(onSearch = { query -> updateSearch(query) })
                }
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.widthIn(max = 500.dp)
                ) {
                    filteredUsers.forEach { user ->
                        val isRequested = user.username in requestedUsers.value
                        val isFriend = user.username in friends.value
                        val icon = if (isFriend || isRequested) Icons.Default.Check else Icons.Default.Add
                        val iconDescription = when {
                            isFriend -> "Already Friends"
                            isRequested -> "Request Sent"
                            else -> "Add Friend"
                        }
                        val isClickable = !(isRequested || isFriend)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.first_name.firstOrNull()?.toString() ?: "",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "${user.first_name} ${user.lastname} (${user.username})",
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = icon,
                                contentDescription = iconDescription,
                                modifier = Modifier
                                    .size(32.dp)
                                    .let { mod ->
                                        if (isClickable) mod.clickable {
                                            requestedUsers.value = requestedUsers.value + user.username
                                            coroutineScope.launch {
                                                val success = sendFriendRequest(sender = username, receiver = user.username)
                                                val message = if (success) {
                                                    "Sent friend request to ${user.username}"
                                                } else {
                                                    requestedUsers.value = requestedUsers.value - user.username
                                                    "Failed to send friend request to ${user.username}"
                                                }
                                                scaffoldState.snackbarHostState.showSnackbar(message)
                                            }
                                        } else mod
                                    }
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    )
}