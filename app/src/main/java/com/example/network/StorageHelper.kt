package com.example.network

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets

object StorageHelper {

    /**
     * Programmatically generate and save a highly recognizable QR block pattern
     * as an image file directly to '/storage/emulated/0/Download/LessonSync/'.
     */
    fun saveMockQrToStorage(context: Context, isPersonal: Boolean): File? {
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LessonSync")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val filename = if (isPersonal) "Lessonsync_Personal_QR.png" else "Lessonsync_Business_QR.png"
        val file = File(folder, filename)
        return try {
            val size = 512
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()

            // Background
            paint.color = AndroidColor.WHITE
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

            // Draw QR corners
            paint.color = AndroidColor.BLACK
            // Top-left corner block
            canvas.drawRect(20f, 20f, 130f, 130f, paint)
            paint.color = AndroidColor.WHITE
            canvas.drawRect(35f, 35f, 115f, 115f, paint)
            paint.color = AndroidColor.BLACK
            canvas.drawRect(50f, 50f, 100f, 100f, paint)

            // Top-right corner block
            canvas.drawRect(382f, 20f, 492f, 130f, paint)
            paint.color = AndroidColor.WHITE
            canvas.drawRect(397f, 35f, 477f, 115f, paint)
            paint.color = AndroidColor.BLACK
            canvas.drawRect(412f, 50f, 462f, 100f, paint)

            // Bottom-left corner block
            canvas.drawRect(20f, 382f, 130f, 492f, paint)
            paint.color = AndroidColor.WHITE
            canvas.drawRect(35f, 397f, 115f, 477f, paint)
            paint.color = AndroidColor.BLACK
            canvas.drawRect(50f, 412f, 100f, 462f, paint)

            // Draw beautiful dummy scanning blocks
            val random = java.util.Random(101010L)
            for (x in 2..30) {
                for (y in 2..30) {
                    if ((x < 10 && y < 10) || (x > 22 && y < 10) || (x < 10 && y > 22)) {
                        continue // skip finder pattern areas
                    }
                    if (random.nextBoolean()) {
                        paint.color = AndroidColor.BLACK
                        canvas.drawRect((x * 16).toFloat(), (y * 16).toFloat(), ((x + 1) * 16).toFloat(), ((y + 1) * 16).toFloat(), paint)
                    }
                }
            }

            // Draw central branding block
            paint.color = if (isPersonal) AndroidColor.parseColor("#2E7D32") else AndroidColor.parseColor("#0277BD")
            canvas.drawRect(210f, 210f, 302f, 302f, paint)
            paint.color = AndroidColor.WHITE
            paint.textSize = 28f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("LS", 256f, 266f, paint)

            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            // Send scanner system media scan request for the saved file so picker can see it
            try {
                val mediaScanValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/LessonSync")
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    } else {
                        put(MediaStore.MediaColumns.DATA, file.absolutePath)
                    }
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaScanValues)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save a single note/guide as a markdown file into the device's public Download/LessonSync folder
     */
    fun saveMarkdownFile(
        context: Context,
        subFolder: String, // "Notes", "Guides", "Exams"
        title: String,
        content: String
    ): Boolean {
        // Sanitize title for filename compatibility
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9-_ ]"), "").trim().replace(" ", "_")
        val filename = "${sanitizedTitle}.md"
        val folderPath = "${Environment.DIRECTORY_DOWNLOADS}/LessonSync/$subFolder"

        return try {
            val contentResolver = context.contentResolver
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Modern Scoped Storage pattern (API 29+)
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/markdown")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                
                // Check if file already exists to avoid duplication crashed inserts
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(filename, "$folderPath/")
                val cursor = contentResolver.query(collection, arrayOf(MediaStore.MediaColumns._ID), selection, selectionArgs, null)
                var uri = if (cursor != null && cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    cursor.close()
                    // Delete the existing one to overwrite
                    val deleteUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    contentResolver.delete(deleteUri, "${MediaStore.MediaColumns._ID} = ?", arrayOf(id.toString()))
                    contentResolver.insert(collection, contentValues)
                } else {
                    cursor?.close()
                    contentResolver.insert(collection, contentValues)
                }

                if (uri != null) {
                    context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).channel.use { channel ->
                            val bytes = content.toByteArray(StandardCharsets.UTF_8)
                            val buffer = ByteBuffer.wrap(bytes)
                            channel.write(buffer)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                    true
                } else {
                    false
                }
            } else {
                // Legacy / Direct storage fallback for traditional environments
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destDir = File(downloadsDir, "LessonSync/$subFolder")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                val destFile = File(destDir, filename)
                FileOutputStream(destFile).channel.use { channel ->
                    val bytes = content.toByteArray(StandardCharsets.UTF_8)
                    val buffer = ByteBuffer.wrap(bytes)
                    channel.write(buffer)
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Direct write fallback as ultimate failover
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destDir = File(downloadsDir, "LessonSync/$subFolder")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                val destFile = File(destDir, filename)
                FileOutputStream(destFile).channel.use { channel ->
                    val bytes = content.toByteArray(StandardCharsets.UTF_8)
                    val buffer = ByteBuffer.wrap(bytes)
                    channel.write(buffer)
                }
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
        }
    }

    /**
     * Batch export all academic contents of LessonSync into the structured local storage folder
     */
    suspend fun exportAllData(
        context: Context,
        notes: List<com.example.data.NoteItem>,
        guides: List<com.example.data.StudyGuide>,
        exams: List<com.example.data.IndexedExam>,
        words: List<com.example.data.WordItem>
    ): Int {
        var count = 0
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())

        // 1. Export Notes
        for (note in notes) {
            val noteBody = """
                # ${note.title}
                *Last Updated: ${dateFormat.format(java.util.Date(note.updatedTimestamp))}*
                
                ---
                
                ${note.content}
            """.trimIndent()
            if (saveMarkdownFile(context, "Notes", note.title, noteBody)) {
                count++
            }
        }

        // 2. Export Study Guides
        for (guide in guides) {
            val guideBody = """
                # Study Guide: ${guide.topic}
                *Subject: ${guide.subject}*
                *Created: ${dateFormat.format(java.util.Date(guide.createdTimestamp))}*
                
                ---
                
                ## Summary Overview
                ${guide.summary}
                
                ---
                
                ## Full Guide Content
                ${guide.content}
            """.trimIndent()
            if (saveMarkdownFile(context, "Guides", "${guide.subject}_${guide.topic}", guideBody)) {
                count++
            }
        }

        // 3. Export Exams
        for (exam in exams) {
            val examBody = """
                # Past Exam: ${exam.title}
                *Subject: ${exam.subject}*
                
                ---
                
                ## Summary Analysis
                ${exam.summary}
                
                ---
                
                ## Full Exam Materials
                ${exam.content}
            """.trimIndent()
            if (saveMarkdownFile(context, "Exams", "${exam.subject}_${exam.title}", examBody)) {
                count++
            }
        }

        // 4. Export Vocabulary Lists
        if (words.isNotEmpty()) {
            val vocabularyBody = StringBuilder().apply {
                append("# Vocabulary & Word Items List\n")
                append("*Generated: ${dateFormat.format(java.util.Date(System.currentTimeMillis()))}*\n\n")
                append("| Word | Definition | Example Sentence | Language | Difficulty |\n")
                append("| ---- | ---------- | ---------------- | -------- | ---------- |\n")
                for (word in words) {
                    append("| **${word.word}** | ${word.definition} | *${word.example}* | ${word.language} | ${word.difficulty} |\n")
                }
            }.toString()

            if (saveMarkdownFile(context, "Vocabulary", "All_Words_Reference", vocabularyBody)) {
                count++
            }
        }

        return count
    }
}
