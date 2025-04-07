package com.example.youtubewrapped.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import ui.components.ActionButton
import ui.components.HoverableNavArea
import userApiKit.HttpProvider
import java.awt.FileDialog
import java.awt.Frame
import java.io.File


@Composable
fun UploadDataScreen(
    username: String,
    onBackUploadScreen: () -> Unit,
    editProfileUploadScreen: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    var uploadMessage by remember { mutableStateOf<String?>(null) }

    fun pickFileDesktop(): File? {
        val fileDialog = FileDialog(null as Frame?, "Select File", FileDialog.LOAD)
        fileDialog.isVisible = true
        return if (fileDialog.file != null) File(fileDialog.directory, fileDialog.file) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            HoverableNavArea(
                enabled = true,
                onClick = onBackUploadScreen,
                icon = Icons.Default.ArrowBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            HoverableNavArea(
                enabled = true,
                onClick = editProfileUploadScreen,
                icon = Icons.Default.AccountBox,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Steps to download your data are shown below",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ActionButton(
                onClick = {
                    val file = pickFileDesktop()
                    if (file != null) {
                        val fileBytes = file.readBytes()
                        coroutineScope.launch {
                            val message = storeFile(fileBytes, "watch_history.json", username)
                            uploadMessage = message
                            if (message.contains("Success", ignoreCase = true)
                                || message.contains("Uploaded", ignoreCase = true)
                            ) {
                                onUploadSuccess()
                            }
                        }
                    } else {
                        println("No file selected.")
                    }
                },
                title = "Upload File"
            )
        }

        if (uploadMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(color = Color.Green)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = uploadMessage!!,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(800.dp)
                    .wrapContentHeight()
                    .border(BorderStroke(2.dp, androidx.compose.ui.graphics.Color.Black)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val annotatedText = buildAnnotatedString {
                        append("Visit ")
                        pushStringAnnotation(
                            tag = "URL",
                            annotation = "https://takeout.google.com/settings/takeout/custom/youtube"
                        )
                        withStyle(
                            style = androidx.compose.ui.text.SpanStyle(
                                color = androidx.compose.ui.graphics.Color.Red,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Google Takeout")
                        }
                        pop()
                        append(" for YouTube, then follow these steps:")
                    }

                    ClickableText(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(10.dp),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    uriHandler.openUri(annotation.item)
                                }
                        }
                    )
                    Image(
                        bitmap = useResource("images/YTW1.png") { loadImageBitmap(it) },
                        contentDescription = "Data Download Steps",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Text(
                        text = "Make sure you are logged into your YouTube account. Click on ‘Multiple Formats’.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Image(
                        bitmap = useResource("images/YTW2.png") { loadImageBitmap(it) },
                        contentDescription = "YouTube Logo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Text(
                        text = "Find the ‘history’ option, and select ‘JSON’. Once you have selected ‘JSON’, press the ‘OK’ button in the bottom right.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Image(
                        bitmap = useResource("images/YTW3.png") { loadImageBitmap(it) },
                        contentDescription = "Data Download Steps",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Text(
                        text = "You will then be returned to this page. Click on ‘All YouTube data included’.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Image(
                        bitmap = useResource("images/YTW4.png") { loadImageBitmap(it) },
                        contentDescription = "Data Download Steps",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Text(
                        text = "Make sure only ‘history’ is selected, then press ‘OK’. You will be redirected to the previous page, click ‘Next Step’.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Image(
                        bitmap = useResource("images/YTW5.png") { loadImageBitmap(it) },
                        contentDescription = "Data Download Steps",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Text(
                        text = "Once you see this page, click ‘Create Export’. You will see a message saying ‘Export Progress’. After a few minutes, you will receive an email with your data.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Image(
                        bitmap = useResource("images/YTW6.png") { loadImageBitmap(it) },
                        contentDescription = "Data Download Steps",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Text(
                        text = "Click on 'Download your files'.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Unzip the downloaded file, and upload the file ‘watch-history.json’ to the dropbox. It is found at:\n")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(" Takeout/YouTube and YouTube Music/history/watch-history.json")
                            }
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            androidx.compose.material3.Text(
                text = buildAnnotatedString {
                    pushStringAnnotation(tag = "link", annotation = "Back to top")
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            color = androidx.compose.ui.graphics.Color.Black,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Back to top")
                    }
                    pop()
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    }
                    .padding(30.dp)
            )
        }
    }
}

suspend fun storeFile(fileBytes: ByteArray, filename: String, username: String, client: HttpClient = HttpProvider.client): String {
    return try {
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "https://youtubewrapper-450406.uc.r.appspot.com/upload",
            formData = formData {
                append("username", username)
                append("file", fileBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=$filename")
                })
            }
        )

        if (response.status.isSuccess()) {
            response.bodyAsText()
        } else {
            "Upload failed: ${response.status}"
        }

    } catch (e: Exception) {
        "Upload failed: ${e.message}"
    }
}