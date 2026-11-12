package com.example.ui.screens.settings

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CustomBackgroundSheet
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.DarkSlatePrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightPrimary
import com.example.ui.theme.OledPrimary
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.SepiaPrimary
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToTrash: () -> Unit
) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val books by viewModel.activeBooks.collectAsStateWithLifecycle()
    val notes by viewModel.activeNotes.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showWallpaperSheet by remember { mutableStateOf(false) }

    val themeOptions = listOf(
        ThemeMeta("AMETHYST", "Obsidian Magenta (Default)", AmethystPrimary, "Ungu gelap modern & aksen pink"),
        ThemeMeta("SYSTEM", "Ikuti Sistem", Color(0xFF64748B), "Sesuai mode perangkat"),
        ThemeMeta("LIGHT", "Kertas Terang", LightPrimary, "Minimalis dan bersih"),
        ThemeMeta("DARK_SLATE", "Dark Slate", DarkSlatePrimary, "Abu-abu gelap 2026"),
        ThemeMeta("OLED", "Hitam Murni", OledPrimary, "OLED pekat hemat daya"),
        ThemeMeta("SEPIA", "Parchment Klasik", SepiaPrimary, "Warna kertas buku kuno"),
        ThemeMeta("SAPPHIRE", "Sapphire Navy", SapphirePrimary, "Biru royal elegan"),
        ThemeMeta("EMERALD", "Nordic Emerald", EmeraldPrimary, "Hijau pinus menenangkan"),
        ThemeMeta("CRIMSON", "Ruby Manuscript", CrimsonPrimary, "Merah delima & emas")
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header 2026
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.testTag("settings_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Pengaturan",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Novel Desk v1.6.4",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 1. Sistem Tema Dinamis
            item {
                SettingsSectionCard(title = "Sistem Tema Dinamis") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        themeOptions.forEach { opt ->
                            val isSelected = preferences.themeMode.uppercase() == opt.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                    .clickable { viewModel.updateTheme(opt.id) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(opt.accentColor)
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = opt.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = opt.desc,
                                            fontSize = 10.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Aktif",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Wallpaper & Latar Galeri
            item {
                SettingsSectionCard(title = "Kustomisasi Latar Belakang") {
                    val hasCustomBg = preferences.customBgPath.isNotBlank()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showWallpaperSheet = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Wallpaper dari Galeri Ponsel",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (hasCustomBg) "Wallpaper khusus aktif (${(preferences.bgOverlayOpacity * 100).toInt()}% kegelapan)" else "Gunakan foto favorit sebagai latar",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showWallpaperSheet = true },
                            modifier = Modifier.testTag("open_wallpaper_sheet_button")
                        ) {
                            Text("Atur", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Keamanan & PIN Lock
            item {
                SettingsSectionCard(title = "Keamanan Aplikasi") {
                    val isPinEnabled = preferences.securityPin.isNotBlank()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Kunci Aplikasi (PIN 4-Digit)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isPinEnabled) "Aplikasi terkunci dengan PIN" else "Kunci belum diaktifkan",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinDialog = true
                                } else {
                                    viewModel.updateSecurityPin("")
                                    Toast.makeText(context, "Kunci PIN dinonaktifkan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (isPinEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { showPinDialog = true }) {
                            Text("Ubah Kode PIN", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 4. Cadangan & Pemulihan (Clean UI without raw JSON text)
            item {
                SettingsSectionCard(title = "Cadangan & Pemulihan Naskah") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBackupDialog = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Buat Cadangan Lengkap", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Amankan seluruh buku, bab, dan catatan Anda", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRestoreDialog = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Pulihkan Data Naskah", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Kembalikan tulisan dari berkas cadangan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 5. Tempat Sampah
            item {
                SettingsSectionCard(title = "Penyimpanan & Tempat Sampah") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTrash() }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Buka Tempat Sampah", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Pulihkan atau bersihkan naskah yang telah dihapus", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 6. Informasi Aplikasi 1.6.4
            item {
                SettingsSectionCard(title = "Tentang Aplikasi") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Novel Desk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Versi 1.6.4 • Edisi Profesional 2026", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal Sheet Wallpaper
    if (showWallpaperSheet) {
        CustomBackgroundSheet(
            preferences = preferences,
            onSaveBackground = { path, opacity, position ->
                viewModel.updateCustomBackground(path, opacity, position)
            },
            onRemoveBackground = {
                viewModel.clearCustomBackground()
            },
            onDismiss = { showWallpaperSheet = false }
        )
    }

    // Dialog Set PIN
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Atur PIN Keamanan (4 Digit)", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                    label = { Text("Masukkan 4 Digit Angka") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pin_code_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.updateSecurityPin(pinInput)
                            showPinDialog = false
                            Toast.makeText(context, "Kunci PIN berhasil diaktifkan", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN harus 4 digit angka", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("confirm_pin_button")
                ) {
                    Text("Simpan PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Backup (Clean summary, NO raw JSON printed on screen)
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Cadangan Seluruh Naskah", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Arsip cadangan akan mengamankan seluruh karya Anda:",
                        fontSize = 13.sp
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• ${books.size} Buku & Novel Lengkap", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("• ${notes.size} Catatan Cepat & Memo", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("• Unsur alur cerita & riwayat tersimpan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = "Data dapat dibagikan atau disimpan ke tempat penyimpanan aman Anda.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val backupText = viewModel.repository.exportAllDataToJson()
                            CoroutineScope(Dispatchers.Main).launch {
                                showBackupDialog = false
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Novel Desk - Cadangan Data Naskah Lengkap")
                                    putExtra(Intent.EXTRA_TEXT, backupText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Cadangan Naskah"))
                            }
                        }
                    }
                ) {
                    Text("Bagikan & Simpan Cadangan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Dialog Restore (Clean UI without raw JSON output)
    if (showRestoreDialog) {
        var restoreText by remember { mutableStateOf("") }
        var isAnalyzing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Pulihkan Data Naskah", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tempel teks berkas cadangan dari papan klip (clipboard) Anda:",
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val text = clip.getItemAt(0).text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    restoreText = text
                                    Toast.makeText(context, "Data berhasil ditempel dari papan klip", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Papan klip kosong", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (restoreText.isNotBlank()) "Data Cadangan Ditempel" else "Tempel dari Papan Klip")
                    }

                    if (restoreText.isNotBlank()) {
                        // Validate and show human-readable summary
                        val summary = try {
                            val root = JSONObject(restoreText)
                            val booksCount = root.optJSONArray("books")?.length() ?: 0
                            val notesCount = root.optJSONArray("notes")?.length() ?: 0
                            "Siap memulihkan $booksCount buku dan $notesCount catatan."
                        } catch (e: Exception) {
                            "Format data tidak dikenali. Pastikan teks cadangan disalin secara utuh."
                        }

                        Text(
                            text = summary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.startsWith("Siap")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreText.isNotBlank()) {
                            isAnalyzing = true
                            CoroutineScope(Dispatchers.IO).launch {
                                val success = viewModel.repository.restoreDataFromJson(restoreText)
                                CoroutineScope(Dispatchers.Main).launch {
                                    isAnalyzing = false
                                    showRestoreDialog = false
                                    if (success) {
                                        Toast.makeText(context, "Naskah berhasil dipulihkan ke Perpustakaan!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Gagal memproses data cadangan", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = restoreText.isNotBlank() && !isAnalyzing
                ) {
                    Text("Mulai Pemulihan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog About 1.6.4
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "Novel Desk",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Versi 1.6.4 (Pembaruan Desain 2026)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Aplikasi penulisan novel dan buku profesional dengan sistem tema dinamis, kustomisasi wallpaper galeri, editor bebas distraksi, manajemen alur cerita, dan pelindung data otomatis.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dirancang dengan Material Design 3 Expressive untuk kenyamanan membaca dan menulis.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

private data class ThemeMeta(
    val id: String,
    val name: String,
    val accentColor: Color,
    val desc: String
)

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    }
}
