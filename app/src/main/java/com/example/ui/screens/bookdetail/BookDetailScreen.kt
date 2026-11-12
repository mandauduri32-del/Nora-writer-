package com.example.ui.screens.bookdetail

import android.content.Intent
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.StoryElementEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.WordCountBadge
import com.example.ui.screens.library.CreateBookDialog
import com.example.ui.theme.GoldAccent
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BookDetailScreen(
    bookId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String, Long) -> Unit
) {
    val context = LocalContext.current
    val repository = viewModel.repository

    var book by remember { mutableStateOf<BookEntity?>(null) }
    var chapters by remember { mutableStateOf<List<ChapterEntity>>(emptyList()) }
    var storyElements by remember { mutableStateOf<List<StoryElementEntity>>(emptyList()) }
    var totalWords by remember { mutableIntStateOf(0) }

    LaunchedEffect(bookId) {
        launch {
            repository.getBookById(bookId).collect { book = it }
        }
        launch {
            repository.getChaptersForBook(bookId).collect { chapters = it }
        }
        launch {
            repository.getStoryElementsForBook(bookId).collect { storyElements = it }
        }
        launch {
            repository.getTotalWordsForBook(bookId).collect { totalWords = it }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showEditBookDialog by remember { mutableStateOf(false) }
    var showCreateChapterDialog by remember { mutableStateOf(false) }
    var showStoryElementDialog by remember { mutableStateOf(false) }
    var elementToEdit by remember { mutableStateOf<StoryElementEntity?>(null) }
    var chapterToDelete by remember { mutableStateOf<ChapterEntity?>(null) }

    val currentBook = book ?: return

    val coverColor = try {
        Color(android.graphics.Color.parseColor(currentBook.coverColorHex))
    } catch (e: Exception) {
        Color(0xFF1E293B)
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showCreateChapterDialog = true
                    else showStoryElementDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("book_detail_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = if (selectedTab == 0) "Tambah Bab Baru" else "Tambah Unsur Cerita")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(coverColor, coverColor.copy(alpha = 0.85f), MaterialTheme.colorScheme.surface)
                            )
                        )
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Column {
                        // Top Nav
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("book_detail_back_button")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                            }
                            Row {
                                IconButton(onClick = { viewModel.toggleBookFavorite(currentBook) }) {
                                    Icon(
                                        imageVector = if (currentBook.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorit",
                                        tint = if (currentBook.isFavorite) Color(0xFFEF4444) else Color.White
                                    )
                                }
                                IconButton(onClick = { showEditBookDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Ubah Rincian", tint = Color.White)
                                }
                                IconButton(
                                    onClick = {
                                        // Export full book
                                        val sb = StringBuilder()
                                        sb.append("# ${currentBook.title}\n")
                                        sb.append("Penulis: ${currentBook.author}\n")
                                        sb.append("Genre: ${currentBook.genre}\n\n")
                                        if (currentBook.synopsis.isNotBlank()) {
                                            sb.append("> ${currentBook.synopsis}\n\n---\n\n")
                                        }
                                        chapters.forEach { ch ->
                                            sb.append("${ch.content}\n\n---\n\n")
                                        }
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, currentBook.title)
                                            putExtra(Intent.EXTRA_TEXT, sb.toString())
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Ekspor Seluruh Buku"))
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Ekspor Buku", tint = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Book Title & Metadata
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Spine Thumbnail
                            val hasCustomCover = currentBook.coverUri.isNotBlank() && File(currentBook.coverUri).exists()
                            Box(
                                modifier = Modifier
                                    .size(width = 68.dp, height = 98.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                if (hasCustomCover) {
                                    AsyncImage(
                                        model = File(currentBook.coverUri),
                                        contentDescription = currentBook.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp).align(Alignment.Center)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(Color.White.copy(alpha = 0.35f))
                                        .align(Alignment.CenterStart)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentBook.title,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "oleh ${currentBook.author} • ${currentBook.genre}",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    WordCountBadge(
                                        count = totalWords,
                                        backgroundColor = Color.White.copy(alpha = 0.2f),
                                        textColor = Color.White
                                    )
                                    Text(
                                        text = "${chapters.size} Bab",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        if (currentBook.synopsis.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = currentBook.synopsis,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Tabs: "Daftar Bab" and "Plot & Karakter"
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Daftar Bab (${chapters.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Plot & Karakter (${storyElements.size})", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Content of Tab 0: Chapters List
            if (selectedTab == 0) {
                // Search chapters
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari judul bab atau isi tulisan...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("chapter_search_input")
                    )
                }

                val filteredChapters = if (searchQuery.isBlank()) chapters else {
                    chapters.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.content.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredChapters.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Tidak ada bab yang cocok dengan pencarian" else "Belum ada bab. Klik tombol '+' untuk membuat Bab 1.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredChapters) { ch ->
                        ChapterItemRow(
                            chapter = ch,
                            onClick = { onNavigateToEditor("CHAPTER", ch.id) },
                            onMoveUp = {
                                val currentIdx = chapters.indexOfFirst { it.id == ch.id }
                                if (currentIdx > 0) {
                                    val mutable = chapters.toMutableList()
                                    val temp = mutable[currentIdx]
                                    mutable[currentIdx] = mutable[currentIdx - 1]
                                    mutable[currentIdx - 1] = temp
                                    CoroutineScope(Dispatchers.IO).launch {
                                        repository.reorderChapters(mutable)
                                    }
                                }
                            },
                            onMoveDown = {
                                val currentIdx = chapters.indexOfFirst { it.id == ch.id }
                                if (currentIdx in 0 until chapters.size - 1) {
                                    val mutable = chapters.toMutableList()
                                    val temp = mutable[currentIdx]
                                    mutable[currentIdx] = mutable[currentIdx + 1]
                                    mutable[currentIdx + 1] = temp
                                    CoroutineScope(Dispatchers.IO).launch {
                                        repository.reorderChapters(mutable)
                                    }
                                }
                            },
                            onDuplicate = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    repository.createChapter(
                                        ch.copy(
                                            id = 0,
                                            title = "${ch.title} (Salinan)",
                                            orderIndex = ch.orderIndex + 1
                                        )
                                    )
                                }
                            },
                            onDelete = { chapterToDelete = ch }
                        )
                    }
                }
            } else {
                // Content of Tab 1: Story Elements
                if (storyElements.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Belum ada unsur cerita.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Catat karakter, latar tempat, dan alur plot penting novel Anda.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(storyElements) { elem ->
                        StoryElementRow(
                            element = elem,
                            onEdit = {
                                elementToEdit = elem
                                showStoryElementDialog = true
                            },
                            onDelete = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    repository.deleteStoryElement(elem.id)
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Create Chapter Dialog
    if (showCreateChapterDialog) {
        var chapterTitleInput by remember {
            val nextNumber = (chapters.maxOfOrNull { it.chapterNumber } ?: 0) + 1
            mutableStateOf("Bab $nextNumber")
        }

        AlertDialog(
            onDismissRequest = { showCreateChapterDialog = false },
            title = { Text("Tambah Bab Baru", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = chapterTitleInput,
                    onValueChange = { chapterTitleInput = it },
                    label = { Text("Judul Bab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_chapter_title_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nextNumber = (chapters.maxOfOrNull { it.chapterNumber } ?: 0) + 1
                        val nextOrder = (chapters.maxOfOrNull { it.orderIndex } ?: 0) + 1
                        CoroutineScope(Dispatchers.IO).launch {
                            val newId = repository.createChapter(
                                ChapterEntity(
                                    bookId = bookId,
                                    title = chapterTitleInput.ifBlank { "Bab $nextNumber" },
                                    chapterNumber = nextNumber,
                                    content = "# $chapterTitleInput\n\n",
                                    orderIndex = nextOrder
                                )
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                showCreateChapterDialog = false
                                onNavigateToEditor("CHAPTER", newId)
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_chapter_button")
                ) {
                    Text("Buat & Buka Editor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChapterDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Story Element Dialog
    if (showStoryElementDialog) {
        StoryElementDialog(
            element = elementToEdit,
            onConfirm = { type, name, role, desc, details ->
                CoroutineScope(Dispatchers.IO).launch {
                    if (elementToEdit == null) {
                        repository.addStoryElement(
                            StoryElementEntity(
                                bookId = bookId,
                                type = type,
                                name = name,
                                roleOrTag = role,
                                description = desc,
                                details = details
                            )
                        )
                    } else {
                        repository.updateStoryElement(
                            elementToEdit!!.copy(
                                type = type,
                                name = name,
                                roleOrTag = role,
                                description = desc,
                                details = details
                            )
                        )
                    }
                }
                elementToEdit = null
                showStoryElementDialog = false
            },
            onDismiss = {
                elementToEdit = null
                showStoryElementDialog = false
            }
        )
    }

    // Edit Book Dialog
    if (showEditBookDialog) {
        CreateBookDialog(
            initialTitle = currentBook.title,
            initialSynopsis = currentBook.synopsis,
            initialGenre = currentBook.genre,
            initialAuthor = currentBook.author,
            initialStatus = currentBook.status,
            initialColorHex = currentBook.coverColorHex,
            initialCoverUri = currentBook.coverUri,
            initialTargetWords = currentBook.targetWordCount,
            isEditing = true,
            onConfirm = { title, synopsis, genre, author, status, colorHex, coverUri, targetWordsVal ->
                showEditBookDialog = false
                viewModel.updateBook(
                    currentBook.copy(
                        title = title,
                        synopsis = synopsis,
                        genre = genre,
                        author = author,
                        status = status,
                        coverColorHex = colorHex,
                        coverUri = coverUri,
                        targetWordCount = targetWordsVal
                    )
                )
            },
            onDismiss = { showEditBookDialog = false }
        )
    }

    // Delete Chapter Confirmation
    chapterToDelete?.let { ch ->
        ConfirmDialog(
            title = "Pindahkan Bab ke Tempat Sampah?",
            message = "Bab '${ch.title}' akan dipindahkan ke Tempat Sampah. Anda dapat memulihkannya nanti.",
            confirmButtonText = "Ya, Pindahkan",
            cancelButtonText = "Batal",
            isDestructive = true,
            onConfirm = {
                viewModel.moveChapterToTrash(ch.id)
                chapterToDelete = null
            },
            onDismiss = { chapterToDelete = null }
        )
    }
}

@Composable
fun ChapterItemRow(
    chapter: ChapterEntity,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag("chapter_item_${chapter.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WordCountBadge(count = chapter.wordCount)
                    Text(
                        text = "${chapter.charCount} karakter",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Move Up / Down
            IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Naikkan Bab", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Turunkan Bab", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // More Options
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opsi Bab")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Buka di Editor") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplikasi Bab") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus ke Tempat Sampah", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StoryElementRow(
    element: StoryElementEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val typeLabel = when (element.type) {
        "CHARACTER" -> "Karakter"
        "LOCATION" -> "Lokasi"
        "PLOT_EVENT" -> "Alur Cerita"
        "WORLDBUILDING" -> "Dunia Fiksi"
        else -> "Catatan"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onEdit() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (element.roleOrTag.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = element.roleOrTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Ubah Rincian") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus Unsur", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = element.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (element.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = element.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (element.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = element.details,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
