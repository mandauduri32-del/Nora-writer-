package com.example.ui.screens.files

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.screens.library.CreateBookDialog
import com.example.ui.theme.FolderAmber
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Screen "Berkas" matching Screenshot 1 (Screenshot_20261112-113755_df12463e.png)
 */
@Composable
fun FilesScreen(
    viewModel: MainViewModel,
    onNavigateToBookDetail: (Long) -> Unit,
    onNavigateToEditor: (String, Long) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val books by viewModel.activeBooks.collectAsStateWithLifecycle()
    val allChapters by viewModel.allActiveChapters.collectAsStateWithLifecycle()

    var selectedFolderBook by remember { mutableStateOf<BookEntity?>(null) }
    var showCreateBookDialog by remember { mutableStateOf(false) }
    var showCreateChapterDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("UPDATED_DESC") }
    var bookToRename by remember { mutableStateOf<BookEntity?>(null) }
    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }
    var chapterToDelete by remember { mutableStateOf<ChapterEntity?>(null) }

    BackHandler(enabled = selectedFolderBook != null) {
        selectedFolderBook = null
    }

    // Format relative / standard timestamp matching screenshot
    fun formatSubtitle(timestamp: Long): String {
        val nowCal = Calendar.getInstance()
        val itemCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = nowCal.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)

        val isYesterday = nowCal.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) - itemCal.get(Calendar.DAY_OF_YEAR) == 1

        val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("id", "ID"))

        return when {
            isToday -> "Hari ini ${timeFormat.format(Date(timestamp))}"
            isYesterday -> "Kemarin ${timeFormat.format(Date(timestamp))}"
            else -> fullDateFormat.format(Date(timestamp))
        }
    }

    // Sort books
    val sortedBooks = remember(books, sortBy) {
        when (sortBy) {
            "TITLE_ASC" -> books.sortedBy { it.title.lowercase() }
            "TITLE_DESC" -> books.sortedByDescending { it.title.lowercase() }
            "UPDATED_ASC" -> books.sortedBy { it.updatedAt }
            else -> books.sortedByDescending { it.updatedAt }
        }
    }

    // Chapters inside selected folder
    val folderChapters = remember(allChapters, selectedFolderBook, sortBy) {
        val book = selectedFolderBook ?: return@remember emptyList()
        val filtered = allChapters.filter { it.bookId == book.id }
        when (sortBy) {
            "TITLE_ASC" -> filtered.sortedBy { it.title.lowercase() }
            "TITLE_DESC" -> filtered.sortedByDescending { it.title.lowercase() }
            "UPDATED_ASC" -> filtered.sortedBy { it.updatedAt }
            else -> filtered.sortedBy { it.orderIndex }
        }
    }

    fun exportBook(book: BookEntity) {
        coroutineScope.launch {
            val chapters = viewModel.repository.getChaptersForBook(book.id).first()
            val sb = StringBuilder()
            sb.append("# ${book.title}\n")
            sb.append("Penulis: ${book.author}\n")
            sb.append("Genre: ${book.genre}\n\n")
            if (book.synopsis.isNotBlank()) {
                sb.append("> ${book.synopsis}\n\n---\n\n")
            }
            chapters.forEach { ch ->
                sb.append("## ${ch.title}\n\n${ch.content}\n\n---\n\n")
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, book.title)
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }
            context.startActivity(Intent.createChooser(shareIntent, "Ekspor Seluruh Buku"))
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedFolderBook != null) showCreateChapterDialog = true
                    else showCreateBookDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("files_create_fab")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = if (selectedFolderBook != null) "Buat Bab Baru" else "Buat Berkas / Buku Baru",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar: Hamburger (or Back when inside folder), "Berkas", Search, Sort, More (Screenshot 1)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedFolderBook != null) {
                            IconButton(onClick = { selectedFolderBook = null }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali ke Daftar Folder",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier.testTag("files_drawer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Buka Menu",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedFolderBook?.title ?: "Berkas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenSearch,
                            modifier = Modifier.testTag("files_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Pencarian",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Urutkan",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Terbaru diubah") },
                                    onClick = { sortBy = "UPDATED_DESC"; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Terlama diubah") },
                                    onClick = { sortBy = "UPDATED_ASC"; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Nama (A - Z)") },
                                    onClick = { sortBy = "TITLE_ASC"; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Nama (Z - A)") },
                                    onClick = { sortBy = "TITLE_DESC"; showSortMenu = false }
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opsi Lainnya",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (selectedFolderBook != null) "Buat Bab Baru" else "Buat Berkas / Buku Baru") },
                                    onClick = {
                                        showOptionsMenu = false
                                        if (selectedFolderBook != null) showCreateChapterDialog = true
                                        else showCreateBookDialog = true
                                    }
                                )
                                if (selectedFolderBook != null) {
                                    DropdownMenuItem(
                                        text = { Text("Buka Rincian & Alur Buku") },
                                        onClick = {
                                            val bookId = selectedFolderBook!!.id
                                            showOptionsMenu = false
                                            onNavigateToBookDetail(bookId)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Pencarian Global") },
                                    onClick = { showOptionsMenu = false; onOpenSearch() }
                                )
                            }
                        }
                    }
                }
            }

            // Sub-bar Breadcrumb: Down chevron "v" and "Buku-buku saya >" (Screenshot 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (selectedFolderBook != null) selectedFolderBook = null
                    }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedFolderBook != null) "Buku-buku saya > ${selectedFolderBook!!.title}" else "Buku-buku saya >",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selectedFolderBook == null) {
                // Root view: List of Folders / Books (Screenshot 1)
                if (sortedBooks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = FolderAmber.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum ada berkas atau buku.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(sortedBooks, key = { it.id }) { book ->
                            FileItemRow(
                                book = book,
                                subtitle = formatSubtitle(book.updatedAt),
                                onClick = { selectedFolderBook = book },
                                onOpenDetail = { onNavigateToBookDetail(book.id) },
                                onRename = { bookToRename = book },
                                onDuplicate = {
                                    viewModel.createBook(
                                        title = "${book.title} (Salinan)",
                                        synopsis = book.synopsis,
                                        genre = book.genre,
                                        author = book.author,
                                        status = book.status,
                                        coverColorHex = book.coverColorHex,
                                        coverUri = book.coverUri
                                    ) {}
                                },
                                onExport = { exportBook(book) },
                                onDelete = { bookToDelete = book }
                            )
                        }
                    }
                }
            } else {
                // Folder inside view: List of Chapters in Selected Book
                if (folderChapters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Folder ini belum memiliki bab. Ketuk + untuk menambah bab baru.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(folderChapters, key = { it.id }) { chapter ->
                            ChapterFileRow(
                                chapter = chapter,
                                subtitle = "${formatSubtitle(chapter.updatedAt)} • ${chapter.wordCount} kata",
                                onClick = { onNavigateToEditor("CHAPTER", chapter.id) },
                                onDelete = { chapterToDelete = chapter }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Rename Book
    if (bookToRename != null) {
        val targetBook = bookToRename!!
        var newTitle by remember { mutableStateOf(targetBook.title) }
        AlertDialog(
            onDismissRequest = { bookToRename = null },
            title = { Text("Ganti Nama Berkas", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Nama Berkas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.updateBook(targetBook.copy(title = newTitle))
                            bookToRename = null
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToRename = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Delete Book -> Trash
    if (bookToDelete != null) {
        val targetBook = bookToDelete!!
        ConfirmDialog(
            title = "Pindahkan ke Tempat Sampah?",
            message = "Berkas \"${targetBook.title}\" akan dipindahkan ke tempat sampah dan dapat dipulihkan sewaktu-waktu.",
            confirmButtonText = "Pindahkan",
            onConfirm = {
                viewModel.moveBookToTrash(targetBook.id)
                if (selectedFolderBook?.id == targetBook.id) selectedFolderBook = null
                bookToDelete = null
            },
            onDismiss = { bookToDelete = null }
        )
    }

    // Dialog Delete Chapter -> Trash
    if (chapterToDelete != null) {
        val targetCh = chapterToDelete!!
        ConfirmDialog(
            title = "Pindahkan Bab ke Tempat Sampah?",
            message = "Bab \"${targetCh.title}\" akan dipindahkan ke tempat sampah.",
            confirmButtonText = "Pindahkan",
            onConfirm = {
                viewModel.moveChapterToTrash(targetCh.id)
                chapterToDelete = null
            },
            onDismiss = { chapterToDelete = null }
        )
    }

    // Dialog Create Book
    if (showCreateBookDialog) {
        CreateBookDialog(
            onConfirm = { title, synopsis, genre, author, status, colorHex, coverUri, targetWordsVal ->
                showCreateBookDialog = false
                viewModel.createBook(title, synopsis, genre, author, status, colorHex, coverUri, targetWordsVal) { newId ->
                    onNavigateToBookDetail(newId)
                }
            },
            onDismiss = { showCreateBookDialog = false }
        )
    }

    // Dialog Create Chapter inside selected book folder
    if (showCreateChapterDialog && selectedFolderBook != null) {
        var chapterTitle by remember { mutableStateOf("Bab ${folderChapters.size + 1}") }
        AlertDialog(
            onDismissRequest = { showCreateChapterDialog = false },
            title = { Text("Buat Bab Baru", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("Judul Bab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val book = selectedFolderBook!!
                        coroutineScope.launch {
                            val nextNum = folderChapters.size + 1
                            val newId = viewModel.repository.createChapter(
                                ChapterEntity(
                                    bookId = book.id,
                                    title = chapterTitle.ifBlank { "Bab $nextNum" },
                                    chapterNumber = nextNum,
                                    orderIndex = nextNum,
                                    content = ""
                                )
                            )
                            showCreateChapterDialog = false
                            onNavigateToEditor("CHAPTER", newId)
                        }
                    }
                ) {
                    Text("Buat & Tulis")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChapterDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Single File / Folder row in Screenshot 1
 */
@Composable
fun FileItemRow(
    book: BookEntity,
    subtitle: String,
    onClick: () -> Unit,
    onOpenDetail: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("file_item_${book.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Golden Amber Folder Icon (Screenshot 1)
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = FolderAmber,
            modifier = Modifier.size(38.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title and timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        // Action Menu (•••) on right side
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Opsi",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Buka Folder Bab") },
                    onClick = { showMenu = false; onClick() }
                )
                DropdownMenuItem(
                    text = { Text("Rincian & Alur Buku") },
                    onClick = { showMenu = false; onOpenDetail() }
                )
                DropdownMenuItem(
                    text = { Text("Ganti Nama") },
                    onClick = { showMenu = false; onRename() }
                )
                DropdownMenuItem(
                    text = { Text("Salin Berkas") },
                    onClick = { showMenu = false; onDuplicate() }
                )
                DropdownMenuItem(
                    text = { Text("Ekspor Teks") },
                    onClick = { showMenu = false; onExport() }
                )
                DropdownMenuItem(
                    text = { Text("Pindahkan ke Tempat Sampah") },
                    onClick = { showMenu = false; onDelete() }
                )
            }
        }
    }
}

@Composable
private fun ChapterFileRow(
    chapter: ChapterEntity,
    subtitle: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Opsi Bab",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Buka di Editor") },
                    onClick = { showMenu = false; onClick() }
                )
                DropdownMenuItem(
                    text = { Text("Pindahkan ke Tempat Sampah") },
                    onClick = { showMenu = false; onDelete() }
                )
            }
        }
    }
}
