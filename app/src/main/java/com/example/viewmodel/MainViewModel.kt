package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.UserPreferences
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.QuickNoteEntity
import com.example.data.model.StoryElementEntity
import com.example.data.repository.NovelDeskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val preferencesManager = PreferencesManager(application)
    val repository = NovelDeskRepository(database, preferencesManager)

    val preferences: StateFlow<UserPreferences> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val activeBooks: StateFlow<List<BookEntity>> = repository.allActiveBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteBooks: StateFlow<List<BookEntity>> = repository.favoriteBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentChapters: StateFlow<List<ChapterEntity>> = repository.recentChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveChapters: StateFlow<List<ChapterEntity>> = repository.allActiveChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNotes: StateFlow<List<QuickNoteEntity>> = repository.allActiveNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashBooks: StateFlow<List<BookEntity>> = repository.trashBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashChapters: StateFlow<List<ChapterEntity>> = repository.trashChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashNotes: StateFlow<List<QuickNoteEntity>> = repository.trashNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWordsAllChapters: StateFlow<Int> = repository.totalWordsAllChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCharsAllChapters: StateFlow<Int> = repository.totalCharsAllChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalChapterCount: StateFlow<Int> = repository.totalChapterCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWordsAllNotes: StateFlow<Int> = repository.totalWordsAllNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentWritingLogs: StateFlow<List<DailyWritingLogEntity>> = repository.recentWritingLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global Search Query & State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // App Pin Lock State
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun unlockApp() {
        _isUnlocked.value = true
    }

    // Book Management Actions
    fun createBook(
        title: String,
        synopsis: String,
        genre: String,
        author: String,
        status: String,
        coverColorHex: String,
        coverUri: String = "",
        targetWords: Int = 50000,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val book = BookEntity(
                title = title.ifBlank { "Buku Baru" },
                synopsis = synopsis,
                genre = genre,
                author = author.ifBlank { "Penulis" },
                status = status,
                coverColorHex = coverColorHex,
                coverUri = coverUri,
                targetWordCount = targetWords
            )
            val id = repository.createBook(book)
            // Auto create first chapter for convenience
            repository.createChapter(
                ChapterEntity(
                    bookId = id,
                    title = "Bab 1",
                    chapterNumber = 1,
                    content = "# Bab 1\n\n",
                    orderIndex = 1
                )
            )
            onCreated(id)
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun toggleBookFavorite(book: BookEntity) {
        viewModelScope.launch {
            repository.updateBook(book.copy(isFavorite = !book.isFavorite))
        }
    }

    fun toggleBookPin(book: BookEntity) {
        viewModelScope.launch {
            repository.updateBook(book.copy(isPinned = !book.isPinned))
        }
    }

    fun moveBookToTrash(bookId: Long) {
        viewModelScope.launch {
            repository.moveBookToTrash(bookId)
        }
    }

    fun restoreBook(bookId: Long) {
        viewModelScope.launch {
            repository.restoreBookFromTrash(bookId)
        }
    }

    fun deleteBookPermanently(bookId: Long) {
        viewModelScope.launch {
            repository.deleteBookPermanently(bookId)
        }
    }

    // Quick Note Actions
    fun createNote(title: String, content: String, category: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createNote(
                QuickNoteEntity(
                    title = title.ifBlank { "Catatan Baru" },
                    content = content,
                    category = category
                )
            )
            onCreated(id)
        }
    }

    fun createQuickNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.createNote(
                QuickNoteEntity(
                    title = title.ifBlank { "Catatan Baru" },
                    content = content,
                    category = category
                )
            )
        }
    }

    fun updateQuickNote(note: QuickNoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun toggleNotePin(note: QuickNoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun moveNoteToTrash(noteId: Long) {
        viewModelScope.launch {
            repository.moveNoteToTrash(noteId)
        }
    }

    fun restoreNote(noteId: Long) {
        viewModelScope.launch {
            repository.restoreNoteFromTrash(noteId)
        }
    }

    fun deleteNotePermanently(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNotePermanently(noteId)
        }
    }

    // Chapter Actions
    fun moveChapterToTrash(chapterId: Long) {
        viewModelScope.launch {
            repository.moveChapterToTrash(chapterId)
        }
    }

    fun restoreChapter(chapterId: Long) {
        viewModelScope.launch {
            repository.restoreChapterFromTrash(chapterId)
        }
    }

    fun deleteChapterPermanently(chapterId: Long) {
        viewModelScope.launch {
            repository.deleteChapterPermanently(chapterId)
        }
    }

    // Settings
    fun updateTheme(themeMode: String) {
        viewModelScope.launch {
            repository.updateTheme(themeMode)
        }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch {
            repository.updateDailyTarget(target)
        }
    }

    fun updateDailyWordTarget(target: Int) {
        updateDailyTarget(target)
    }

    fun updateSecurityPin(pin: String) {
        viewModelScope.launch {
            repository.updateSecurityPin(pin)
        }
    }

    fun updateCustomBackground(path: String, opacity: Float, position: String) {
        viewModelScope.launch {
            repository.updateCustomBg(path, opacity, position)
        }
    }

    fun clearCustomBackground() {
        viewModelScope.launch {
            repository.clearCustomBg()
        }
    }
}
