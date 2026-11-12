package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Side Navigation Drawer matching Screenshot 4 (Screenshot_20261112-113749_d22a56e2.png)
 */
@Composable
fun AppSideDrawer(
    viewModel: MainViewModel,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val logs by viewModel.recentWritingLogs.collectAsStateWithLifecycle()
    val books by viewModel.activeBooks.collectAsStateWithLifecycle()
    val notes by viewModel.activeNotes.collectAsStateWithLifecycle()

    var showTautanDialog by remember { mutableStateOf(false) }
    var showCadanganDialog by remember { mutableStateOf(false) }
    var showRiwayatDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background,
        drawerContentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .width(136.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section Items: Tautan, Cadangan, Riwayat, Tempat sampah (Screenshot 4)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DrawerItemButton(
                    icon = Icons.Default.Laptop,
                    label = "Tautan",
                    onClick = {
                        onCloseDrawer()
                        showTautanDialog = true
                    },
                    testTag = "drawer_item_tautan"
                )

                DrawerItemButton(
                    icon = Icons.Default.CloudQueue,
                    label = "Cadangan",
                    onClick = {
                        onCloseDrawer()
                        showCadanganDialog = true
                    },
                    testTag = "drawer_item_cadangan"
                )

                DrawerItemButton(
                    icon = Icons.Default.History,
                    label = "Riwayat",
                    onClick = {
                        onCloseDrawer()
                        showRiwayatDialog = true
                    },
                    testTag = "drawer_item_riwayat"
                )

                DrawerItemButton(
                    icon = Icons.Default.DeleteOutline,
                    label = "Tempat sampah",
                    onClick = {
                        onCloseDrawer()
                        onNavigateToTrash()
                    },
                    testTag = "drawer_item_trash"
                )
            }

            // Bottom Item: Pengaturan (Screenshot 4)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DrawerItemButton(
                    icon = Icons.Default.Settings,
                    label = "Pengaturan",
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSettings()
                    },
                    testTag = "drawer_item_settings"
                )
            }
        }
    }

    // Dialog: Tautan & Penyimpanan Lokal
    if (showTautanDialog) {
        AlertDialog(
            onDismissRequest = { showTautanDialog = false },
            title = { Text("Tautan & Sinkronisasi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kelola tautan cadangan lokal dan ekspor proyek Anda secara aman.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Status: Penyimpanan Lokal Aktif",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Seluruh naskah dan bab Anda tersimpan aman di basis data perangkat. Gunakan menu Cadangan untuk membagikan arsip.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showTautanDialog = false }) {
                    Text("Mengerti")
                }
            }
        )
    }

    // Dialog: Cadangan Seluruh Naskah
    if (showCadanganDialog) {
        AlertDialog(
            onDismissRequest = { showCadanganDialog = false },
            title = { Text("Cadangan Naskah Lengkap", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Arsipkan karya Anda ke berkas cadangan aman:",
                        fontSize = 13.sp
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• ${books.size} Buku & Novel Aktif", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("• ${notes.size} Catatan & Memo Tersimpan", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val backupText = viewModel.repository.exportAllDataToJson()
                            withContext(Dispatchers.Main) {
                                showCadanganDialog = false
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Novel Desk - Cadangan Data Naskah")
                                    putExtra(Intent.EXTRA_TEXT, backupText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Cadangan Naskah"))
                            }
                        }
                    }
                ) {
                    Text("Ekspor & Bagikan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCadanganDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Dialog: Riwayat Aktivitas Menulis
    if (showRiwayatDialog) {
        AlertDialog(
            onDismissRequest = { showRiwayatDialog = false },
            title = { Text("Riwayat Aktivitas Menulis", fontWeight = FontWeight.Bold) },
            text = {
                if (logs.isEmpty()) {
                    Text("Belum ada riwayat sesi menulis yang tercatat.", fontSize = 13.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(log.dateStr, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("${log.writingTimeMinutes} menit sesi", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "+${log.wordsWritten} kata",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRiwayatDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

/**
 * Individual Drawer Item Button (Stacked icon above label matching Screenshot 4)
 */
@Composable
fun DrawerItemButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
