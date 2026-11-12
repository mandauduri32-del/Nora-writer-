package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [Index(value = ["isInTrash"]), Index(value = ["updatedAt"])]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val synopsis: String = "",
    val genre: String = "Fiksi Umum",
    val author: String = "Penulis",
    val status: String = "DRAFT", // IDE, DRAFT, REVISI, SELESAI
    val coverUri: String = "",
    val coverColorHex: String = "#1E293B",
    val targetWordCount: Int = 50000,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isInTrash: Boolean = false,
    val folderName: String = "Utama",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["isInTrash"]),
        Index(value = ["orderIndex"])
    ]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val title: String,
    val chapterNumber: Int = 1,
    val content: String = "",
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val orderIndex: Int = 0,
    val lastCursorPosition: Int = 0,
    val notes: String = "",
    val isArchived: Boolean = false,
    val isInTrash: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quick_notes",
    indices = [Index(value = ["isInTrash"]), Index(value = ["updatedAt"])]
)
data class QuickNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val category: String = "Ide Cerita", // Ide Cerita, Karakter, Dialog, Riset, Draf Kasar, Umum
    val isPinned: Boolean = false,
    val isInTrash: Boolean = false,
    val wordCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "story_elements",
    indices = [Index(value = ["bookId"]), Index(value = ["type"])]
)
data class StoryElementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val type: String, // CHARACTER, LOCATION, PLOT_EVENT, WORLDBUILDING, GENERAL_NOTE
    val name: String,
    val roleOrTag: String = "",
    val description: String = "",
    val details: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "document_versions",
    indices = [Index(value = ["chapterId"]), Index(value = ["noteId"]), Index(value = ["timestamp"])]
)
data class DocumentVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long = 0L,
    val noteId: Long = 0L,
    val title: String,
    val content: String,
    val wordCount: Int,
    val summary: String = "Versi Tersimpan",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_writing_logs")
data class DailyWritingLogEntity(
    @PrimaryKey val dateStr: String, // YYYY-MM-DD
    val wordsWritten: Int = 0,
    val targetWords: Int = 1000,
    val writingTimeMinutes: Int = 0
)
