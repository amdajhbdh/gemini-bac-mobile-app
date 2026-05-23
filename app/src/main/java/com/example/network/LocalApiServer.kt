package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.*
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object LocalApiServer {
    private const val TAG = "LocalApiServer"
    private var server: HttpServer? = null
    private var executorService: java.util.concurrent.ExecutorService? = null
    var isRunning = false
        private set
    var activePort = 9090
        private set

    fun start(context: Context, repository: LessonSyncRepository, port: Int = 9090) {
        if (isRunning) {
            stop()
        }
        try {
            activePort = port
            executorService = java.util.concurrent.Executors.newFixedThreadPool(4)
            server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0).apply {
                createContext("/", RootHandler(activePort))
                createContext("/notes", NotesHandler(context, repository))
                createContext("/add_note", AddNoteHandler(context, repository))
                createContext("/schedule", ScheduleHandler(repository))
                createContext("/add_schedule", AddScheduleHandler(context, repository))
                createContext("/messages", MessagesHandler(repository))
                createContext("/add_message", AddMessageHandler(context, repository))
                createContext("/guides", GuidesHandler(repository))
                createContext("/export_docs", ExportDocsHandler(context, repository))
                executor = executorService
            }
            server?.start()
            isRunning = true
            Log.d(TAG, "Local API Server started successfully on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Local API Server: ${e.message}", e)
        }
    }

    fun stop() {
        try {
            server?.stop(0)
            server = null
            executorService?.shutdownNow()
            executorService = null
            isRunning = false
            Log.d(TAG, "Local API Server stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Local API Server", e)
        }
    }

    // ============================================
    // HTTP UTILITIES
    // ============================================

    private fun sendResponse(exchange: HttpExchange, status: Int, mimeType: String, content: String) {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.apply {
            set("Content-Type", "$mimeType; charset=utf-8")
            set("Access-Control-Allow-Origin", "*")
            set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            set("Access-Control-Allow-Headers", "Content-Type")
            set("Connection", "keep-alive") // Keeps persistent TCP connection alive for low latency script calls
            set("Keep-Alive", "timeout=30, max=1000")
        }
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { os ->
            os.write(bytes)
            os.flush()
        }
    }

    private fun handleOptions(exchange: HttpExchange) {
        exchange.responseHeaders.apply {
            set("Access-Control-Allow-Origin", "*")
            set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            set("Access-Control-Allow-Headers", "Content-Type")
        }
        exchange.sendResponseHeaders(204, -1)
    }

    private fun readRequestBody(exchange: HttpExchange): String {
        return exchange.requestBody.use { inputStream ->
            val buffer = ByteArray(4096)
            val sb = StringBuilder()
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                sb.append(String(buffer, 0, bytesRead, StandardCharsets.UTF_8))
            }
            sb.toString()
        }
    }

    private fun parseUrlEncoded(body: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val pairs = body.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx != -1) {
                val key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name())
                val value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name())
                map[key] = value
            }
        }
        return map
    }

    // Handle extraction from simple JSON format like {"title":"X","content":"Y"}
    private fun extractFromJson(json: String, key: String): String {
        // Simple regex matching for key-value pair to avoid importing third-party libraries
        val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
        val match = pattern.find(json)
        if (match != null) {
            return match.groupValues[1]
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
        }
        return ""
    }

    // ============================================
    // HANDLERS
    // ============================================

    // Root handler: returns API guidance manual
    class RootHandler(private val port: Int) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            val htmlManual = """
                {
                  "status": "online",
                  "app": "LessonSync Programmatic Console API",
                  "port": $port,
                  "developer_guide": {
                    "overview": "Access your LessonSync data programmatically from Termux, ADB, or scripts on your phone.",
                    "endpoints": [
                      {
                        "path": "/notes",
                        "method": "GET",
                        "description": "Output all course notes in JSON format."
                      },
                      {
                        "path": "/add_note",
                        "method": "POST",
                        "description": "Inject/create an academic note.",
                        "params_url_encoded": "title=MyTitle&content=MarkdownText",
                        "params_json": "{\"title\":\"MyTitle\", \"content\":\"MarkdownText\"}"
                      },
                      {
                        "path": "/schedule",
                        "method": "GET",
                        "description": "Fetch current weekly class schedule."
                      },
                      {
                        "path": "/add_schedule",
                        "method": "POST",
                        "description": "Add lesson item to schedule.",
                        "params": "dayOfWeek=Monday&subject=Chemistry&startTime=09:00&endTime=10:30&classroom=Lab 3"
                      },
                      {
                        "path": "/messages",
                        "method": "GET",
                        "description": "Fetch archived classes/WhatsApp lessons feed."
                      },
                      {
                        "path": "/add_message",
                        "method": "POST",
                        "description": "Programmatically inject a lesson or prompt text to trigger AI features.",
                        "params": "chatName=Biology&sender=Dr.+ Henderson&messageText=Read+Chapter+5&isBusiness=true&isLesson=true&lessonSubject=Biology"
                      },
                      {
                        "path": "/guides",
                        "method": "GET",
                        "description": "Fetch all created study guides."
                      },
                      {
                        "path": "/export_docs",
                        "method": "GET",
                        "description": "Automatically download/export all notes, study guides and vocabulary lists into shared external storage (Download/LessonSync/)."
                      }
                    ],
                    "termux_usage_examples": {
                      "get_notes": "curl http://localhost:$port/notes",
                      "backup_all_docs_to_storage": "curl http://localhost:$port/export_docs",
                      "inject_new_note": "curl -X POST -d \"title=TermuxNote&content=CreatedFromCommandline\" http://localhost:$port/add_note"
                    }
                  }
                }
            """.trimIndent()
            sendResponse(exchange, 200, "application/json", htmlManual)
        }
    }

    // GET /notes API handler
    class NotesHandler(private val context: Context, private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            if (!exchange.requestMethod.equals("GET", ignoreCase = true)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}")
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val notes = repository.allNotes.first()
                    val jsonBuilder = StringBuilder("[")
                    notes.forEachIndexed { idx, note ->
                        val escapedContent = note.content
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "")
                        jsonBuilder.append("{")
                            .append("\"id\":${note.id},")
                            .append("\"title\":\"${note.title.replace("\"", "\\\"")}\",")
                            .append("\"content\":\"$escapedContent\",")
                            .append("\"updatedTimestamp\":${note.updatedTimestamp}")
                            .append("}")
                        if (idx < notes.size - 1) jsonBuilder.append(",")
                    }
                    jsonBuilder.append("]")
                    sendResponse(exchange, 200, "application/json", jsonBuilder.toString())
                } catch (e: Exception) {
                    sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
                }
            }
        }
    }

    // POST /add_note API handler
    class AddNoteHandler(private val context: Context, private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}")
                return
            }
            try {
                val body = readRequestBody(exchange)
                var title = ""
                var content = ""

                if (body.contains("{") && body.contains("}")) {
                    // Try to parse JSON format
                    title = extractFromJson(body, "title")
                    content = extractFromJson(body, "content")
                }

                if (title.isBlank()) {
                    // Fallback to url-encoded query format
                    val params = parseUrlEncoded(body)
                    title = params["title"] ?: ""
                    content = params["content"] ?: ""
                }

                if (title.isBlank()) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Title parameter is required\"}")
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val newId = repository.insertNote(NoteItem(title = title, content = content))
                    sendResponse(exchange, 201, "application/json", "{\"success\":true, \"id\":$newId, \"message\":\"Note programmatically added successfully!\"}")
                }
            } catch (e: Exception) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
            }
        }
    }

    // GET /schedule API handler
    class ScheduleHandler(private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            if (!exchange.requestMethod.equals("GET", ignoreCase = true)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}")
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sList = repository.weeklySchedule.first()
                    val jsonBuilder = StringBuilder("[")
                    sList.forEachIndexed { idx, item ->
                        jsonBuilder.append("{")
                            .append("\"id\":${item.id},")
                            .append("\"dayOfWeek\":\"${item.dayOfWeek}\",")
                            .append("\"subject\":\"${item.subject.replace("\"", "\\\"")}\",")
                            .append("\"startTime\":\"${item.startTime}\",")
                            .append("\"endTime\":\"${item.endTime}\",")
                            .append("\"classroom\":\"${item.classroom.replace("\"", "\\\"")}\"")
                            .append("}")
                        if (idx < sList.size - 1) jsonBuilder.append(",")
                    }
                    jsonBuilder.append("]")
                    sendResponse(exchange, 200, "application/json", jsonBuilder.toString())
                } catch (e: Exception) {
                    sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
                }
            }
        }
    }

    // POST /add_schedule API handler
    class AddScheduleHandler(private val context: Context, private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}")
                return
            }
            try {
                val body = readRequestBody(exchange)
                var dayOfWeek = ""
                var subject = ""
                var startTime = "09:00"
                var endTime = "10:30"
                var classroom = "General Room"

                if (body.contains("{") && body.contains("}")) {
                    dayOfWeek = extractFromJson(body, "dayOfWeek")
                    subject = extractFromJson(body, "subject")
                    startTime = extractFromJson(body, "startTime").ifBlank { "09:00" }
                    endTime = extractFromJson(body, "endTime").ifBlank { "10:30" }
                    classroom = extractFromJson(body, "classroom").ifBlank { "General Room" }
                }

                if (dayOfWeek.isBlank()) {
                    val params = parseUrlEncoded(body)
                    dayOfWeek = params["dayOfWeek"] ?: ""
                    subject = params["subject"] ?: ""
                    startTime = params["startTime"] ?: "09:00"
                    endTime = params["endTime"] ?: "10:30"
                    classroom = params["classroom"] ?: "General Room"
                }

                if (dayOfWeek.isBlank() || subject.isBlank()) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"dayOfWeek and subject are required\"}")
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val uid = repository.insertScheduleItem(
                        ScheduleItem(
                            dayOfWeek = dayOfWeek,
                            subject = subject,
                            startTime = startTime,
                            endTime = endTime,
                            classroom = classroom
                        )
                    )
                    sendResponse(exchange, 201, "application/json", "{\"success\":true, \"id\":$uid, \"message\":\"Schedule added successfully!\"}")
                }
            } catch (e: Exception) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
            }
        }
    }

    // GET /messages API handler
    class MessagesHandler(private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            if (!exchange.requestMethod.equals("GET", ignoreCase = true)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}")
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val msgs = repository.allMessages.first()
                    val jsonBuilder = StringBuilder("[")
                    msgs.forEachIndexed { idx, msg ->
                        val textEscaped = msg.messageText.replace("\"", "\\\"").replace("\n", "\\n")
                        jsonBuilder.append("{")
                            .append("\"id\":${msg.id},")
                            .append("\"chatName\":\"${msg.chatName.replace("\"", "\\\"")}\",")
                            .append("\"sender\":\"${msg.sender.replace("\"", "\\\"")}\",")
                            .append("\"messageText\":\"$textEscaped\",")
                            .append("\"timestamp\":${msg.timestamp},")
                            .append("\"isBusiness\":${msg.isBusiness},")
                            .append("\"isLesson\":${msg.isLesson},")
                            .append("\"lessonSubject\":\"${msg.lessonSubject}\"")
                            .append("}")
                        if (idx < msgs.size - 1) jsonBuilder.append(",")
                    }
                    jsonBuilder.append("]")
                    sendResponse(exchange, 200, "application/json", jsonBuilder.toString())
                } catch (e: Exception) {
                    sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
                }
            }
        }
    }

    // POST /add_message API Handler
    class AddMessageHandler(private val context: Context, private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}")
                return
            }
            try {
                val body = readRequestBody(exchange)
                var chatName = "Termux Feed"
                var sender = "Commandline Injector"
                var messageText = ""
                var isBusiness = false
                var isLesson = true
                var lessonSubject = "General"

                if (body.contains("{") && body.contains("}")) {
                    chatName = extractFromJson(body, "chatName").ifBlank { "Termux Terminal" }
                    sender = extractFromJson(body, "sender").ifBlank { "CLI User" }
                    messageText = extractFromJson(body, "messageText")
                    isBusiness = extractFromJson(body, "isBusiness").toBoolean()
                    isLesson = extractFromJson(body, "isLesson").toBoolean()
                    lessonSubject = extractFromJson(body, "lessonSubject").ifBlank { "General" }
                } else {
                    val params = parseUrlEncoded(body)
                    chatName = params["chatName"] ?: "Termux Terminal"
                    sender = params["sender"] ?: "CLI User"
                    messageText = params["messageText"] ?: ""
                    isBusiness = (params["isBusiness"] ?: "false").toBoolean()
                    isLesson = (params["isLesson"] ?: "true").toBoolean()
                    lessonSubject = params["lessonSubject"] ?: "General"
                }

                if (messageText.isBlank()) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"messageText parameter is required\"}")
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val mid = repository.insertMessage(
                        WhatsappMessage(
                            chatName = chatName,
                            sender = sender,
                            messageText = messageText,
                            timestamp = System.currentTimeMillis(),
                            isBusiness = isBusiness,
                            isLesson = isLesson,
                            lessonSubject = lessonSubject
                        )
                    )
                    sendResponse(exchange, 201, "application/json", "{\"success\":true, \"id\":$mid, \"message\":\"WhatsApp message was programmatically injected! App AI Ocr or summaries will re-analyze immediately.\"}")
                }
            } catch (e: Exception) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
            }
        }
    }

    // GET /guides API handler
    class GuidesHandler(private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            if (!exchange.requestMethod.equals("GET", ignoreCase = true)) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}")
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val guides = repository.allGuides.first()
                    val jsonBuilder = StringBuilder("[")
                    guides.forEachIndexed { idx, guide ->
                        val cleanContent = guide.content.replace("\"", "\\\"").replace("\n", "\\n")
                        jsonBuilder.append("{")
                            .append("\"id\":${guide.id},")
                            .append("\"subject\":\"${guide.subject}\",")
                            .append("\"topic\":\"${guide.topic.replace("\"", "\\\"")}\",")
                            .append("\"content\":\"$cleanContent\",")
                            .append("\"summary\":\"${guide.summary.replace("\"", "\\\"").replace("\n", "\\n")}\",")
                            .append("\"createdTimestamp\":${guide.createdTimestamp}")
                            .append("}")
                        if (idx < guides.size - 1) jsonBuilder.append(",")
                    }
                    jsonBuilder.append("]")
                    sendResponse(exchange, 200, "application/json", jsonBuilder.toString())
                } catch (e: Exception) {
                    sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
                }
            }
        }
    }

    // GET /export_docs API trigger handler
    class ExportDocsHandler(private val context: Context, private val repository: LessonSyncRepository) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                handleOptions(exchange)
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val notes = repository.allNotes.first()
                    val guides = repository.allGuides.first()
                    val exams = repository.allExams.first()
                    val words = repository.allWords.first()

                    val exportedCount = StorageHelper.exportAllData(context, notes, guides, exams, words)

                    val responseJson = """
                        {
                          "success": true,
                          "exported_count": $exportedCount,
                          "destination_directory": "/storage/emulated/0/Download/LessonSync/",
                          "sub_directories": ["Notes", "Guides", "Exams", "Vocabulary"],
                          "message": "All database academic elements have been compiled into raw Markdown files down into your physical mobile storage! Fully editable via Termux."
                        }
                    """.trimIndent()

                    sendResponse(exchange, 200, "application/json", responseJson)
                } catch (e: Exception) {
                    sendResponse(exchange, 500, "application/json", "{\"error\":\"${e.message}\"}")
                }
            }
        }
    }
}
