package com.example.youtubewrapped.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.youtubewrapped.ui.components.PrimaryButton
import com.example.youtubewrapped.ui.components.TextFieldComponent
import kotlinx.coroutines.launch
import ui.components.ProfileTopBar
import userApiKit.fetchUserInfo
import userApiKit.updateUserInfo

@Composable
fun ProfileScreen(onHomeClick: () -> Unit, username: String, onProfileUpdated: (String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var initialFirstName by remember { mutableStateOf("") }
    var initialLastName by remember { mutableStateOf("") }

    var showSuccessMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(5000)
            showSuccessMessage = false
        }
    }

    LaunchedEffect(Unit) {
        val userInfo = fetchUserInfo(username)
        initialFirstName = userInfo.firstName ?: ""
        initialLastName = userInfo.lastName ?: ""
        firstName = initialFirstName ?: ""
        lastName = initialLastName ?: ""
    }

    Scaffold(
        topBar = { ProfileTopBar(onHomeClick = onHomeClick) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(500.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Profile",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfilePicture(icon = Icons.Filled.AccountCircle)

                Spacer(modifier = Modifier.height(16.dp))

                TextFieldComponent(
                    label = "First Name",
                    value = firstName,
                    placeholder = initialFirstName,
                    onValueChange = { firstName = it }
                )

                TextFieldComponent(
                    label = "Last Name",
                    value = lastName,
                    placeholder = initialLastName,
                    onValueChange = { lastName = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(text = "Save Changes", onClick = {
                    scope.launch {
                        updateUserInfo(
                            username = username,
                            newFirstName = firstName,
                            newLastName = lastName,
                            onSuccess = {
                                showSuccessMessage = true
                                scope.launch {
                                    val updatedInfo = fetchUserInfo(username)
                                    initialFirstName = updatedInfo.firstName ?: ""
                                    initialLastName = updatedInfo.lastName ?: ""
                                    firstName = initialFirstName
                                    lastName = initialLastName

                                    onProfileUpdated(firstName)
                                }
                            },
                            onError = { errorMsg ->
                                println("Error updating user: $errorMsg")
                            }
                        )
                    }
                }, modifier = Modifier.width(200.dp))
                if (showSuccessMessage) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Changes saved!")
                }
            }
        }
    }
}


@Composable
fun ProfilePicture(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = "Profile Picture", modifier = Modifier.size(100.dp))
}