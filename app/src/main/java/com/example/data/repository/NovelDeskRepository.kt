package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.UserPreferences
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.DocumentVersionEntity
import com.example.data.model.QuickNoteEntity
import com.example.data.model.StoryElementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NovelDeskRepository(
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) {
    private val bookDao = database.bookDao()
    private val chapterDao = database.chapterDao()
    private val quickNoteDao = database.quickNoteDao()
    private val storyElementDao = database.storyElementDao()
    private val versionDao = database.documentVersionDao()
    private val dailyLogDao = database.dailyWritingLogDao()

    val preferences: Flow<UserPreferences> = preferencesManager.preferencesFlow

    // --- Books ---
    val allActiveBooks: Flow<List<BookEntity>> = bookDao.getAllActiveBooks()
    val favoriteBooks: Flow<List<BookEntity>> = bookDao.getFavoriteBooks()
    val trashBooks: Flow<List<BookEntity>> = bookDao.getTrashBooks()
    val totalBookCount: Flow<Int> = bookDao.getBookCount()

    fun getBookById(id: Long): Flow<BookEntity?> = bookDao.getBookById(id)
    suspend fun getBookByIdSync(id: Long): BookEntity? = bookDao.getBookByIdSync(id)

    suspend fun createBook(book: BookEntity): Long = withContext(Dispatchers.IO) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun moveBookToTrash(id: Long) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        bookDao.setTrashStatus(id, true, now)
        chapterDao.setTrashStatusForBook(id, true, now)
    }

    suspend fun restoreBookFromTrash(id: Long) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        bookDao.setTrashStatus(id, false, now)
        chapterDao.setTrashStatusForBook(id, false, now)
    }

    suspend fun deleteBookPermanently(id: Long) = withContext(Dispatchers.IO) {
        bookDao.deletePermanently(id)
        chapterDao.deletePermanentlyForBook(id)
        storyElementDao.deleteElementsForBook(id)
    }

    // --- Chapters ---
    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>> = chapterDao.getChaptersForBook(bookId)
    fun getChapterById(id: Long): Flow<ChapterEntity?> = chapterDao.getChapterById(id)
    suspend fun getChapterByIdSync(id: Long): ChapterEntity? = chapterDao.getChapterByIdSync(id)
    val recentChapters: Flow<List<ChapterEntity>> = chapterDao.getRecentChapters(10)
    val allActiveChapters: Flow<List<ChapterEntity>> = chapterDao.getAllActiveChapters()
    val trashChapters: Flow<List<ChapterEntity>> = chapterDao.getTrashChapters()
    val totalWordsAllChapters: Flow<Int> = chapterDao.getTotalWordsAllChapters()
    val totalCharsAllChapters: Flow<Int> = chapterDao.getTotalCharsAllChapters()
    val totalChapterCount: Flow<Int> = chapterDao.getTotalChapterCount()
    fun getTotalWordsForBook(bookId: Long): Flow<Int> = chapterDao.getTotalWordsForBook(bookId)
    fun getChapterCountForBook(bookId: Long): Flow<Int> = chapterDao.getChapterCountForBook(bookId)

    suspend fun getPreviousChapter(bookId: Long, currentOrder: Int): ChapterEntity? = withContext(Dispatchers.IO) {
        chapterDao.getPreviousChapter(bookId, currentOrder)
    }

    suspend fun getNextChapter(bookId: Long, currentOrder: Int): ChapterEntity? = withContext(Dispatchers.IO) {
        chapterDao.getNextChapter(bookId, currentOrder)
    }

    suspend fun createChapter(chapter: ChapterEntity): Long = withContext(Dispatchers.IO) {
        val words = countWords(chapter.content)
        val id = chapterDao.insertChapter(
            chapter.copy(
                wordCount = words,
                charCount = chapter.content.length,
                updatedAt = System.currentTimeMillis()
            )
        )
        // Also update parent book's updatedAt
        val book = bookDao.getBookByIdSync(chapter.bookId)
        if (book != null) {
            bookDao.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
        }
        id
    }

    suspend fun updateChapterContent(id: Long, newContent: String, cursorPosition: Int = 0) = withContext(Dispatchers.IO) {
        val chapter = chapterDao.getChapterByIdSync(id) ?: return@withContext
        val oldWordCount = chapter.wordCount
        val newWordCount = countWords(newContent)
        val wordsDiff = newWordCount - oldWordCount

        chapterDao.updateChapter(
            chapter.copy(
                content = newContent,
                wordCount = newWordCount,
                charCount = newContent.length,
                lastCursorPosition = cursorPosition,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Update writing log if positive words added
        if (wordsDiff > 0) {
            recordWordsWrittenToday(wordsDiff)
        }

        // Update book updatedAt
        val book = bookDao.getBookByIdSync(chapter.bookId)
        if (book != null) {
            bookDao.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateChapterMetadata(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        chapterDao.updateChapter(chapter.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun moveChapterToTrash(id: Long) = withContext(Dispatchers.IO) {
        chapterDao.setTrashStatus(id, true, System.currentTimeMillis())
    }

    suspend fun restoreChapterFromTrash(id: Long) = withContext(Dispatchers.IO) {
        chapterDao.setTrashStatus(id, false, System.currentTimeMillis())
    }

    suspend fun deleteChapterPermanently(id: Long) = withContext(Dispatchers.IO) {
        chapterDao.deletePermanently(id)
    }

    suspend fun reorderChapters(chapters: List<ChapterEntity>) = withContext(Dispatchers.IO) {
        chapters.forEachIndexed { index, ch ->
            chapterDao.updateChapter(ch.copy(orderIndex = index + 1))
        }
    }

    // --- Quick Notes ---
    val allActiveNotes: Flow<List<QuickNoteEntity>> = quickNoteDao.getAllActiveNotes()
    val trashNotes: Flow<List<QuickNoteEntity>> = quickNoteDao.getTrashNotes()
    val totalWordsAllNotes: Flow<Int> = quickNoteDao.getTotalWordsAllNotes()

    fun getNoteById(id: Long): Flow<QuickNoteEntity?> = quickNoteDao.getNoteById(id)
    suspend fun getNoteByIdSync(id: Long): QuickNoteEntity? = quickNoteDao.getNoteByIdSync(id)

    suspend fun createNote(note: QuickNoteEntity): Long = withContext(Dispatchers.IO) {
        val words = countWords(note.content)
        quickNoteDao.insertNote(
            note.copy(
                wordCount = words,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateNote(note: QuickNoteEntity) = withContext(Dispatchers.IO) {
        val words = countWords(note.content)
        val oldNote = quickNoteDao.getNoteByIdSync(note.id)
        val oldWords = oldNote?.wordCount ?: 0
        val diff = words - oldWords
        if (diff > 0) {
            recordWordsWrittenToday(diff)
        }

        quickNoteDao.updateNote(
            note.copy(
                wordCount = words,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun moveNoteToTrash(id: Long) = withContext(Dispatchers.IO) {
        quickNoteDao.setTrashStatus(id, true, System.currentTimeMillis())
    }

    suspend fun restoreNoteFromTrash(id: Long) = withContext(Dispatchers.IO) {
        quickNoteDao.setTrashStatus(id, false, System.currentTimeMillis())
    }

    suspend fun deleteNotePermanently(id: Long) = withContext(Dispatchers.IO) {
        quickNoteDao.deletePermanently(id)
    }

    // --- Story Elements (Worldbuilding, Characters, Plot) ---
    fun getStoryElementsForBook(bookId: Long): Flow<List<StoryElementEntity>> =
        storyElementDao.getElementsForBook(bookId)

    suspend fun addStoryElement(element: StoryElementEntity): Long = withContext(Dispatchers.IO) {
        storyElementDao.insertElement(element)
    }

    suspend fun updateStoryElement(element: StoryElementEntity) = withContext(Dispatchers.IO) {
        storyElementDao.updateElement(element.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteStoryElement(id: Long) = withContext(Dispatchers.IO) {
        storyElementDao.deleteElement(id)
    }

    // --- Document Versions ---
    fun getVersionsForChapter(chapterId: Long): Flow<List<DocumentVersionEntity>> =
        versionDao.getVersionsForChapter(chapterId)

    fun getVersionsForNote(noteId: Long): Flow<List<DocumentVersionEntity>> =
        versionDao.getVersionsForNote(noteId)

    suspend fun saveVersionSnapshot(
        chapterId: Long = 0L,
        noteId: Long = 0L,
        title: String,
        content: String,
        summary: String = "Snapshot Manual"
    ): Long = withContext(Dispatchers.IO) {
        versionDao.insertVersion(
            DocumentVersionEntity(
                chapterId = chapterId,
                noteId = noteId,
                title = title,
                content = content,
                wordCount = countWords(content),
                summary = summary,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteVersion(id: Long) = withContext(Dispatchers.IO) {
        versionDao.deleteVersion(id)
    }

    // --- Writing Telemetry & Daily Logs ---
    val recentWritingLogs: Flow<List<DailyWritingLogEntity>> = dailyLogDao.getRecentLogs(7)

    fun getTodayLog(dateStr: String): Flow<DailyWritingLogEntity?> = dailyLogDao.getLogForDate(dateStr)

    suspend fun recordWordsWrittenToday(wordsAdded: Int) = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)
        val existing = dailyLogDao.getLogForDateSync(todayStr)
        if (existing != null) {
            dailyLogDao.insertOrUpdateLog(
                existing.copy(
                    wordsWritten = existing.wordsWritten + wordsAdded
                )
            )
        } else {
            dailyLogDao.insertOrUpdateLog(
                DailyWritingLogEntity(
                    dateStr = todayStr,
                    wordsWritten = wordsAdded,
                    targetWords = 1000
                )
            )
        }
    }

    // --- Global Search ---
    fun searchBooks(query: String): Flow<List<BookEntity>> = bookDao.searchBooks(query)
    fun searchChapters(query: String): Flow<List<ChapterEntity>> = chapterDao.searchChapters(query)
    fun searchNotes(query: String): Flow<List<QuickNoteEntity>> = quickNoteDao.searchNotes(query)

    // --- Trash Operations ---
    suspend fun emptyAllTrash() = withContext(Dispatchers.IO) {
        database.runInTransaction {
            database.openHelper.writableDatabase.execSQL("DELETE FROM chapters WHERE isInTrash = 1")
            database.openHelper.writableDatabase.execSQL("DELETE FROM books WHERE isInTrash = 1")
            database.openHelper.writableDatabase.execSQL("DELETE FROM quick_notes WHERE isInTrash = 1")
        }
    }

    // --- Settings & Preferences ---
    suspend fun updateTheme(mode: String) = preferencesManager.setThemeMode(mode)
    suspend fun updateEditorFont(font: String) = preferencesManager.setEditorFontFamily(font)
    suspend fun updateEditorFontSize(size: Float) = preferencesManager.setEditorFontSize(size)
    suspend fun updateEditorLineSpacing(spacing: Float) = preferencesManager.setEditorLineSpacing(spacing)
    suspend fun updateEditorMarginSize(margin: String) = preferencesManager.setEditorMarginSize(margin)
    suspend fun updateEditorTextAlign(align: String) = preferencesManager.setEditorTextAlign(align)
    suspend fun updateTypewriterMode(enabled: Boolean) = preferencesManager.setTypewriterMode(enabled)
    suspend fun updateDailyTarget(target: Int) = preferencesManager.setDailyWordTarget(target)
    suspend fun updateSecurityPin(pin: String) = preferencesManager.setSecurityPin(pin)
    suspend fun updateCustomBg(path: String, opacity: Float, position: String) =
        preferencesManager.setCustomBg(path, opacity, 0f, position)
    suspend fun clearCustomBg() = preferencesManager.clearCustomBg()
    suspend fun setLastActiveDocument(type: String, id: Long, bookId: Long = 0L, title: String = "") =
        preferencesManager.setLastActiveDocument(type, id, bookId, title)

    // --- Export / Backup / Restore ---
    suspend fun exportAllDataToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Novel Desk")
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())

        val booksArray = JSONArray()
        // We can query all books sync
        // Using database directly
        val booksCursor = database.query("SELECT * FROM books WHERE isInTrash = 0", null)
        while (booksCursor.moveToNext()) {
            val bookObj = JSONObject()
            val id = booksCursor.getLong(booksCursor.getColumnIndexOrThrow("id"))
            bookObj.put("id", id)
            bookObj.put("title", booksCursor.getString(booksCursor.getColumnIndexOrThrow("title")))
            bookObj.put("synopsis", booksCursor.getString(booksCursor.getColumnIndexOrThrow("synopsis")))
            bookObj.put("genre", booksCursor.getString(booksCursor.getColumnIndexOrThrow("genre")))
            bookObj.put("author", booksCursor.getString(booksCursor.getColumnIndexOrThrow("author")))
            bookObj.put("status", booksCursor.getString(booksCursor.getColumnIndexOrThrow("status")))
            bookObj.put("coverColorHex", booksCursor.getString(booksCursor.getColumnIndexOrThrow("coverColorHex")))

            // Chapters for this book
            val chCursor = database.query("SELECT * FROM chapters WHERE bookId = $id AND isInTrash = 0 ORDER BY orderIndex ASC", null)
            val chaptersArray = JSONArray()
            while (chCursor.moveToNext()) {
                val chObj = JSONObject()
                chObj.put("title", chCursor.getString(chCursor.getColumnIndexOrThrow("title")))
                chObj.put("chapterNumber", chCursor.getInt(chCursor.getColumnIndexOrThrow("chapterNumber")))
                chObj.put("content", chCursor.getString(chCursor.getColumnIndexOrThrow("content")))
                chObj.put("orderIndex", chCursor.getInt(chCursor.getColumnIndexOrThrow("orderIndex")))
                chaptersArray.put(chObj)
            }
            chCursor.close()
            bookObj.put("chapters", chaptersArray)

            booksArray.put(bookObj)
        }
        booksCursor.close()
        root.put("books", booksArray)

        // Notes
        val notesCursor = database.query("SELECT * FROM quick_notes WHERE isInTrash = 0", null)
        val notesArray = JSONArray()
        while (notesCursor.moveToNext()) {
            val noteObj = JSONObject()
            noteObj.put("title", notesCursor.getString(notesCursor.getColumnIndexOrThrow("title")))
            noteObj.put("content", notesCursor.getString(notesCursor.getColumnIndexOrThrow("content")))
            noteObj.put("category", notesCursor.getString(notesCursor.getColumnIndexOrThrow("category")))
            noteObj.put("isPinned", notesCursor.getInt(notesCursor.getColumnIndexOrThrow("isPinned")) == 1)
            notesArray.put(noteObj)
        }
        notesCursor.close()
        root.put("notes", notesArray)

        root.toString(2)
    }

    suspend fun restoreDataFromJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            val booksArray = root.optJSONArray("books") ?: JSONArray()
            for (i in 0 until booksArray.length()) {
                val bObj = booksArray.getJSONObject(i)
                val newBookId = bookDao.insertBook(
                    BookEntity(
                        title = bObj.optString("title", "Buku Impor"),
                        synopsis = bObj.optString("synopsis", ""),
                        genre = bObj.optString("genre", "Umum"),
                        author = bObj.optString("author", "Penulis"),
                        status = bObj.optString("status", "DRAFT"),
                        coverColorHex = bObj.optString("coverColorHex", "#1E293B")
                    )
                )

                val chArray = bObj.optJSONArray("chapters") ?: JSONArray()
                for (j in 0 until chArray.length()) {
                    val chObj = chArray.getJSONObject(j)
                    val content = chObj.optString("content", "")
                    chapterDao.insertChapter(
                        ChapterEntity(
                            bookId = newBookId,
                            title = chObj.optString("title", "Bab ${j + 1}"),
                            chapterNumber = chObj.optInt("chapterNumber", j + 1),
                            content = content,
                            wordCount = countWords(content),
                            charCount = content.length,
                            orderIndex = chObj.optInt("orderIndex", j + 1)
                        )
                    )
                }
            }

            val notesArray = root.optJSONArray("notes") ?: JSONArray()
            for (i in 0 until notesArray.length()) {
                val nObj = notesArray.getJSONObject(i)
                val content = nObj.optString("content", "")
                quickNoteDao.insertNote(
                    QuickNoteEntity(
                        title = nObj.optString("title", "Catatan Impor"),
                        content = content,
                        category = nObj.optString("category", "Ide Cerita"),
                        isPinned = nObj.optBoolean("isPinned", false),
                        wordCount = countWords(content)
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
}
