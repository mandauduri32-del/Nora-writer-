package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.DocumentVersionEntity
import com.example.data.model.QuickNoteEntity
import com.example.data.model.StoryElementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE isInTrash = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isInTrash = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookById(id: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookByIdSync(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET isInTrash = :inTrash, updatedAt = :timestamp WHERE id = :id")
    suspend fun setTrashStatus(id: Long, inTrash: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT * FROM books WHERE isInTrash = 1 ORDER BY updatedAt DESC")
    fun getTrashBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isInTrash = 0 AND (title LIKE '%' || :query || '%' OR synopsis LIKE '%' || :query || '%')")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books WHERE isInTrash = 0")
    fun getBookCount(): Flow<Int>
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND isInTrash = 0 ORDER BY orderIndex ASC, chapterNumber ASC")
    fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    fun getChapterById(id: Long): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterByIdSync(id: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND isInTrash = 0 AND orderIndex < :currentOrder ORDER BY orderIndex DESC LIMIT 1")
    suspend fun getPreviousChapter(bookId: Long, currentOrder: Int): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND isInTrash = 0 AND orderIndex > :currentOrder ORDER BY orderIndex ASC LIMIT 1")
    suspend fun getNextChapter(bookId: Long, currentOrder: Int): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE isInTrash = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentChapters(limit: Int = 10): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET isInTrash = :inTrash, updatedAt = :timestamp WHERE id = :id")
    suspend fun setTrashStatus(id: Long, inTrash: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE chapters SET isInTrash = :inTrash, updatedAt = :timestamp WHERE bookId = :bookId")
    suspend fun setTrashStatusForBook(bookId: Long, inTrash: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deletePermanentlyForBook(bookId: Long)

    @Query("SELECT * FROM chapters WHERE isInTrash = 1 ORDER BY updatedAt DESC")
    fun getTrashChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE isInTrash = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchChapters(query: String): Flow<List<ChapterEntity>>

    @Query("SELECT COALESCE(SUM(wordCount), 0) FROM chapters WHERE isInTrash = 0")
    fun getTotalWordsAllChapters(): Flow<Int>

    @Query("SELECT COALESCE(SUM(charCount), 0) FROM chapters WHERE isInTrash = 0")
    fun getTotalCharsAllChapters(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chapters WHERE isInTrash = 0")
    fun getTotalChapterCount(): Flow<Int>

    @Query("SELECT * FROM chapters WHERE isInTrash = 0")
    fun getAllActiveChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT COALESCE(SUM(wordCount), 0) FROM chapters WHERE bookId = :bookId AND isInTrash = 0")
    fun getTotalWordsForBook(bookId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId AND isInTrash = 0")
    fun getChapterCountForBook(bookId: Long): Flow<Int>
}

@Dao
interface QuickNoteDao {
    @Query("SELECT * FROM quick_notes WHERE isInTrash = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<QuickNoteEntity>>

    @Query("SELECT * FROM quick_notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: Long): Flow<QuickNoteEntity?>

    @Query("SELECT * FROM quick_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteByIdSync(id: Long): QuickNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuickNoteEntity): Long

    @Update
    suspend fun updateNote(note: QuickNoteEntity)

    @Query("UPDATE quick_notes SET isInTrash = :inTrash, updatedAt = :timestamp WHERE id = :id")
    suspend fun setTrashStatus(id: Long, inTrash: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM quick_notes WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT * FROM quick_notes WHERE isInTrash = 1 ORDER BY updatedAt DESC")
    fun getTrashNotes(): Flow<List<QuickNoteEntity>>

    @Query("SELECT * FROM quick_notes WHERE isInTrash = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun searchNotes(query: String): Flow<List<QuickNoteEntity>>

    @Query("SELECT COALESCE(SUM(wordCount), 0) FROM quick_notes WHERE isInTrash = 0")
    fun getTotalWordsAllNotes(): Flow<Int>
}

@Dao
interface StoryElementDao {
    @Query("SELECT * FROM story_elements WHERE bookId = :bookId ORDER BY orderIndex ASC, updatedAt DESC")
    fun getElementsForBook(bookId: Long): Flow<List<StoryElementEntity>>

    @Query("SELECT * FROM story_elements WHERE bookId = :bookId AND type = :type ORDER BY orderIndex ASC")
    fun getElementsByType(bookId: Long, type: String): Flow<List<StoryElementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElement(element: StoryElementEntity): Long

    @Update
    suspend fun updateElement(element: StoryElementEntity)

    @Query("DELETE FROM story_elements WHERE id = :id")
    suspend fun deleteElement(id: Long)

    @Query("DELETE FROM story_elements WHERE bookId = :bookId")
    suspend fun deleteElementsForBook(bookId: Long)
}

@Dao
interface DocumentVersionDao {
    @Query("SELECT * FROM document_versions WHERE chapterId = :chapterId ORDER BY timestamp DESC")
    fun getVersionsForChapter(chapterId: Long): Flow<List<DocumentVersionEntity>>

    @Query("SELECT * FROM document_versions WHERE noteId = :noteId ORDER BY timestamp DESC")
    fun getVersionsForNote(noteId: Long): Flow<List<DocumentVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: DocumentVersionEntity): Long

    @Query("DELETE FROM document_versions WHERE id = :id")
    suspend fun deleteVersion(id: Long)
}

@Dao
interface DailyWritingLogDao {
    @Query("SELECT * FROM daily_writing_logs ORDER BY dateStr DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 7): Flow<List<DailyWritingLogEntity>>

    @Query("SELECT * FROM daily_writing_logs WHERE dateStr = :dateStr LIMIT 1")
    fun getLogForDate(dateStr: String): Flow<DailyWritingLogEntity?>

    @Query("SELECT * FROM daily_writing_logs WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getLogForDateSync(dateStr: String): DailyWritingLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: DailyWritingLogEntity)
}
