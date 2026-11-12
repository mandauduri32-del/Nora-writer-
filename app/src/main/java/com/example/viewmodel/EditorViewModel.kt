package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.UserPreferences
import com.example.data.model.ChapterEntity
import com.example.data.model.DocumentVersionEntity
import com.example.data.model.QuickNoteEntity
import com.example.data.repository.NovelDeskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SaveStatus {
    SAVED, SAVING, MODIFIED
}

data class EditorStats(
    val words: Int = 0,
    val characters: Int = 0,
    val charactersNoSpaces: Int = 0,
    val paragraphs: Int = 0,
    val readingTimeMinutes: Int = 0
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val preferencesManager = PreferencesManager(application)
    val repository = NovelDeskRepository(database, preferencesManager)

    val preferences: StateFlow<UserPreferences> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    // Document identification
    var docType: String = "CHAPTER" // "CHAPTER" or "NOTE"
        private set
    var docId: Long = 0L
        private set
    var bookId: Long = 0L
        private set

    private val _documentTitle = MutableStateFlow("")
    val documentTitle: StateFlow<String> = _documentTitle.asStateFlow()

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue: StateFlow<TextFieldValue> = _textFieldValue.asStateFlow()

    private val _saveStatus = MutableStateFlow(SaveStatus.SAVED)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    private val _stats = MutableStateFlow(EditorStats())
    val stats: StateFlow<EditorStats> = _stats.asStateFlow()

    // Undo / Redo Stacks
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Modes
    private val _isFocusMode = MutableStateFlow(false)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

    private val _isReadMode = MutableStateFlow(false)
    val isReadMode: StateFlow<Boolean> = _isReadMode.asStateFlow()

    private val _isPreviewMarkdown = MutableStateFlow(false)
    val isPreviewMarkdown: StateFlow<Boolean> = _isPreviewMarkdown.asStateFlow()

    // Search and Replace State
    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()

    private val _matchIndices = MutableStateFlow<List<Int>>(emptyList())
    val matchIndices: StateFlow<List<Int>> = _matchIndices.asStateFlow()

    private val _currentMatchIndex = MutableStateFlow(0)
    val currentMatchIndex: StateFlow<Int> = _currentMatchIndex.asStateFlow()

    // Previous and Next Chapter info for quick navigation
    private val _prevChapter = MutableStateFlow<ChapterEntity?>(null)
    val prevChapter: StateFlow<ChapterEntity?> = _prevChapter.asStateFlow()

    private val _nextChapter = MutableStateFlow<ChapterEntity?>(null)
    val nextChapter: StateFlow<ChapterEntity?> = _nextChapter.asStateFlow()

    // Version history
    private val _versions = MutableStateFlow<List<DocumentVersionEntity>>(emptyList())
    val versions: StateFlow<List<DocumentVersionEntity>> = _versions.asStateFlow()

    private var autoSaveJob: Job? = null

    fun loadDocument(type: String, id: Long) {
        docType = type
        docId = id
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false

        viewModelScope.launch {
            if (type == "CHAPTER") {
                val chapter = repository.getChapterByIdSync(id)
                if (chapter != null) {
                    bookId = chapter.bookId
                    _documentTitle.value = chapter.title
                    _textFieldValue.value = TextFieldValue(
                        text = chapter.content,
                        selection = TextRange(chapter.lastCursorPosition.coerceIn(0, chapter.content.length))
                    )
                    undoStack.add(chapter.content)
                    updateStats(chapter.content)
                    loadAdjacentChapters(chapter.bookId, chapter.orderIndex)
                    loadVersions(chapterId = chapter.id)

                    repository.setLastActiveDocument("CHAPTER", chapter.id, chapter.bookId, chapter.title)
                }
            } else {
                val note = repository.getNoteByIdSync(id)
                if (note != null) {
                    _documentTitle.value = note.title
                    _textFieldValue.value = TextFieldValue(note.content)
                    undoStack.add(note.content)
                    updateStats(note.content)
                    loadVersions(noteId = note.id)

                    repository.setLastActiveDocument("NOTE", note.id, 0L, note.title)
                }
            }
            _saveStatus.value = SaveStatus.SAVED
        }
    }

    private suspend fun loadAdjacentChapters(bookId: Long, currentOrder: Int) {
        _prevChapter.value = repository.getPreviousChapter(bookId, currentOrder)
        _nextChapter.value = repository.getNextChapter(bookId, currentOrder)
    }

    private fun loadVersions(chapterId: Long = 0L, noteId: Long = 0L) {
        viewModelScope.launch {
            if (chapterId > 0) {
                repository.getVersionsForChapter(chapterId).collect {
                    _versions.value = it
                }
            } else if (noteId > 0) {
                repository.getVersionsForNote(noteId).collect {
                    _versions.value = it
                }
            }
        }
    }

    fun onContentChanged(newValue: TextFieldValue) {
        val oldText = _textFieldValue.value.text
        _textFieldValue.value = newValue

        if (oldText != newValue.text) {
            _saveStatus.value = SaveStatus.MODIFIED
            updateStats(newValue.text)

            // Undo stack recording
            if (undoStack.isEmpty() || undoStack.last() != newValue.text) {
                undoStack.add(newValue.text)
                if (undoStack.size > 50) undoStack.removeAt(0)
                redoStack.clear()
                _canUndo.value = undoStack.size > 1
                _canRedo.value = false
            }

            // Update search matches if search is active
            if (_isSearchVisible.value && _searchQuery.value.isNotEmpty()) {
                computeSearchMatches(_searchQuery.value, newValue.text)
            }

            // Schedule debounce autosave (3 seconds)
            autoSaveJob?.cancel()
            autoSaveJob = viewModelScope.launch {
                delay(3000L)
                saveImmediately()
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _documentTitle.value = newTitle
        _saveStatus.value = SaveStatus.MODIFIED
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500L)
            saveImmediately()
        }
    }

    fun saveImmediately() {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.SAVING
            val currentText = _textFieldValue.value.text
            val cursor = _textFieldValue.value.selection.start
            if (docType == "CHAPTER") {
                val ch = repository.getChapterByIdSync(docId)
                if (ch != null) {
                    repository.updateChapterMetadata(ch.copy(title = _documentTitle.value))
                    repository.updateChapterContent(docId, currentText, cursor)
                }
            } else {
                val note = repository.getNoteByIdSync(docId)
                if (note != null) {
                    repository.updateNote(note.copy(title = _documentTitle.value, content = currentText))
                }
            }
            _saveStatus.value = SaveStatus.SAVED
        }
    }

    fun undo() {
        if (undoStack.size > 1) {
            val current = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)
            val previous = undoStack.last()
            _textFieldValue.value = TextFieldValue(previous, selection = TextRange(previous.length))
            updateStats(previous)
            _canUndo.value = undoStack.size > 1
            _canRedo.value = true
            _saveStatus.value = SaveStatus.MODIFIED
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val restored = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(restored)
            _textFieldValue.value = TextFieldValue(restored, selection = TextRange(restored.length))
            updateStats(restored)
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            _saveStatus.value = SaveStatus.MODIFIED
        }
    }

    fun toggleFocusMode() {
        _isFocusMode.value = !_isFocusMode.value
    }

    fun toggleReadMode() {
        _isReadMode.value = !_isReadMode.value
    }

    fun toggleMarkdownPreview() {
        _isPreviewMarkdown.value = !_isPreviewMarkdown.value
    }

    fun toggleSearch() {
        _isSearchVisible.value = !_isSearchVisible.value
        if (!_isSearchVisible.value) {
            _searchQuery.value = ""
            _replaceQuery.value = ""
            _matchIndices.value = emptyList()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        computeSearchMatches(query, _textFieldValue.value.text)
    }

    fun updateReplaceQuery(replaceText: String) {
        _replaceQuery.value = replaceText
    }

    private fun computeSearchMatches(query: String, text: String) {
        if (query.isBlank()) {
            _matchIndices.value = emptyList()
            _currentMatchIndex.value = 0
            return
        }
        val matches = mutableListOf<Int>()
        var index = text.indexOf(query, 0, ignoreCase = true)
        while (index != -1) {
            matches.add(index)
            index = text.indexOf(query, index + query.length, ignoreCase = true)
        }
        _matchIndices.value = matches
        if (matches.isNotEmpty() && _currentMatchIndex.value >= matches.size) {
            _currentMatchIndex.value = 0
        }
    }

    fun nextMatch() {
        val matches = _matchIndices.value
        if (matches.isEmpty()) return
        val nextIdx = (_currentMatchIndex.value + 1) % matches.size
        _currentMatchIndex.value = nextIdx
        highlightMatch(matches[nextIdx], _searchQuery.value.length)
    }

    fun prevMatch() {
        val matches = _matchIndices.value
        if (matches.isEmpty()) return
        val prevIdx = if (_currentMatchIndex.value - 1 < 0) matches.size - 1 else _currentMatchIndex.value - 1
        _currentMatchIndex.value = prevIdx
        highlightMatch(matches[prevIdx], _searchQuery.value.length)
    }

    private fun highlightMatch(start: Int, length: Int) {
        _textFieldValue.value = _textFieldValue.value.copy(
            selection = TextRange(start, start + length)
        )
    }

    fun replaceCurrent() {
        val matches = _matchIndices.value
        val query = _searchQuery.value
        val replace = _replaceQuery.value
        if (matches.isEmpty() || query.isEmpty()) return

        val currentIdx = _currentMatchIndex.value.coerceIn(0, matches.size - 1)
        val matchPos = matches[currentIdx]
        val currentText = _textFieldValue.value.text
        val newText = currentText.substring(0, matchPos) + replace + currentText.substring(matchPos + query.length)

        onContentChanged(TextFieldValue(newText, selection = TextRange(matchPos + replace.length)))
        computeSearchMatches(query, newText)
    }

    fun replaceAll() {
        val query = _searchQuery.value
        val replace = _replaceQuery.value
        if (query.isEmpty()) return
        val currentText = _textFieldValue.value.text
        val newText = currentText.replace(query, replace, ignoreCase = true)
        onContentChanged(TextFieldValue(newText))
        computeSearchMatches(query, newText)
    }

    // Markdown insertion helpers
    fun insertMarkdownWrap(prefix: String, suffix: String = prefix) {
        val value = _textFieldValue.value
        val text = value.text
        val selection = value.selection
        if (selection.collapsed) {
            val newText = text.substring(0, selection.start) + prefix + suffix + text.substring(selection.start)
            val newCursor = selection.start + prefix.length
            onContentChanged(TextFieldValue(newText, selection = TextRange(newCursor)))
        } else {
            val selectedText = text.substring(selection.start, selection.end)
            val newText = text.substring(0, selection.start) + prefix + selectedText + suffix + text.substring(selection.end)
            val newEnd = selection.end + prefix.length + suffix.length
            onContentChanged(TextFieldValue(newText, selection = TextRange(newEnd)))
        }
    }

    fun insertLinePrefix(prefix: String) {
        val value = _textFieldValue.value
        val text = value.text
        val selection = value.selection
        val lineStart = text.lastIndexOf('\n', (selection.start - 1).coerceAtLeast(0)).let {
            if (it == -1) 0 else it + 1
        }
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        onContentChanged(TextFieldValue(newText, selection = TextRange(selection.start + prefix.length)))
    }

    fun insertDivider() {
        val value = _textFieldValue.value
        val text = value.text
        val cursor = value.selection.start
        val insertText = "\n\n---\n\n"
        val newText = text.substring(0, cursor) + insertText + text.substring(cursor)
        onContentChanged(TextFieldValue(newText, selection = TextRange(cursor + insertText.length)))
    }

    // Paragraph movement & operations
    fun moveCurrentParagraphUp() {
        val value = _textFieldValue.value
        val text = value.text
        val cursor = value.selection.start
        val paragraphs = text.split("\n\n").toMutableList()
        val currentParaIndex = findParagraphIndex(text, cursor)
        if (currentParaIndex > 0 && currentParaIndex < paragraphs.size) {
            val temp = paragraphs[currentParaIndex]
            paragraphs[currentParaIndex] = paragraphs[currentParaIndex - 1]
            paragraphs[currentParaIndex - 1] = temp
            val newText = paragraphs.joinToString("\n\n")
            onContentChanged(TextFieldValue(newText))
        }
    }

    fun moveCurrentParagraphDown() {
        val value = _textFieldValue.value
        val text = value.text
        val cursor = value.selection.start
        val paragraphs = text.split("\n\n").toMutableList()
        val currentParaIndex = findParagraphIndex(text, cursor)
        if (currentParaIndex >= 0 && currentParaIndex < paragraphs.size - 1) {
            val temp = paragraphs[currentParaIndex]
            paragraphs[currentParaIndex] = paragraphs[currentParaIndex + 1]
            paragraphs[currentParaIndex + 1] = temp
            val newText = paragraphs.joinToString("\n\n")
            onContentChanged(TextFieldValue(newText))
        }
    }

    fun duplicateCurrentParagraph() {
        val value = _textFieldValue.value
        val text = value.text
        val cursor = value.selection.start
        val paragraphs = text.split("\n\n").toMutableList()
        val currentParaIndex = findParagraphIndex(text, cursor)
        if (currentParaIndex in paragraphs.indices) {
            paragraphs.add(currentParaIndex + 1, paragraphs[currentParaIndex])
            val newText = paragraphs.joinToString("\n\n")
            onContentChanged(TextFieldValue(newText))
        }
    }

    fun deleteCurrentParagraph() {
        val value = _textFieldValue.value
        val text = value.text
        val cursor = value.selection.start
        val paragraphs = text.split("\n\n").toMutableList()
        val currentParaIndex = findParagraphIndex(text, cursor)
        if (currentParaIndex in paragraphs.indices) {
            paragraphs.removeAt(currentParaIndex)
            val newText = paragraphs.joinToString("\n\n")
            onContentChanged(TextFieldValue(newText))
        }
    }

    private fun findParagraphIndex(text: String, cursor: Int): Int {
        var runningIndex = 0
        val paragraphs = text.split("\n\n")
        paragraphs.forEachIndexed { idx, p ->
            val nextIndex = runningIndex + p.length + 2
            if (cursor in runningIndex..nextIndex) return idx
            runningIndex = nextIndex
        }
        return (paragraphs.size - 1).coerceAtLeast(0)
    }

    // Versioning
    fun createSnapshot(summary: String) {
        viewModelScope.launch {
            if (docType == "CHAPTER") {
                repository.saveVersionSnapshot(
                    chapterId = docId,
                    title = _documentTitle.value,
                    content = _textFieldValue.value.text,
                    summary = summary.ifBlank { "Snapshot Manual" }
                )
            } else {
                repository.saveVersionSnapshot(
                    noteId = docId,
                    title = _documentTitle.value,
                    content = _textFieldValue.value.text,
                    summary = summary.ifBlank { "Snapshot Manual" }
                )
            }
        }
    }

    fun restoreVersion(version: DocumentVersionEntity) {
        onContentChanged(TextFieldValue(version.content))
        saveImmediately()
    }

    // Stats computation
    private fun updateStats(text: String) {
        val words = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val chars = text.length
        val charsNoSpaces = text.count { !it.isWhitespace() }
        val paragraphs = if (text.isBlank()) 0 else text.split(Regex("\n\n+")).filter { it.isNotBlank() }.size
        val readingTime = (words / 200).coerceAtLeast(if (words > 0) 1 else 0)

        _stats.value = EditorStats(
            words = words,
            characters = chars,
            charactersNoSpaces = charsNoSpaces,
            paragraphs = paragraphs,
            readingTimeMinutes = readingTime
        )
    }

    // Quick Typography setters
    fun setFontFamily(font: String) {
        viewModelScope.launch { repository.updateEditorFont(font) }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch { repository.updateEditorFontSize(size) }
    }

    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch { repository.updateEditorLineSpacing(spacing) }
    }

    fun setMarginSize(margin: String) {
        viewModelScope.launch { repository.updateEditorMarginSize(margin) }
    }

    fun setTextAlign(align: String) {
        viewModelScope.launch { repository.updateEditorTextAlign(align) }
    }

    fun setTypewriterMode(enabled: Boolean) {
        viewModelScope.launch { repository.updateTypewriterMode(enabled) }
    }
}
