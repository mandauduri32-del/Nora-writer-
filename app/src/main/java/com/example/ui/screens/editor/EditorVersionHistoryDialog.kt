package com.example.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DocumentVersionEntity
import com.example.ui.components.ConfirmDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditorVersionHistoryDialog(
    versions: List<DocumentVersionEntity>,
    onCreateSnapshot: (String) -> Unit,
    onRestoreVersion: (DocumentVersionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var snapshotNote by remember { mutableStateOf("") }
    var selectedVersionForPreview by remember { mutableStateOf<DocumentVersionEntity?>(null) }
    var showRestoreConfirmFor by remember { mutableStateOf<DocumentVersionEntity?>(null) }

    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Riwayat Versi Naskah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Create new snapshot row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = snapshotNote,
                        onValueChange = { snapshotNote = it },
                        placeholder = { Text("Catatan versi (misal: Selesai Babak 1)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("snapshot_note_input")
                    )
                    Button(
                        onClick = {
                            onCreateSnapshot(snapshotNote.ifBlank { "Snapshot Tersimpan" })
                            snapshotNote = ""
                        },
                        modifier = Modifier.testTag("save_snapshot_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Versions list
                if (versions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada rekaman versi terdahulu.\nKlik 'Simpan' untuk mencatat versi naskah saat ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(versions) { ver ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { selectedVersionForPreview = ver }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ver.summary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${sdf.format(Date(ver.timestamp))} • ${ver.wordCount} kata",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { showRestoreConfirmFor = ver },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = "Pulihkan",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Tutup")
                    }
                }
            }
        }
    }

    // Preview Dialog
    selectedVersionForPreview?.let { previewVer ->
        AlertDialog(
            onDismissRequest = { selectedVersionForPreview = null },
            title = {
                Text(
                    text = "Pratinjau Versi: ${previewVer.summary}",
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Waktu: ${sdf.format(Date(previewVer.timestamp))} (${previewVer.wordCount} kata)\n",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = previewVer.content,
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedVersionForPreview = null
                        showRestoreConfirmFor = previewVer
                    }
                ) {
                    Text("Pulihkan Versi Ini")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedVersionForPreview = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Restore confirmation
    showRestoreConfirmFor?.let { restoreVer ->
        ConfirmDialog(
            title = "Pulihkan Versi Lama?",
            message = "Naskah saat ini akan digantikan dengan isi versi '${restoreVer.summary}' dari tanggal ${sdf.format(Date(restoreVer.timestamp))}. Tindakan ini tidak dapat dibatalkan.",
            confirmButtonText = "Ya, Pulihkan",
            cancelButtonText = "Batal",
            onConfirm = {
                onRestoreVersion(restoreVer)
                showRestoreConfirmFor = null
                onDismiss()
            },
            onDismiss = { showRestoreConfirmFor = null }
        )
    }
}
