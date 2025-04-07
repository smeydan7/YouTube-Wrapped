package com.example.youtubewrapped.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.youtubewrapped.ui.components.PrimaryButton
import ui.components.ProfileTopBar
import com.example.youtubewrapped.ui.components.TextFieldComponent

@Composable
fun LoginPage(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // YOUTUBE LOGO IMAGE
            Text(
                text = "YouTube Wrapped",
                style = MaterialTheme.typography.displayLarge,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextFieldComponent(label = "Email", value = email, onValueChange = { email = it })
            TextFieldComponent(label = "Password", value = password, onValueChange = { password = it })
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(text = "Login", onClick = {}, modifier = Modifier.width(120.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? Create one ")
                    pushStringAnnotation(tag = "link", annotation = "Create account")
                    withStyle(style = androidx.compose.ui.text.SpanStyle(
                        color = Color.Red,
                        textDecoration = TextDecoration.Underline
                    )) {
                        append("here.")
                    }
                    pop()
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable {
                    onLogin()
                }
            )
        }
    }
}

fun onLogin() {
    // implement navigation logic
}
