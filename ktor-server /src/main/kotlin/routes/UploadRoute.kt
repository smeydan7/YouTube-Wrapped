package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import services.ActivityService
import java.io.File
import java.util.*
import io.ktor.utils.io.jvm.javaio.toInputStream

fun Route.uploadRoute() {
    post("/upload") {
        println("in upload")
        val multipartData = call.receiveMultipart()
        var username: String = ""
        var storedFile: File? = null

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "username") {
                        username = part.value
                    }
                }



                is PartData.FileItem -> {

                    val uploadDir = File("/tmp/uploads")
                    if (!uploadDir.exists()) uploadDir.mkdirs()

                    val extension = File(part.originalFileName ?: "watch_history.json").extension
                    val uniqueFileName = "${username ?: UUID.randomUUID()}_${System.currentTimeMillis()}.$extension"
                    val file = File(uploadDir, uniqueFileName)

                    withContext(Dispatchers.IO) {
                        part.provider().toInputStream().use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                    storedFile = file
                    println("File stored at: ${file.absolutePath}")
                }

                else -> {}
            }
            part.dispose()
        }

        if (storedFile != null) {
            println("Triggering processing for: ${storedFile!!.name}")
            val activityService = ActivityService()
            activityService.getFilteredActivities(storedFile!!.absolutePath, username) // you can adapt this to take path

            val deleted = storedFile!!.delete()
            if (deleted) {
                println("Temporary file deleted: ${storedFile!!.name}")
            } else {
                println("WARNING: Could not delete file: ${storedFile!!.name}")
            }

            call.respond(HttpStatusCode.OK, "File uploaded and processed successfully. Go back to view stats!")
        } else {
            call.respond(HttpStatusCode.BadRequest, "No file uploaded.")
        }
    }
}