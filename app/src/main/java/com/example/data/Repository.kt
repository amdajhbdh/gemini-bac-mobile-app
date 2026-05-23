package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LessonSyncRepository(private val db: AppDatabase) {

    val allMessages: Flow<List<WhatsappMessage>> = db.messageDao().getAllMessages()
    val lessonHistory: Flow<List<WhatsappMessage>> = db.messageDao().getLessonHistory()
    val allTasks: Flow<List<TaskReminder>> = db.taskDao().getAllTasks()
    val weeklySchedule: Flow<List<ScheduleItem>> = db.scheduleDao().getWeeklySchedule()
    val allGuides: Flow<List<StudyGuide>> = db.studyDao().getAllGuides()
    val allNotes: Flow<List<NoteItem>> = db.noteDao().getAllNotes()
    val allExams: Flow<List<IndexedExam>> = db.languageDao().getAllExams()
    val allWords: Flow<List<WordItem>> = db.languageDao().getAllWords()
    val allQa: Flow<List<LanguageQa>> = db.languageDao().getAllQa()

    // Message actions
    suspend fun insertMessage(message: WhatsappMessage): Long {
        return db.messageDao().insertMessage(message)
    }

    suspend fun deleteMessage(id: Int) {
        db.messageDao().deleteMessageById(id)
    }

    // Task actions
    suspend fun insertTask(task: TaskReminder): Long {
        return db.taskDao().insertTask(task)
    }

    suspend fun updateTask(task: TaskReminder) {
        db.taskDao().updateTask(task)
    }

    suspend fun deleteTask(id: Int) {
        db.taskDao().deleteTaskById(id)
    }

    // Schedule actions
    suspend fun insertScheduleItem(item: ScheduleItem): Long {
        return db.scheduleDao().insertScheduleItem(item)
    }

    suspend fun deleteScheduleItem(item: ScheduleItem) {
        db.scheduleDao().deleteScheduleItem(item)
    }

    suspend fun clearSchedule() {
        db.scheduleDao().clearSchedule()
    }

    // Study Guide & Flashcards
    suspend fun insertStudyGuide(guide: StudyGuide): Long {
        return db.studyDao().insertGuide(guide)
    }

    fun getFlashcardsForGuide(guideId: Int): Flow<List<Flashcard>> {
        return db.studyDao().getFlashcardsForGuide(guideId)
    }

    suspend fun insertFlashcard(flashcard: Flashcard) {
        db.studyDao().insertFlashcard(flashcard)
    }

    suspend fun updateFlashcard(flashcard: Flashcard) {
        db.studyDao().updateFlashcard(flashcard)
    }

    fun getAllFlashcards(): Flow<List<Flashcard>> {
        return db.studyDao().getAllFlashcards()
    }

    // Notes actions
    suspend fun insertNote(note: NoteItem): Long {
        return db.noteDao().insertNote(note)
    }

    suspend fun updateNote(id: Int, title: String, content: String) {
        db.noteDao().updateNoteContent(id, title, content, System.currentTimeMillis())
    }

    suspend fun deleteNote(note: NoteItem) {
        db.noteDao().deleteNote(note)
    }

    // Language and Exams actions
    suspend fun insertExam(exam: IndexedExam): Long {
        return db.languageDao().insertExam(exam)
    }

    suspend fun deleteExam(id: Int) {
        db.languageDao().deleteExamById(id)
    }

    suspend fun insertWord(word: WordItem): Long {
        return db.languageDao().insertWord(word)
    }

    suspend fun deleteWord(id: Int) {
        db.languageDao().deleteWordById(id)
    }

    suspend fun insertQa(qa: LanguageQa): Long {
        return db.languageDao().insertQa(qa)
    }

    suspend fun clearAllQa() {
        db.languageDao().clearAllQa()
    }

    // Seed Data
    suspend fun seedDatabaseIfEmpty() {
        // Only seed if schedule is empty
        val currentSchedule = weeklySchedule.first()
        if (currentSchedule.isNotEmpty()) return

        // 1. Seed Schedule items
        val scheduleList = listOf(
            ScheduleItem(dayOfWeek = "Monday", subject = "Mathematics", startTime = "09:00", endTime = "10:30", classroom = "Room 301"),
            ScheduleItem(dayOfWeek = "Monday", subject = "Physics", startTime = "11:00", endTime = "12:30", classroom = "Lab A"),
            ScheduleItem(dayOfWeek = "Tuesday", subject = "Biology", startTime = "09:00", endTime = "10:30", classroom = "Room 102"),
            ScheduleItem(dayOfWeek = "Tuesday", subject = "Chemistry", startTime = "13:00", endTime = "14:30", classroom = "Lab B"),
            ScheduleItem(dayOfWeek = "Wednesday", subject = "Mathematics", startTime = "09:00", endTime = "10:30", classroom = "Room 301"),
            ScheduleItem(dayOfWeek = "Wednesday", subject = "English Literature", startTime = "11:00", endTime = "12:30", classroom = "Room 204"),
            ScheduleItem(dayOfWeek = "Thursday", subject = "Computer Science", startTime = "09:00", endTime = "10:30", classroom = "Room 402"),
            ScheduleItem(dayOfWeek = "Thursday", subject = "History", startTime = "14:00", endTime = "15:30", classroom = "Seminar Room 3"),
            ScheduleItem(dayOfWeek = "Friday", subject = "Physics", startTime = "11:00", endTime = "12:30", classroom = "Lab A"),
            ScheduleItem(dayOfWeek = "Friday", subject = "Chemistry", startTime = "13:00", endTime = "14:30", classroom = "Lab B")
        )
        for (item in scheduleList) {
            db.scheduleDao().insertScheduleItem(item)
        }

        // 2. Seed WhatsApp Messages
        val messages = listOf(
            WhatsappMessage(
                chatName = "Maths Lecture Series - Prof. Arthur",
                sender = "Prof. Arthur (Business)",
                messageText = "Hello Class, here are your notes on Limits & Derivatives. Please examine the attached diagram containing the graph of sin(x)/x as x approaches 0. Your lesson assignment is to complete problems 1-5 by tomorrow's lecture. Reminder: Exam is next Monday!",
                timestamp = System.currentTimeMillis() - 86400000 * 2,
                isBusiness = true,
                isLesson = true,
                lessonSubject = "Mathematics",
                imageUrl = "math_limits_diagram"
            ),
            WhatsappMessage(
                chatName = "Physics Study Group",
                sender = "Marcus",
                messageText = "Check this out - we did this velocity vs time vector experiment in Lab yesterday. I took a crop of the white board diagram of equations from horizontal projectile acceleration. Let's research this subject more and convert it into study flashcards.",
                timestamp = System.currentTimeMillis() - 86400000 * 1,
                isBusiness = false,
                isLesson = true,
                lessonSubject = "Physics",
                imageUrl = "physics_kinematics_diagram"
            ),
            WhatsappMessage(
                chatName = "Biology Private Tutoring",
                sender = "Dr. Henderson (Business)",
                messageText = "Hi Alice, here is the study plan for the Cellular Respiration sequence. Do study the Krebs cycle stages thoroughly as it is heavily featured in the upcoming exam. Please write active summary notes in your binder.",
                timestamp = System.currentTimeMillis() - 3600000 * 6,
                isBusiness = true,
                isLesson = true,
                lessonSubject = "Biology",
                imageUrl = "biology_krebs_diagram"
            ),
            WhatsappMessage(
                chatName = "Besties Team ✨",
                sender = "Sarah",
                messageText = "Hey! Let's get lunch in the campus cafeteria tomorrow after the Physics exam! Are we still meeting?",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                isBusiness = false,
                isLesson = false,
                lessonSubject = ""
            )
        )
        for (msg in messages) {
            db.messageDao().insertMessage(msg)
        }

        // 3. Seed some Tasks
        val tasks = listOf(
            TaskReminder(
                title = "Solve Math Problems 1-5",
                description = "Complete homework sent by Prof. Arthur via Business Chat. Focus on limits.",
                dueDate = "2026-05-24",
                dueTime = "09:00",
                isCompleted = false,
                relatedLessonId = 1,
                syncedWithSchedule = true
            ),
            TaskReminder(
                title = "Study Krebs Cycle Diagram",
                description = "Dr. Henderson's homework: Review metabolic stages and generate summaries.",
                dueDate = "2026-05-26",
                dueTime = "09:00",
                isCompleted = true,
                relatedLessonId = 3,
                syncedWithSchedule = true
            ),
            TaskReminder(
                title = "Review Physics Projectile Velocity",
                description = "Organize whiteboard notes shared by Marcus in the class group.",
                dueDate = "2026-05-25",
                dueTime = "11:00",
                isCompleted = false,
                relatedLessonId = 2,
                syncedWithSchedule = true
            )
        )
        for (task in tasks) {
            db.taskDao().insertTask(task)
        }

        // 4. Seed Notes - including deep documentation about keep and markdown format!
        val appNotesDoc = """
        # Notes & Lesson Strategy Guide 📓
        
        Welcome to **LessonSync Notes**! Here is how you can use this tab to manage your school academic workflow efficiently:
        
        ## 1. Local Notes
        * Create notes directly in-app to organize lesson lectures.
        * Use markdown headings, lists, and formatted syntax.
        * Edit or delete your notes anytime. Your updates are saved reactive-time locally.
        
        ## 2. Dynamic Export to Markdown (.md)
        Want your notes on your computer or an external folder?
        * Open any note, tap **"Export to Markdown (.md)"**.
        * Paste into Obsidian, Notion, or save to your local file explorer as raw `.md` documents.
        * LessonSync supports headers (`#`, `##`), bullet points, and code blocks for absolute styling!
        """.trimIndent()

        val googleKeepDoc = """
        # Syncing Notes to Google Keep & Google Notes 📒
        
        Google Keep (formerly Google Notes) is incredibly versatile. Although Google Keep does not have an open public API for automatic third-party syncs, you can set up a high-productivity workflow with LessonSync:
        
        ## 🚀 Two-Way Integration Protocol:
        1. **Fast-Share to Keep**: 
           Inside LessonSync, open any note or Study Guide, tap the **"Share Notes"** or **"Copy to Clipboard"** button. This directly packages the formatted text, and you can instantly Paste it into Keep.
        2. **Launch Google Keep Directly**:
           Use the quick **"Open Google Keep"** button built into our Notes tab. It invokes an Android implicit Intent to boot the Google Keep app straight away so you can paste your researched flashcards or summaries!
        3. **Formatting Advice**:
           Use Markdown formatting. When pasted into Google Keep, it maintains crystal clear structure. Use check-boxes in Keep for tasks derived in LessonSync.
        """.trimIndent()

        val subjectsDoc = """
        # Subjects Overview & Goals
        
        Current active subjects:
        * **Mathematics**: Differential derivatives, limits, integrals.
        * **Physics**: Mechanics, 2D kinematics, projectiles, forces.
        * **Biology**: Cellular respiration, Krebs Cycle, genetics, plant photosynthesis.
        
        ## Strategy:
        Use the **Study Assistant** tab to generate custom study sheets and flashcards. Test yourself twice weekly to raise retention metrics in our Analytics charts!
        """.trimIndent()

        db.noteDao().insertNote(NoteItem(title = "Notes & Lesson Strategy", content = appNotesDoc, updatedTimestamp = System.currentTimeMillis() - 3600000))
        db.noteDao().insertNote(NoteItem(title = "Google Keep / Notes Integration", content = googleKeepDoc, updatedTimestamp = System.currentTimeMillis() - 1200000))
        db.noteDao().insertNote(NoteItem(title = "Subjects Overview", content = subjectsDoc, updatedTimestamp = System.currentTimeMillis()))

        // 5. Seed some initial researched guides
        val mathGuideId = db.studyDao().insertGuide(StudyGuide(
            subject = "Mathematics",
            topic = "Limits and Continuity",
            content = """
            # Study Guide: Limits & Continuity 📐
            
            ## 1. What is a Limit?
            A limit is the fundamental building block of Calculus. It describes what value a function **approaches** as the inputs grow infinitesimally close to some point x = c, regardless of whether the function is defined at c.
            
            Mathematically written as:
            lim_{x -> c} f(x) = L
            
            ## 2. Properties of Limits
            * **Sum Law**: lim (f(x) + g(x)) = lim f(x) + lim g(x)
            * **Product Law**: lim (f(x) * g(x)) = lim f(x) * lim g(x)
            * **Quotient Law**: lim (f(x)/g(x)) = lim f(x) / lim g(x) (if denominator is not 0)
            
            ## 3. Continuity Requirements
            A function f(x) is continuous at a point x = c if and only if three criteria are met:
            1. f(c) is defined.
            2. lim_{x -> c} f(x) exists.
            3. lim_{x -> c} f(x) = f(c).
            
            ## 4. Continuity on Graphs
            No holes, jumps, or vertical asymptotes exists across the interval.
            """.trimIndent(),
            summary = "Fundamental explanation of what mathematical limits are, their computational laws, and the exact three criteria for continuity."
        ))

        db.studyDao().insertFlashcard(Flashcard(guideId = mathGuideId.toInt(), question = "What is a Limit in calculus?", answer = "The value a function approaches as its input approaches a specific point, regardless of functional definition there.", category = "Concept"))
        db.studyDao().insertFlashcard(Flashcard(guideId = mathGuideId.toInt(), question = "What are the three criteria of Continuity for f(x) at x=c?", answer = "1) f(c) is defined; 2) Lim as x->c exists; 3) Limit equals f(c).", category = "Formula"))
        db.studyDao().insertFlashcard(Flashcard(guideId = mathGuideId.toInt(), question = "State the Sum Law of Limits.", answer = "The limit of a sum is equal to the sum of the individual limits: lim(f+g) = lim(f) + lim(g).", category = "Law"))

        // 6. Seed Language Exams Knowledge Base
        db.languageDao().insertExam(IndexedExam(
            title = "French Level B1 Grammaire & Comprehension",
            subject = "French",
            content = """
            # Past Exam: French Level B1 Exam
            
            ## Section 1: Grammaire (Le Subjonctif)
            Complétez la phrase suivante: "Il faut que tu (apprendre) ________ tes leçons."
            Réponse: apprennes.
            
            "Bien que nous (avoir) ________ fini, nous attendons."
            Réponse: ayons.
            
            ## Section 2: Vocabulaire Essentiel
            * S'entraîner: To practice or train. Example: "Il s'entraîne tous les jours pour l'examen."
            * Réussir: To succeed or pass. Example: "Elle espère réussir son baccalauréat."
            * Quotidien: Daily. Example: "La lecture quotidienne enrichit l'esprit."
            """.trimIndent(),
            summary = "Sample intermediate B1 French exam containing grammar questions on the subjunctive mood and core action verbs."
        ))

        db.languageDao().insertExam(IndexedExam(
            title = "Spanish AP Conjugation & Conversation",
            subject = "Spanish",
            content = """
            # Past Exam: Spanish AP Language
            
            ## Section A: El Pretérito Imperfecto vs Pretérito Indefinido
            "Ayer yo (ir) ________ a la escuela cuando de repente (comenzar) ________ a llover."
            Respuesta: iba (imperfecto), comenzó (indefinido).
            
            ## Section B: Conversaciones Reales
            * El porvenir: The future. Ejemplo: "Debemos estudiar para asegurar nuestro porvenir."
            * El desafío: The challenge. Ejemplo: "Aprender español es un desafío fascinante."
            * Facilitar: To make easy, facilitate. Ejemplo: "Esta aplicación facilita el estudio."
            """.trimIndent(),
            summary = "Advanced placement Spanish mock exam testing preterite vs imperfect aspects and conversational vocabulary."
        ))

        // 7. Seed Word Lists (collection of words lists)
        db.languageDao().insertWord(WordItem(word = "Apprendre", definition = "To learn", example = "J'aime apprendre de nouvelles langues.", language = "French", difficulty = "Easy"))
        db.languageDao().insertWord(WordItem(word = "Réussir", definition = "To succeed / pass an exam", example = "Elle a réussi son examen de français !", language = "French", difficulty = "Medium"))
        db.languageDao().insertWord(WordItem(word = "Le Défi", definition = "The challenge", example = "Parler couramment est un vrai défi.", language = "French", difficulty = "Hard"))
        db.languageDao().insertWord(WordItem(word = "El Porvenir", definition = "The future", example = "Nadie puede predecir el porvenir.", language = "Spanish", difficulty = "Hard"))
        db.languageDao().insertWord(WordItem(word = "Ayer", definition = "Yesterday", example = "Ayer fuimos al museum de arte.", language = "Spanish", difficulty = "Easy"))
        db.languageDao().insertWord(WordItem(word = "Mejorar", definition = "To improve", example = "Quiero mejorar mi pronunciación.", language = "Spanish", difficulty = "Medium"))

        // 8. Seed Chat Q&A History
        db.languageDao().insertQa(LanguageQa(
            question = "What is the difference between El Pretérito Imperfecto and Indefinido in Spanish?",
            answer = "In Spanish, the **Pretérito Indefinido** is used for completed actions in the past with a specific timeframe (e.g., *ayer fuimos*). The **Pretérito Imperfecto** is used for ongoing, habitual actions, descriptions, or backgrounds in the past with no specific start/end (e.g., *yo iba a la escuela...*).",
            timestamp = System.currentTimeMillis() - 600000
        ))
    }
}
