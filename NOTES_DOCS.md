# 📝 LessonSync Notes & Language Hub Documentation

Welcome to **LessonSync's** internal and external documentation! This guide outlines how the application manages, processes, and synchronizes academic notes, language past exams, word collections, and daily school schedules.

---

## 📂 1. Notes Inside the Application

The app includes a dedicated, offline-first secure **Notes** tab powered by **Jetpack Room Database**. 
- **Auto-Seeding**: Upon first installation, the app imports a rich documentation note ("*App Documentation: Language Sync Guide*") directly into your workspace.
- **Manage Lessons**: You can create, edit, search, and delete lesson summaries, grammar explanations, and study items directly inside the app.
- **Intelligent Synchronization**:
  - The notes context is automatically compiled and provided to the **AI Language Coach**.
  - When asking questions, the Coach will reference your local class notes to provide context-aware feedback!

---

## 🗂️ 2. Formatting Notes in Markdown (`.md`)

LessonSync natively formats all AI-generated study guides, exam summaries, and Q&A responses in **GitHub Flavored Markdown (GFM)**:
- **Visual Rhythm**: Clear structural headings (`#`, `##`), itemized checklists (`- [ ]`), highlighting, and horizontal divider bars.
- **Code Blocks**: Formats dictionary alignments, conjugations, and code blocks with high legibility.
- **Interoperability**: You can easily copy and paste any study guide or AI chat card directly into raw markdown files or text editors!

---

## 🗒️ 3. Google Keep & Google Notes Integration

Because Google Notes (Keep) uses the standard cloud-synced account integrations, LessonSync supports simple interoperability options:

### A. Quick-Copy & Export
- Every card in your Notes tab has an active **Copy to Clipboard** icon.
- You can copy rich formatted notes or bullet points with a single tap and painlessly paste them straight into **Google Keep (Keep.google.com)** or **Google Chrome Extensions**.

### B. Syncing Task Reminders to Google Calendar/Notes
- When you use the **AI Whiteboard/Lesson OCR** scanner, the Gemini engine parses the whiteboard math/text and automatically suggests calendar tasks with due dates (e.g. "*Solve Physics Trajectory Homework*", "*Practice June French Subjunctive*").
- These are saved locally as interactive reminders, matching your weekly classroom schedule times securely!

---

## 🎓 4. Language Knowledge Base & Exam Parser

### Features Overview:
1. **Receive Questions**: Enter tricky questions about subjunctive moods or preterit aspects to the Coach chat, which references your custom indexed documents.
2. **Indexed Exams**: Copy/paste past exam contents. Store exam papers in the database.
3. **AI Extractor**: Tap the orange **AI Extract** button next to any past exam. LessonSync calls Gemini to parse the paper and instantly:
   - Populates your **Word Glossary** with high-density words.
   - Appends interactive **Flashcards** into your quiz decks.
4. **Word Catalog**: Review easily accessible words, filter by language (French, Spanish), search definitions, and run revision tests!

---
*(Compiled for Google AI Studio Build - 2026)*
