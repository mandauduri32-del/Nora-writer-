package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "novel_desk_settings")

data class UserPreferences(
    val themeMode: String = "AMETHYST", // SYSTEM, LIGHT, DARK_SLATE, OLED, SEPIA, SAPPHIRE, EMERALD, AMETHYST, CRIMSON
    val editorFontFamily: String = "SERIF", // SERIF, SANS, MONOSPACE
    val editorFontSize: Float = 17f,
    val editorLineSpacing: Float = 1.5f,
    val editorParagraphSpacing: Float = 12f,
    val editorMarginSize: String = "NORMAL", // COMPACT, NORMAL, WIDE
    val editorTextAlign: String = "START", // START, JUSTIFY, CENTER
    val typewriterMode: Boolean = true,
    val autoSaveIntervalSeconds: Int = 3,
    val dailyWordTarget: Int = 1000,
    val securityPin: String = "",
    val customBgPath: String = "",
    val bgOverlayOpacity: Float = 0.55f,
    val bgBlurRadius: Float = 0f,
    val bgPosition: String = "COVER", // COVER, CENTER, TOP
    val lastActiveDocType: String = "NONE", // NONE, CHAPTER, NOTE
    val lastActiveDocId: Long = 0L,
    val lastActiveBookId: Long = 0L,
    val lastActiveDocTitle: String = ""
)

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val EDITOR_FONT_FAMILY = stringPreferencesKey("editor_font_family")
        val EDITOR_FONT_SIZE = floatPreferencesKey("editor_font_size")
        val EDITOR_LINE_SPACING = floatPreferencesKey("editor_line_spacing")
        val EDITOR_PARAGRAPH_SPACING = floatPreferencesKey("editor_paragraph_spacing")
        val EDITOR_MARGIN_SIZE = stringPreferencesKey("editor_margin_size")
        val EDITOR_TEXT_ALIGN = stringPreferencesKey("editor_text_align")
        val TYPEWRITER_MODE = booleanPreferencesKey("typewriter_mode")
        val AUTOSAVE_INTERVAL = intPreferencesKey("autosave_interval")
        val DAILY_WORD_TARGET = intPreferencesKey("daily_word_target")
        val SECURITY_PIN = stringPreferencesKey("security_pin")
        val CUSTOM_BG_PATH = stringPreferencesKey("custom_bg_path")
        val BG_OVERLAY_OPACITY = floatPreferencesKey("bg_overlay_opacity")
        val BG_BLUR_RADIUS = floatPreferencesKey("bg_blur_radius")
        val BG_POSITION = stringPreferencesKey("bg_position")
        val LAST_ACTIVE_DOC_TYPE = stringPreferencesKey("last_active_doc_type")
        val LAST_ACTIVE_DOC_ID = longPreferencesKey("last_active_doc_id")
        val LAST_ACTIVE_BOOK_ID = longPreferencesKey("last_active_book_id")
        val LAST_ACTIVE_DOC_TITLE = stringPreferencesKey("last_active_doc_title")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { pref ->
        UserPreferences(
            themeMode = pref[PreferencesKeys.THEME_MODE] ?: "AMETHYST",
            editorFontFamily = pref[PreferencesKeys.EDITOR_FONT_FAMILY] ?: "SERIF",
            editorFontSize = pref[PreferencesKeys.EDITOR_FONT_SIZE] ?: 17f,
            editorLineSpacing = pref[PreferencesKeys.EDITOR_LINE_SPACING] ?: 1.5f,
            editorParagraphSpacing = pref[PreferencesKeys.EDITOR_PARAGRAPH_SPACING] ?: 12f,
            editorMarginSize = pref[PreferencesKeys.EDITOR_MARGIN_SIZE] ?: "NORMAL",
            editorTextAlign = pref[PreferencesKeys.EDITOR_TEXT_ALIGN] ?: "START",
            typewriterMode = pref[PreferencesKeys.TYPEWRITER_MODE] ?: true,
            autoSaveIntervalSeconds = pref[PreferencesKeys.AUTOSAVE_INTERVAL] ?: 3,
            dailyWordTarget = pref[PreferencesKeys.DAILY_WORD_TARGET] ?: 1000,
            securityPin = pref[PreferencesKeys.SECURITY_PIN] ?: "",
            customBgPath = pref[PreferencesKeys.CUSTOM_BG_PATH] ?: "",
            bgOverlayOpacity = pref[PreferencesKeys.BG_OVERLAY_OPACITY] ?: 0.55f,
            bgBlurRadius = pref[PreferencesKeys.BG_BLUR_RADIUS] ?: 0f,
            bgPosition = pref[PreferencesKeys.BG_POSITION] ?: "COVER",
            lastActiveDocType = pref[PreferencesKeys.LAST_ACTIVE_DOC_TYPE] ?: "NONE",
            lastActiveDocId = pref[PreferencesKeys.LAST_ACTIVE_DOC_ID] ?: 0L,
            lastActiveBookId = pref[PreferencesKeys.LAST_ACTIVE_BOOK_ID] ?: 0L,
            lastActiveDocTitle = pref[PreferencesKeys.LAST_ACTIVE_DOC_TITLE] ?: ""
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setEditorFontFamily(font: String) {
        context.dataStore.edit { it[PreferencesKeys.EDITOR_FONT_FAMILY] = font }
    }

    suspend fun setEditorFontSize(size: Float) {
        context.dataStore.edit { it[PreferencesKeys.EDITOR_FONT_SIZE] = size }
    }

    suspend fun setEditorLineSpacing(spacing: Float) {
        context.dataStore.edit { it[PreferencesKeys.EDITOR_LINE_SPACING] = spacing }
    }

    suspend fun setEditorMarginSize(margin: String) {
        context.dataStore.edit { it[PreferencesKeys.EDITOR_MARGIN_SIZE] = margin }
    }

    suspend fun setEditorTextAlign(align: String) {
        context.dataStore.edit { it[PreferencesKeys.EDITOR_TEXT_ALIGN] = align }
    }

    suspend fun setTypewriterMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TYPEWRITER_MODE] = enabled }
    }

    suspend fun setDailyWordTarget(target: Int) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_WORD_TARGET] = target }
    }

    suspend fun setSecurityPin(pin: String) {
        context.dataStore.edit { it[PreferencesKeys.SECURITY_PIN] = pin }
    }

    suspend fun setCustomBg(path: String, opacity: Float = 0.55f, blur: Float = 0f, position: String = "COVER") {
        context.dataStore.edit {
            it[PreferencesKeys.CUSTOM_BG_PATH] = path
            it[PreferencesKeys.BG_OVERLAY_OPACITY] = opacity
            it[PreferencesKeys.BG_BLUR_RADIUS] = blur
            it[PreferencesKeys.BG_POSITION] = position
        }
    }

    suspend fun clearCustomBg() {
        context.dataStore.edit {
            it[PreferencesKeys.CUSTOM_BG_PATH] = ""
            it[PreferencesKeys.BG_OVERLAY_OPACITY] = 0.55f
            it[PreferencesKeys.BG_BLUR_RADIUS] = 0f
            it[PreferencesKeys.BG_POSITION] = "COVER"
        }
    }

    suspend fun setLastActiveDocument(type: String, id: Long, bookId: Long = 0L, title: String = "") {
        context.dataStore.edit {
            it[PreferencesKeys.LAST_ACTIVE_DOC_TYPE] = type
            it[PreferencesKeys.LAST_ACTIVE_DOC_ID] = id
            it[PreferencesKeys.LAST_ACTIVE_BOOK_ID] = bookId
            it[PreferencesKeys.LAST_ACTIVE_DOC_TITLE] = title
        }
    }
}
