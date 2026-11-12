package com.example.ui.screens.library

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.ImageStorageHelper
import java.io.File

@Composable
fun CreateBookDialog(
    initialTitle: String = "",
    initialSynopsis: String = "",
    initialGenre: String = "Fantasi Epik",
    initialAuthor: String = "Penulis",
    initialStatus: String = "DRAFT",
    initialColorHex: String = "#1E293B",
    initialCoverUri: String = "",
    initialTargetWords: Int = 50000,
    isEditing: Boolean = false,
    onConfirm: (
        title: String,
        synopsis: String,
        genre: String,
        author: String,
        status: String,
        colorHex: String,
        coverUri: String,
        targetWords: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialTitle) }
    var synopsis by remember { mutableStateOf(initialSynopsis) }
    var genre by remember { mutableStateOf(initialGenre) }
    var author by remember { mutableStateOf(initialAuthor) }
    var status by remember { mutableStateOf(initialStatus) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }
    var coverUri by remember { mutableStateOf(initialCoverUri) }
    var targetWords by remember { mutableIntStateOf(initialTargetWords) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImagePermanently(
                context = context,
                uri = uri,
                subDirName = "book_covers",
                fileName = "cover_book_${System.currentTimeMillis()}.jpg"
            )
            if (savedPath != null) {
                coverUri = savedPath
                Toast.makeText(context, "Sampul foto berhasil dipilih", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val presetColors = listOf(
        "#1E293B", // Navy
        "#881337", // Crimson
        "#064E3B", // Emerald
        "#78350F", // Amber
        "#312E81", // Indigo
        "#134E4A", // Teal
        "#374151"  // Charcoal
    )

    val genreList = listOf(
        "Fantasi Epik", "Fiksi Ilmiah", "Romansa", "Misteri & Thriller",
        "Fiksi Sejarah", "Horor", "Non-Fiksi", "Cerpen", "Drama"
    )

    val statusList = listOf(
        "IDE" to "Ide Cerita",
        "DRAFT" to "Sedang Ditulis",
        "REVISI" to "Revisi",
        "SELESAI" to "Selesai"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Ubah Rincian Buku & Sampul" else "Buat Buku Novel Baru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Book Cover Preview & Picker Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Sampul Buku (Cover)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 2:3 ratio thumbnail
                        val fallbackColor = try {
                            Color(android.graphics.Color.parseColor(selectedColorHex))
                        } catch (e: Exception) {
                            Color(0xFF1E293B)
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 115.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(fallbackColor)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            if (coverUri.isNotBlank() && File(coverUri).exists()) {
                                AsyncImage(
                                    model = File(coverUri),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(fallbackColor.copy(alpha = 0.85f), fallbackColor)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title.ifBlank { "Sampul" },
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(6.dp),
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.testTag("pick_book_cover_button")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (coverUri.isNotBlank()) "Ganti Sampul Foto" else "Pilih dari Galeri", fontSize = 11.sp)
                            }

                            if (coverUri.isNotBlank()) {
                                TextButton(
                                    onClick = { coverUri = "" },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gunakan Warna Standar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Buku / Novel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("book_title_input")
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Nama Penulis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("book_author_input")
                )

                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Sinopsis / Deskripsi Cerita") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("book_synopsis_input")
                )

                // Genre selection chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Genre", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genreList.forEach { g ->
                            val isSelected = genre == g
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { genre = g }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Status selection chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Status Proyek", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statusList.forEach { (stKey, stLabel) ->
                            val isSelected = status == stKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { status = stKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stLabel,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Cover Color Palette (Used if no photo cover)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Warna Sampul Dasar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColors.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorHex = hex }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title.ifBlank { "Buku Tanpa Judul" },
                        synopsis,
                        genre,
                        author.ifBlank { "Penulis" },
                        status,
                        selectedColorHex,
                        coverUri,
                        targetWords
                    )
                },
                modifier = Modifier.testTag("confirm_create_book_button")
            ) {
                Text(if (isEditing) "Simpan Perubahan" else "Buat Buku")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
