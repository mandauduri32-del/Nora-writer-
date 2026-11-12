package com.example.ui.screens.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.QuickNoteEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.GoldAccent
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen "Catatan" matching Screenshot 3 (Screenshot_20261112-113759_a23b1147.png)
 */
@Composable
fun NotesScreen(
    viewModel: MainViewModel,
    onNavigateToEditor: (String, Long) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val notes by viewModel.activeNotes.collectAsStateWithLifecycle()

    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("UPDATED_DESC") }
    var noteToDelete by remember { mutableStateOf<QuickNoteEntity?>(null) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("id", "ID"))

    // Sort notes
    val sortedNotes = remember(notes, sortBy) {
        when (sortBy) {
            "TITLE_ASC" -> notes.sortedWith(compareByDescending<QuickNoteEntity> { it.isPinned }.thenBy { it.title.lowercase() })
            "TITLE_DESC" -> notes.sortedWith(compareByDescending<QuickNoteEntity> { it.isPinned }.thenByDescending { it.title.lowercase() })
            "WORDS_DESC" -> notes.sortedWith(compareByDescending<QuickNoteEntity> { it.isPinned }.thenByDescending { it.wordCount })
            else -> notes.sortedWith(compareByDescending<QuickNoteEntity> { it.isPinned }.thenByDescending { it.updatedAt })
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Top Bar: Hamburger, "Catatan", Search, Sort, More Options (Screenshot 3)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("notes_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Buka Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Catatan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenSearch,
                            modifier = Modifier.testTag("notes_search_button")
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
                                    text = { Text("Judul (A - Z)") },
                                    onClick = { sortBy = "TITLE_ASC"; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Jumlah Kata Terbanyak") },
                                    onClick = { sortBy = "WORDS_DESC"; showSortMenu = false }
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
                                    text = { Text("Buat Catatan Baru") },
                                    onClick = { showOptionsMenu = false; showCreateNoteDialog = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Pencarian Global") },
                                    onClick = { showOptionsMenu = false; onOpenSearch() }
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateNoteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("notes_create_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Catatan Baru", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Main Content: 2-Column Grid of Note Cards (Screenshot 3)
            if (sortedNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada catatan atau memo. Ketuk tombol + untuk membuat catatan baru.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedNotes, key = { it.id }) { note ->
                        NoteGridCard(
                            note = note,
                            formattedDate = dateFormat.format(Date(note.updatedAt)),
                            onClick = { onNavigateToEditor("NOTE", note.id) },
                            onTogglePin = { viewModel.toggleNotePin(note) },
                            onDelete = { noteToDelete = note }
                        )
                    }
                }
            }
        }
    }

    // Dialog Create Note
    if (showCreateNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }
        var noteCategory by remember { mutableStateOf("Ide Cerita") }
        val categoryChoices = listOf("Ide Cerita", "Karakter", "Dialog", "Riset", "Umum")

        AlertDialog(
            onDismissRequest = { showCreateNoteDialog = false },
            title = { Text("Buat Catatan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Judul Catatan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoryChoices.take(3).forEach { cat ->
                            FilterChip(
                                selected = noteCategory == cat,
                                onClick = { noteCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Isi Catatan / Ide") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = noteTitle.ifBlank { "Catatan Baru" }
                        viewModel.createNote(finalTitle, noteContent, noteCategory) { newId ->
                            showCreateNoteDialog = false
                            onNavigateToEditor("NOTE", newId)
                        }
                    }
                ) {
                    Text("Buat & Buka")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Delete Note
    if (noteToDelete != null) {
        val targetNote = noteToDelete!!
        ConfirmDialog(
            title = "Hapus Catatan?",
            message = "Catatan \"${targetNote.title}\" akan dipindahkan ke tempat sampah.",
            confirmButtonText = "Hapus",
            onConfirm = {
                viewModel.moveNoteToTrash(targetNote.id)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }
}

/**
 * 2-Column Note Card matching Screenshot 3
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteGridCard(
    note: QuickNoteEntity,
    formattedDate: String,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .testTag("note_grid_card_${note.id}"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Snippet preview (4-5 lines of excerpt like in Screenshot 3)
                Text(
                    text = note.content.ifBlank { "(Catatan kosong)" },
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )

                // Card Footer: Title on bottom-left, date on bottom-right (Screenshot 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = "Disematkan",
                                tint = GoldAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = note.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formattedDate,
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Buka & Sunting") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = { showMenu = false; onClick() }
            )
            DropdownMenuItem(
                text = { Text(if (note.isPinned) "Lepas Sematan" else "Sematkan di Atas") },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                onClick = { showMenu = false; onTogglePin() }
            )
            DropdownMenuItem(
                text = { Text("Hapus ke Sampah") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = { showMenu = false; onDelete() }
            )
        }
    }
}
