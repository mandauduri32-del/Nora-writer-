package com.example.ui.screens.library

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookEntity
import com.example.ui.components.BookCoverCard
import com.example.ui.components.BookGridItem
import com.example.ui.components.ConfirmDialog
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Screen "Buku" matching Screenshot 2 (Screenshot_20261112-113737_43d89264.png)
 */
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToBookDetail: (Long) -> Unit,
    onNavigateToEditor: (String, Long) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val books by viewModel.activeBooks.collectAsStateWithLifecycle()
    val recentChapters by viewModel.recentChapters.collectAsStateWithLifecycle()
    val allChapters by viewModel.allActiveChapters.collectAsStateWithLifecycle()

    var isGridView by remember { mutableStateOf(true) } // Default 3-column grid view (Screenshot 2)
    var showCreateBookDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("UPDATED_DESC") }
    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }

    val statsByBookId = remember(allChapters) {
        allChapters.groupBy { it.bookId }.mapValues { (_, list) ->
            Pair(list.size, list.sumOf { it.wordCount })
        }
    }

    val lastEditedChapter = recentChapters.firstOrNull()
    val recentEditingLabel = if (lastEditedChapter != null) {
        val parentBook = books.find { it.id == lastEditedChapter.bookId }
        parentBook?.title ?: lastEditedChapter.title
    } else {
        books.firstOrNull()?.title ?: "Mulai menulis"
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Top Bar: Hamburger, Rounded Outlined Search Button, Sort Icon, More Options (Screenshot 2)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hamburger Menu Icon
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("library_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Center Outlined Search Pill "Pencarian teks lengkap"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { onOpenSearch() }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pencarian teks lengkap",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sort Filter Icon
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
                                text = { Text("Terbaru disunting") },
                                onClick = { sortBy = "UPDATED_DESC"; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Terlama disunting") },
                                onClick = { sortBy = "UPDATED_ASC"; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Judul (A - Z)") },
                                onClick = { sortBy = "TITLE_ASC"; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Judul (Z - A)") },
                                onClick = { sortBy = "TITLE_DESC"; showSortMenu = false }
                            )
                        }
                    }

                    // More Options Icon
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
                                text = { Text("Buat Buku / Novel Baru") },
                                onClick = { showOptionsMenu = false; showCreateBookDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Tampilan ${if (isGridView) "Daftar" else "Kisi"}") },
                                onClick = { isGridView = !isGridView; showOptionsMenu = false }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBookDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("library_add_book_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Buku Baru", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Sub-bar: "Penyuntingan terkini: <judul> >" and Grid/List toggle (Screenshot 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (lastEditedChapter != null) {
                                onNavigateToEditor("CHAPTER", lastEditedChapter.id)
                            } else if (books.isNotEmpty()) {
                                onNavigateToBookDetail(books.first().id)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Penyuntingan terkini: $recentEditingLabel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Lanjutkan Menulis",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Grid / List View Toggle Button (Screenshot 2 shows list icon when in grid mode)
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.size(36.dp).testTag("toggle_view_mode_button")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.FormatListBulleted else Icons.Default.GridView,
                        contentDescription = if (isGridView) "Beralih ke Daftar" else "Beralih ke Kisi",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Main Content: 3-Column Grid or List
            if (sortedBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada buku atau novel. Ketuk tombol + untuk mulai menulis.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isGridView) {
                // 3 Columns Grid matching Screenshot 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedBooks, key = { it.id }) { book ->
                        BookGridItem(
                            book = book,
                            onClick = { onNavigateToBookDetail(book.id) },
                            onToggleFavorite = { viewModel.toggleBookFavorite(book) },
                            onExport = { exportBook(book) },
                            onDelete = { bookToDelete = book }
                        )
                    }
                }
            } else {
                // List View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sortedBooks, key = { it.id }) { book ->
                        val (chapCount, wordTotal) = statsByBookId[book.id] ?: Pair(0, 0)
                        BookCoverCard(
                            book = book,
                            totalWords = wordTotal,
                            chapterCount = chapCount,
                            isGridView = false,
                            onClick = { onNavigateToBookDetail(book.id) },
                            onToggleFavorite = { viewModel.toggleBookFavorite(book) },
                            onEdit = { onNavigateToBookDetail(book.id) },
                            onDelete = { bookToDelete = book },
                            onExport = { exportBook(book) }
                        )
                    }
                }
            }
        }
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

    // Dialog Confirm Delete
    if (bookToDelete != null) {
        val targetBook = bookToDelete!!
        ConfirmDialog(
            title = "Pindahkan ke Tempat Sampah?",
            message = "Buku \"${targetBook.title}\" beserta seluruh bab di dalamnya akan dipindahkan ke tempat sampah.",
            confirmButtonText = "Pindahkan",
            onConfirm = {
                viewModel.moveBookToTrash(targetBook.id)
                bookToDelete = null
            },
            onDismiss = { bookToDelete = null }
        )
    }
}
