package com.example.ui.screens.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoryElementEntity

@Composable
fun StoryElementDialog(
    element: StoryElementEntity? = null,
    defaultType: String = "CHARACTER",
    onConfirm: (type: String, name: String, role: String, desc: String, details: String) -> Unit,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(element?.type ?: defaultType) }
    var name by remember { mutableStateOf(element?.name ?: "") }
    var role by remember { mutableStateOf(element?.roleOrTag ?: "") }
    var desc by remember { mutableStateOf(element?.description ?: "") }
    var details by remember { mutableStateOf(element?.details ?: "") }

    val types = listOf(
        "CHARACTER" to "Karakter",
        "LOCATION" to "Lokasi",
        "PLOT_EVENT" to "Alur Cerita",
        "WORLDBUILDING" to "Dunia Fiksi",
        "GENERAL_NOTE" to "Catatan Riset"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (element == null) "Tambah Unsur Cerita" else "Ubah Unsur Cerita",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { (tKey, tLabel) ->
                        val isSelected = type == tKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { type = tKey }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            when (type) {
                                "CHARACTER" -> "Nama Karakter"
                                "LOCATION" -> "Nama Lokasi / Tempat"
                                "PLOT_EVENT" -> "Peristiwa / Babak Cerita"
                                "WORLDBUILDING" -> "Nama Konsep / Sistem Magis"
                                else -> "Judul Catatan"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("element_name_input")
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = {
                        Text(
                            when (type) {
                                "CHARACTER" -> "Peran (Protagonis / Antagonis / Pendukung)"
                                "LOCATION" -> "Wilayah / Iklim / Tipe Tempat"
                                "PLOT_EVENT" -> "Babak (Insiden Pemicu / Klimaks / Bab Terkait)"
                                else -> "Label / Kategori Singkat"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("element_role_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi Utama") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("element_desc_input")
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Catatan Tambahan & Hubungan") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("element_details_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(type, name.ifBlank { "Unsur Tanpa Nama" }, role, desc, details)
                },
                modifier = Modifier.testTag("confirm_element_button")
            ) {
                Text(if (element == null) "Tambahkan" else "Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
