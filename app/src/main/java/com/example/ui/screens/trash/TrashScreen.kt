package com.example.ui.screens.trash

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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfirmDialog
import com.example.viewmodel.MainViewModel

@Composable
fun TrashScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val trashBooks by viewModel.trashBooks.collectAsStateWithLifecycle()
    val trashChapters by viewModel.trashChapters.collectAsStateWithLifecycle()
    val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var itemToDeletePermanently by remember { mutableStateOf<Triple<String, Long, String>?>(null) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }

    val totalTrashItems = trashBooks.size + trashChapters.size + trashNotes.size

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.testTag("trash_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Tempat Sampah",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (totalTrashItems > 0) {
                            TextButton(
                                onClick = { showEmptyTrashConfirm = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Kosongkan", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Buku (${trashBooks.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Bab (${trashChapters.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Catatan (${trashNotes.size})", fontSize = 12.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Tab Content
            when (selectedTab) {
                0 -> {
                    if (trashBooks.isEmpty()) {
                        EmptyTrashView(message = "Tidak ada buku di tempat sampah.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(trashBooks) { book ->
                                TrashItemRow(
                                    title = book.title,
                                    subtitle = "${book.author} • ${book.genre}",
                                    onRestore = { viewModel.restoreBook(book.id) },
                                    onDeletePermanently = {
                                        itemToDeletePermanently = Triple("Buku", book.id, book.title)
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (trashChapters.isEmpty()) {
                        EmptyTrashView(message = "Tidak ada bab naskah di tempat sampah.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(trashChapters) { ch ->
                                TrashItemRow(
                                    title = ch.title,
                                    subtitle = "${ch.wordCount} kata",
                                    onRestore = { viewModel.restoreChapter(ch.id) },
                                    onDeletePermanently = {
                                        itemToDeletePermanently = Triple("Bab", ch.id, ch.title)
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    if (trashNotes.isEmpty()) {
                        EmptyTrashView(message = "Tidak ada catatan di tempat sampah.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(trashNotes) { note ->
                                TrashItemRow(
                                    title = note.title,
                                    subtitle = note.category,
                                    onRestore = { viewModel.restoreNote(note.id) },
                                    onDeletePermanently = {
                                        itemToDeletePermanently = Triple("Catatan", note.id, note.title)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Permanent Delete Confirmation
    itemToDeletePermanently?.let { (type, id, name) ->
        ConfirmDialog(
            title = "Hapus Permanen $type?",
            message = "Apakah Anda yakin ingin menghapus '$name' secara permanen? Data tidak dapat dikembalikan lagi setelah dihapus.",
            confirmButtonText = "Hapus Permanen",
            cancelButtonText = "Batal",
            isDestructive = true,
            onConfirm = {
                when (type) {
                    "Buku" -> viewModel.deleteBookPermanently(id)
                    "Bab" -> viewModel.deleteChapterPermanently(id)
                    "Catatan" -> viewModel.deleteNotePermanently(id)
                }
                itemToDeletePermanently = null
            },
            onDismiss = { itemToDeletePermanently = null }
        )
    }

    // Empty Trash confirmation
    if (showEmptyTrashConfirm) {
        ConfirmDialog(
            title = "Kosongkan Tempat Sampah?",
            message = "Semua buku, bab, dan catatan di Tempat Sampah akan dihapus secara permanen.",
            confirmButtonText = "Kosongkan Sekarang",
            cancelButtonText = "Batal",
            isDestructive = true,
            onConfirm = {
                trashBooks.forEach { viewModel.deleteBookPermanently(it.id) }
                trashChapters.forEach { viewModel.deleteChapterPermanently(it.id) }
                trashNotes.forEach { viewModel.deleteNotePermanently(it.id) }
                showEmptyTrashConfirm = false
            },
            onDismiss = { showEmptyTrashConfirm = false }
        )
    }
}

@Composable
private fun TrashItemRow(
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Pulihkan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeletePermanently, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Hapus Permanen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTrashView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
