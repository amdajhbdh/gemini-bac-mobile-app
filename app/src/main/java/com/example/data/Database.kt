package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ============================================
// ENTITIES
// ============================================

@Entity(tableName = "whatsapp_messages")
data class WhatsappMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatName: String,
    val sender: String,
    val messageText: String,
    val timestamp: Long,
    val isBusiness: Boolean,
    val isLesson: Boolean,
    val lessonSubject: String,
    val imageUrl: String? = null // Simulated scan JPEG or URI
)

@Entity(tableName = "task_reminders")
data class TaskReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dueDate: String, // e.g. "2026-05-24"
    val dueTime: String, // e.g. "14:15"
    val isCompleted: Boolean = false,
    val relatedLessonId: Int? = null,
    val syncedWithSchedule: Boolean = false
)

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val subject: String,
    val startTime: String, // "09:00"
    val endTime: String, // "10:30"
    val classroom: String
)

@Entity(tableName = "study_guides")
data class StudyGuide(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val topic: String,
    val content: String, // Detailed markdown notes
    val summary: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val guideId: Int,
    val question: String,
    val answer: String,
    val category: String = "General",
    val checkedCount: Int = 0,
    val correctCount: Int = 0
)

@Entity(tableName = "note_items")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "indexed_exams")
data class IndexedExam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String, // e.g. "French", "Spanish", "English"
    val content: String, // Full text of the exam paper
    val summary: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "word_items")
data class WordItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val definition: String,
    val example: String = "",
    val language: String, // Course target language
    val difficulty: String = "Medium",
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "language_qa")
data class LanguageQa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ============================================
// DAOS
// ============================================

@Dao
interface MessageDao {
    @Query("SELECT * FROM whatsapp_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<WhatsappMessage>>

    @Query("SELECT * FROM whatsapp_messages WHERE isLesson = 1 ORDER BY timestamp DESC")
    fun getLessonHistory(): Flow<List<WhatsappMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: WhatsappMessage): Long

    @Query("DELETE FROM whatsapp_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    @Query("DELETE FROM whatsapp_messages")
    suspend fun clearAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_reminders ORDER BY dueDate ASC, dueTime ASC")
    fun getAllTasks(): Flow<List<TaskReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskReminder): Long

    @Update
    suspend fun updateTask(task: TaskReminder)

    @Query("DELETE FROM task_reminders WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items ORDER BY CASE dayOfWeek " +
           "WHEN 'Monday' THEN 1 " +
           "WHEN 'Tuesday' THEN 2 " +
           "WHEN 'Wednesday' THEN 3 " +
           "WHEN 'Thursday' THEN 4 " +
           "WHEN 'Friday' THEN 5 " +
           "WHEN 'Saturday' THEN 6 " +
           "WHEN 'Sunday' THEN 7 END, startTime ASC")
    fun getWeeklySchedule(): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItem): Long

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItem)

    @Query("DELETE FROM schedule_items")
    suspend fun clearSchedule()
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_guides ORDER BY createdTimestamp DESC")
    fun getAllGuides(): Flow<List<StudyGuide>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuide(guide: StudyGuide): Long

    @Query("SELECT * FROM flashcards WHERE guideId = :guideId")
    fun getFlashcardsForGuide(guideId: Int): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Query("SELECT * FROM flashcards")
    fun getAllFlashcards(): Flow<List<Flashcard>>
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_items ORDER BY updatedTimestamp DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem): Long

    @Delete
    suspend fun deleteNote(note: NoteItem)

    @Query("UPDATE note_items SET title = :title, content = :content, updatedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateNoteContent(id: Int, title: String, content: String, timestamp: Long)
}

@Dao
interface LanguageDao {
    @Query("SELECT * FROM indexed_exams ORDER BY createdTimestamp DESC")
    fun getAllExams(): Flow<List<IndexedExam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: IndexedExam): Long

    @Query("DELETE FROM indexed_exams WHERE id = :id")
    suspend fun deleteExamById(id: Int)

    @Query("SELECT * FROM word_items ORDER BY word ASC")
    fun getAllWords(): Flow<List<WordItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordItem): Long

    @Query("DELETE FROM word_items WHERE id = :id")
    suspend fun deleteWordById(id: Int)

    @Query("SELECT * FROM language_qa ORDER BY timestamp ASC")
    fun getAllQa(): Flow<List<LanguageQa>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQa(qa: LanguageQa): Long

    @Query("DELETE FROM language_qa")
    suspend fun clearAllQa()
}

// ============================================
// DATABASE HOLDER
// ============================================

@Database(
    entities = [
        WhatsappMessage::class,
        TaskReminder::class,
        ScheduleItem::class,
        StudyGuide::class,
        Flashcard::class,
        NoteItem::class,
        IndexedExam::class,
        WordItem::class,
        LanguageQa::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun taskDao(): TaskDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun studyDao(): StudyDao
    abstract fun noteDao(): NoteDao
    abstract fun languageDao(): LanguageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lessonsync_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
