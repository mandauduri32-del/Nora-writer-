package com.example.ui.screens.editor

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MarkdownRenderer
import com.example.ui.components.WordCountBadge
import com.example.ui.theme.GoldAccent
import com.example.viewmodel.EditorViewModel
import com.example.viewmodel.SaveStatus

@Composable
fun EditorScreen(
    docType: String,
    docId: Long,
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (Long) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(docType, docId) {
        viewModel.loadDocument(docType, docId)
    }

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val title by viewModel.documentTitle.collectAsStateWithLifecycle()
    val textFieldValue by viewModel.textFieldValue.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()

    val isFocusMode by viewModel.isFocusMode.collectAsStateWithLifecycle()
    val isReadMode by viewModel.isReadMode.collectAsStateWithLifecycle()
    val isPreviewMarkdown by viewModel.isPreviewMarkdown.collectAsStateWithLifecycle()
    val isSearchVisible by viewModel.isSearchVisible.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.replaceQuery.collectAsStateWithLifecycle()
    val matchIndices by viewModel.matchIndices.collectAsStateWithLifecycle()
    val currentMatchIndex by viewModel.currentMatchIndex.collectAsStateWithLifecycle()

    val prevChapter by viewModel.prevChapter.collectAsStateWithLifecycle()
    val nextChapter by viewModel.nextChapter.collectAsStateWithLifecycle()
    val versions by viewModel.versions.collectAsStateWithLifecycle()

    var showStatsSheet by remember { mutableStateOf(false) }
    var showTypographySheet by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.saveImmediately()
        onNavigateBack()
    }

    val selectedFontFamily = when (preferences.editorFontFamily) {
        "SANS" -> FontFamily.SansSerif
        "MONOSPACE" -> FontFamily.Monospace
        else -> FontFamily.Serif
    }

    val horizontalMargin = when (preferences.editorMarginSize) {
        "COMPACT" -> 14.dp
        "WIDE" -> 28.dp
        else -> 20.dp
    }

    val textAlign = when (preferences.editorTextAlign) {
        "JUSTIFY" -> TextAlign.Justify
        "CENTER" -> TextAlign.Center
        else -> TextAlign.Start
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isFocusMode) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.saveImmediately()
                                    onNavigateBack()
                                },
                                modifier = Modifier.testTag("editor_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                            }

                            // Title input
                            BasicTextField(
                                value = title,
                                onValueChange = { viewModel.onTitleChanged(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .testTag("editor_title_input"),
                                decorationBox = { inner ->
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "Judul Dokumen...",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    inner()
                                }
                            )

                            // Save status indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (saveStatus) {
                                            SaveStatus.SAVED -> Color(0xFF10B981).copy(alpha = 0.15f)
                                            SaveStatus.SAVING -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                            SaveStatus.MODIFIED -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (saveStatus) {
                                        SaveStatus.SAVED -> "Tersimpan"
                                        SaveStatus.SAVING -> "Menyimpan..."
                                        SaveStatus.MODIFIED -> "Diedit"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (saveStatus) {
                                        SaveStatus.SAVED -> Color(0xFF10B981)
                                        SaveStatus.SAVING -> Color(0xFF3B82F6)
                                        SaveStatus.MODIFIED -> Color(0xFFF59E0B)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Word count badge (click to view stats)
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))) {
                                WordCountBadge(
                                    count = stats.words,
                                    modifier = Modifier.testTag("word_count_trigger")
                                )
                            }

                            // Overflow menu
                            Box {
                                IconButton(
                                    onClick = { showOverflowMenu = true },
                                    modifier = Modifier.size(36.dp).testTag("editor_overflow_button")
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu Editor")
                                }

                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Statistik Lengkap") },
                                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showStatsSheet = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Pengaturan Tampilan & Huruf") },
                                        leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showTypographySheet = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Riwayat Versi") },
                                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showVersionDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Cari & Ganti") },
                                        leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.toggleSearch()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (isPreviewMarkdown) "Mode Edit" else "Pratinjau Format") },
                                        leadingIcon = { Icon(if (isPreviewMarkdown) Icons.Outlined.Edit else Icons.Default.Preview, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.toggleMarkdownPreview()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mode Fokus") },
                                        leadingIcon = { Icon(Icons.Default.Fullscreen, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.toggleFocusMode()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Bagikan Naskah") },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, title)
                                                putExtra(Intent.EXTRA_TEXT, "${title}\n\n${textFieldValue.text}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Tulisan"))
                                        }
                                    )
                                }
                            }
                        }

                        // Search and Replace bar
                        AnimatedVisibility(visible = isSearchVisible) {
                            EditorSearchReplaceBar(
                                searchQuery = searchQuery,
                                replaceQuery = replaceQuery,
                                matchesCount = matchIndices.size,
                                currentMatchIndex = currentMatchIndex,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onReplaceQueryChange = { viewModel.updateReplaceQuery(it) },
                                onPrevMatch = { viewModel.prevMatch() },
                                onNextMatch = { viewModel.nextMatch() },
                                onReplaceCurrent = { viewModel.replaceCurrent() },
                                onReplaceAll = { viewModel.replaceAll() },
                                onClose = { viewModel.toggleSearch() }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isFocusMode && !isPreviewMarkdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                ) {
                    // Quick Chapter Switcher bar (if editing book chapter)
                    if (docType == "CHAPTER" && (prevChapter != null || nextChapter != null)) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (prevChapter != null) {
                                    TextButton(
                                        onClick = {
                                            viewModel.saveImmediately()
                                            onNavigateToChapter(prevChapter!!.id)
                                        },
                                        modifier = Modifier.testTag("prev_chapter_button")
                                    ) {
                                        Icon(Icons.Default.NavigateBefore, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text(text = prevChapter!!.title, fontSize = 11.sp, maxLines = 1)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                if (nextChapter != null) {
                                    TextButton(
                                        onClick = {
                                            viewModel.saveImmediately()
                                            onNavigateToChapter(nextChapter!!.id)
                                        },
                                        modifier = Modifier.testTag("next_chapter_button")
                                    ) {
                                        Text(text = nextChapter!!.title, fontSize = 11.sp, maxLines = 1)
                                        Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Formatting Toolbar
                    EditorToolbar(
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onHeading = { level ->
                            val prefix = when (level) {
                                1 -> "# "
                                2 -> "## "
                                else -> "### "
                            }
                            viewModel.insertLinePrefix(prefix)
                        },
                        onBold = { viewModel.insertMarkdownWrap("**") },
                        onItalic = { viewModel.insertMarkdownWrap("*") },
                        onUnderline = { viewModel.insertMarkdownWrap("<u>", "</u>") },
                        onStrikethrough = { viewModel.insertMarkdownWrap("~~") },
                        onQuote = { viewModel.insertLinePrefix("> ") },
                        onBulletList = { viewModel.insertLinePrefix("- ") },
                        onNumberList = { viewModel.insertLinePrefix("1. ") },
                        onDivider = { viewModel.insertDivider() },
                        onMoveParaUp = { viewModel.moveCurrentParagraphUp() },
                        onMoveParaDown = { viewModel.moveCurrentParagraphDown() },
                        onDuplicatePara = { viewModel.duplicateCurrentParagraph() },
                        onDeletePara = { viewModel.deleteCurrentParagraph() }
                    )
                }
            }
        }
    ) { innerPadding ->
        val extraInsetsModifier = when {
            isFocusMode -> Modifier.windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime))
            isPreviewMarkdown -> Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            else -> Modifier
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(extraInsetsModifier)
        ) {
            val scrollState = rememberScrollState()

            if (isPreviewMarkdown) {
                // Rendered Markdown Preview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = horizontalMargin, vertical = 20.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = selectedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (preferences.editorFontSize * 1.5f).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    MarkdownRenderer(
                        content = textFieldValue.text,
                        fontSize = preferences.editorFontSize,
                        lineSpacing = preferences.editorLineSpacing,
                        fontFamily = selectedFontFamily,
                        textAlign = textAlign
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }
            } else {
                // Writing Editor Area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = horizontalMargin, vertical = 16.dp)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { viewModel.onContentChanged(it) },
                        textStyle = TextStyle(
                            fontFamily = selectedFontFamily,
                            fontSize = preferences.editorFontSize.sp,
                            lineHeight = (preferences.editorFontSize * preferences.editorLineSpacing).sp,
                            textAlign = textAlign,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(GoldAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_text_canvas"),
                        decorationBox = { innerTextField ->
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = "Mulai menulis naskah Anda di sini...",
                                    style = TextStyle(
                                        fontFamily = selectedFontFamily,
                                        fontSize = preferences.editorFontSize.sp,
                                        lineHeight = (preferences.editorFontSize * preferences.editorLineSpacing).sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Extra space at bottom to allow comfortable typing above keyboard
                    Spacer(modifier = Modifier.height(260.dp))
                }
            }

            // Focus Mode Exit floating button
            if (isFocusMode) {
                IconButton(
                    onClick = { viewModel.toggleFocusMode() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 16.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        .testTag("exit_focus_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloseFullscreen,
                        contentDescription = "Keluar Mode Fokus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (showStatsSheet) {
        EditorStatsSheet(
            stats = stats,
            onDismiss = { showStatsSheet = false }
        )
    }

    if (showTypographySheet) {
        EditorTypographySheet(
            preferences = preferences,
            onFontFamilyChange = { viewModel.setFontFamily(it) },
            onFontSizeChange = { viewModel.setFontSize(it) },
            onLineSpacingChange = { viewModel.setLineSpacing(it) },
            onMarginSizeChange = { viewModel.setMarginSize(it) },
            onTextAlignChange = { viewModel.setTextAlign(it) },
            onTypewriterModeChange = { viewModel.setTypewriterMode(it) },
            onDismiss = { showTypographySheet = false }
        )
    }

    if (showVersionDialog) {
        EditorVersionHistoryDialog(
            versions = versions,
            onCreateSnapshot = { note -> viewModel.createSnapshot(note) },
            onRestoreVersion = { ver -> viewModel.restoreVersion(ver) },
            onDismiss = { showVersionDialog = false }
        )
    }
}
