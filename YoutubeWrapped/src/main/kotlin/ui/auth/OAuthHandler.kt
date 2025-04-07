package ui.auth

import java.awt.Desktop
import java.net.ServerSocket
import java.net.URI
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun openOAuthLogin(onTokenReceived: (String) -> Unit) {
    val loginUrl = "https://youtubewrapper-450406.uc.r.appspot.com/auth/google-login"

    // Open browser for Google OAuth login
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(URI(loginUrl))
    } else {
        println("Desktop browsing not supported, open this link manually: $loginUrl")
    }

//   Start a local server to listen for OAuth callback
    CoroutineScope(Dispatchers.IO).launch {
        startCallbackServer(onTokenReceived)
    }
}

// start a local server to handle Google OAuth callback
fun startCallbackServer(onTokenReceived: (String) -> Unit) {
    val server = ServerSocket(8081)
    println("Waiting for OAuth callback...")

    while (true) {
        val socket = server.accept()
        val request = socket.getInputStream().bufferedReader().readLine()

        if (request != null && request.contains("GET /callback")) {
            val tokenParam = request.split("token=")
            if (tokenParam.size > 1) {
                val jwtToken = tokenParam[1].split(" ")[0] // Extract token from URL

                onTokenReceived(jwtToken)

                val response = """
                HTTP/1.1 200 OK
                Content-Type: text/html

                <html>
                <body>
                    <h2>Login Successful!</h2>
                    <p>You can close this window now.</p>
                    <script>
                        setTimeout(() => window.close(), 2000);
                    </script>
                </body>
                </html>
                """.trimIndent()

                socket.getOutputStream().write(response.toByteArray())
                socket.close()
                break
            }
        }
    }
    server.close()
}


// exchange Auth Code for Token
suspend fun exchangeAuthCodeForToken(authCode: String): String {
    val httpClient = HttpClient()
    val tokenResponse: String = httpClient.get("https://youtubewrapper-450406.uc.r.appspot.com/auth/callback?code=$authCode").bodyAsText()

    val json = Json.parseToJsonElement(tokenResponse).jsonObject
    return json["token"]?.jsonPrimitive?.content ?: "INVALID_TOKEN"
}