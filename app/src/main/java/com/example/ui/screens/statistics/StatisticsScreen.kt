package com.example.ui.screens.statistics

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * Screen "Statistik" matching Screenshot 5 (Screenshot_20261112-113820_2628d41d.png)
 */
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit
) {
    val books by viewModel.activeBooks.collectAsStateWithLifecycle()
    val allChapters by viewModel.allActiveChapters.collectAsStateWithLifecycle()
    val notes by viewModel.activeNotes.collectAsStateWithLifecycle()
    val logs by viewModel.recentWritingLogs.collectAsStateWithLifecycle()
    val totalWordsAllChapters by viewModel.totalWordsAllChapters.collectAsStateWithLifecycle()
    val totalWordsAllNotes by viewModel.totalWordsAllNotes.collectAsStateWithLifecycle()
    val totalCharsAllChapters by viewModel.totalCharsAllChapters.collectAsStateWithLifecycle()
    val totalChapterCount by viewModel.totalChapterCount.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("STATISTIK") } // "STATISTIK" or "DISTRIBUSI"
    var showDailyOnly by remember { mutableStateOf(false) } // Toggle via top BarChart icon
    var showOptionsMenu by remember { mutableStateOf(false) }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("en", "US")) }
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID")).format(Date()) }

    // Real totals computed from Room DB
    val totalWords = totalWordsAllChapters + totalWordsAllNotes
    val totalNotesChars = remember(notes) { notes.sumOf { it.content.length } }
    val totalAllChars = totalCharsAllChapters + totalNotesChars
    val totalLiteralChars = remember(allChapters, notes) {
        val chLiterals = allChapters.sumOf { ch -> ch.content.count { !it.isWhitespace() } }
        val noteLiterals = notes.sumOf { n -> n.content.count { !it.isWhitespace() } }
        chLiterals + noteLiterals
    }
    val totalCjkChars = remember(allChapters, notes) {
        fun countCjk(text: String): Int = text.count { ch ->
            val block = Character.UnicodeBlock.of(ch)
            block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.HIRAGANA ||
                block == Character.UnicodeBlock.KATAKANA ||
                block == Character.UnicodeBlock.HANGUL_SYLLABLES
        }
        allChapters.sumOf { countCjk(it.content) } + notes.sumOf { countCjk(it.content) }
    }

    val booksCount = books.size
    val filesCount = totalChapterCount + notes.size

    // Build real 7-day trend points (oldest -> newest)
    val dayNameFmt = remember { SimpleDateFormat("EEE", Locale("id", "ID")) }
    val dateKeyFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val sevenDayTrend = remember(logs, totalLiteralChars, showDailyOnly) {
        val logMap = logs.associateBy { it.dateStr }
        val cal = Calendar.getInstance()
        val days = (6 downTo 0).map { offset ->
            val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val key = dateKeyFmt.format(dayCal.time)
            val label = dayNameFmt.format(dayCal.time).replace(".", "").take(3).replaceFirstChar { it.uppercase() }
            val writtenWords = logMap[key]?.wordsWritten ?: 0
            Pair(label, writtenWords)
        }

        if (showDailyOnly) {
            days.map { it.first to it.second.toFloat() }
        } else {
            // Cumulative progression leading to current totalWords/literal count
            val weeklyAdded = days.sumOf { it.second }
            val baseline = max(0, totalWords - weeklyAdded)
            var running = baseline
            days.map { (label, added) ->
                running += added
                label to running.toFloat()
            }
        }
    }

    val weeklyWordsSum = remember(logs) { logs.sumOf { it.wordsWritten } }
    val headlineValue = if (showDailyOnly) {
        logs.firstOrNull()?.wordsWritten ?: 0
    } else {
        totalLiteralChars.takeIf { it > 0 } ?: totalWords
    }
    val diffText = "+ ${numberFormat.format(weeklyWordsSum)}"

    var targetSliderValue by remember(preferences.dailyWordTarget) {
        mutableFloatStateOf(preferences.dailyWordTarget.toFloat())
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar with Hamburger, Tabs ("Statistik" & "Distribusi"), and Action Icons (Screenshot 5)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("stats_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Buka Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Tab "Statistik"
                        Column(
                            modifier = Modifier
                                .clickable { selectedTab = "STATISTIK" }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Statistik",
                                fontSize = 15.sp,
                                fontWeight = if (selectedTab == "STATISTIK") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == "STATISTIK") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (selectedTab == "STATISTIK") {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }

                        // Tab "Distribusi"
                        Column(
                            modifier = Modifier
                                .clickable { selectedTab = "DISTRIBUSI" }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Distribusi",
                                fontSize = 15.sp,
                                fontWeight = if (selectedTab == "DISTRIBUSI") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == "DISTRIBUSI") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (selectedTab == "DISTRIBUSI") {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }

                    // Action Icons on Right (BarChart mode switch and MoreVert)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showDailyOnly = !showDailyOnly }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = if (showDailyOnly) "Lihat Tren Kumulatif" else "Lihat Kata Harian",
                                tint = if (showDailyOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opsi",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (showDailyOnly) "Tampilkan Tren Total" else "Tampilkan Kata Harian") },
                                    onClick = { showDailyOnly = !showDailyOnly; showOptionsMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Atur Target Harian") },
                                    onClick = { selectedTab = "DISTRIBUSI"; showOptionsMenu = false }
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedTab == "STATISTIK") {
                    // Card 1: "Tren - Literal" with Smooth Area Curve Chart (Screenshot 5)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (showDailyOnly) "Tren - Kata Harian" else "Tren - Literal",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = numberFormat.format(headlineValue),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = todayDateStr,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = diffText,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Dibandingkan dengan 7 hari sebelumnya",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Spline Area Chart with real axis labels (Screenshot 5)
                                TrendSplineAreaChart(
                                    pointsData = sevenDayTrend,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    gridColor = MaterialTheme.colorScheme.outlineVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                )
                            }
                        }
                    }

                    // Row of 2 Cards: "Jumlah buku" and "Jumlah berkas" (Screenshot 5)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Left Card: Jumlah buku
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Jumlah buku",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$booksCount",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Right Card: Jumlah berkas
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Jumlah berkas",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$filesCount",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Card 3: "Jenis" (Word Breakdown Bar Chart in Screenshot 5)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Jenis",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                val typesData = listOf(
                                    "Kata-kata" to totalWords.toFloat(),
                                    "CJK" to totalCjkChars.toFloat(),
                                    "Literal" to totalLiteralChars.toFloat(),
                                    "Semua" to totalAllChars.toFloat()
                                )

                                WordTypesBarChart(
                                    bars = typesData,
                                    primaryColor = MaterialTheme.colorScheme.primary,
                                    secondaryColor = MaterialTheme.colorScheme.secondary,
                                    gridColor = MaterialTheme.colorScheme.outlineVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Tab 2: Distribusi & Target Harian
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Target Menulis Harian",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val todayWritten = logs.firstOrNull()?.wordsWritten ?: 0
                                val targetInt = preferences.dailyWordTarget.coerceAtLeast(1)
                                val progressRatio = (todayWritten.toFloat() / targetInt.toFloat()).coerceIn(0f, 1f)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tercapai Hari Ini: $todayWritten kata",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(progressRatio * 100).toInt()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Atur Sasaran: ${targetSliderValue.toInt()} kata/hari",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Slider(
                                    value = targetSliderValue,
                                    onValueChange = { targetSliderValue = it },
                                    onValueChangeFinished = {
                                        viewModel.updateDailyWordTarget(targetSliderValue.toInt())
                                    },
                                    valueRange = 100f..10000f,
                                    steps = 98,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    // Distribusi Status Buku
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Distribusi Status Naskah",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val draftCount = books.count { it.status == "DRAFT" }
                                val revisiCount = books.count { it.status == "REVISI" }
                                val selesaiCount = books.count { it.status == "SELESAI" }
                                val ideCount = books.count { it.status == "IDE" }

                                DistributionRow("Sedang Ditulis (Draft)", draftCount, books.size, Color(0xFF60A5FA))
                                DistributionRow("Revisi & Penyuntingan", revisiCount, books.size, Color(0xFFFBBF24))
                                DistributionRow("Selesai & Tamat", selesaiCount, books.size, Color(0xFF34D399))
                                DistributionRow("Konsep & Ide", ideCount, books.size, Color(0xFF818CF8))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DistributionRow(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "$count buku", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

private fun formatCompactAxisValue(value: Float): String {
    return when {
        value >= 1_000_000f -> String.format(Locale.US, "%.1fM", value / 1_000_000f).replace(".0M", "M")
        value >= 1_000f -> String.format(Locale.US, "%.1fK", value / 1_000f).replace(".0K", "K")
        else -> value.toInt().toString()
    }
}

/**
 * Spline Area Chart matching Screenshot 5 with Y-axis & X-axis labels and vertical dashed marker
 */
@Composable
fun TrendSplineAreaChart(
    pointsData: List<Pair<String, Float>>,
    accentColor: Color,
    gridColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    val safeData = if (pointsData.size >= 2) pointsData else listOf("Min" to 0f, "Sen" to 0f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val paddingLeft = 44.dp.toPx()
        val paddingRight = 10.dp.toPx()
        val paddingTop = 12.dp.toPx()
        val paddingBottom = 26.dp.toPx()
        val graphW = (w - paddingLeft - paddingRight).coerceAtLeast(10f)
        val graphH = (h - paddingTop - paddingBottom).coerceAtLeast(10f)

        val maxRaw = safeData.maxOf { it.second }.coerceAtLeast(10f)
        val yMax = maxRaw * 1.15f

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = labelColor.toArgb()
            textSize = 10.sp.toPx()
            typeface = Typeface.DEFAULT
        }

        // Draw 5 horizontal grid lines + Y-axis labels
        for (step in 0..4) {
            val fraction = step / 4f
            val y = paddingTop + graphH - (fraction * graphH)
            drawLine(
                color = gridColor.copy(alpha = 0.7f),
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + graphW, y),
                strokeWidth = 1f
            )
            val label = formatCompactAxisValue(yMax * fraction)
            textPaint.textAlign = Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText(
                label,
                paddingLeft - 6.dp.toPx(),
                y + 3.5.dp.toPx(),
                textPaint
            )
        }

        val stepX = graphW / (safeData.size - 1)
        val coords = safeData.mapIndexed { idx, (dayLabel, value) ->
            val x = paddingLeft + idx * stepX
            val normalized = (value / yMax).coerceIn(0f, 1f)
            val y = paddingTop + graphH - (normalized * graphH)
            Triple(dayLabel, x, y)
        }

        val path = Path()
        val fillPath = Path()

        val first = coords.first()
        path.moveTo(first.second, first.third)
        fillPath.moveTo(first.second, paddingTop + graphH)
        fillPath.lineTo(first.second, first.third)

        for (i in 1 until coords.size) {
            val prev = coords[i - 1]
            val curr = coords[i]
            val midX = (prev.second + curr.second) / 2f
            path.cubicTo(midX, prev.third, midX, curr.third, curr.second, curr.third)
            fillPath.cubicTo(midX, prev.third, midX, curr.third, curr.second, curr.third)
        }

        val last = coords.last()
        fillPath.lineTo(last.second, paddingTop + graphH)
        fillPath.close()

        // Gradient Fill under Curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.48f),
                    accentColor.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = paddingTop + graphH
            )
        )

        // Stroke line
        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(width = 2.6.dp.toPx())
        )

        // Vertical dashed line at latest day (Screenshot 5)
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(last.second, last.third),
            end = Offset(last.second, paddingTop + graphH),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )

        // Glowing indicator dot on latest day (Screenshot 5)
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = Offset(last.second, last.third)
        )
        drawCircle(
            color = accentColor,
            radius = 3.8.dp.toPx(),
            center = Offset(last.second, last.third)
        )

        // Draw X-axis day labels
        textPaint.textAlign = Paint.Align.CENTER
        coords.forEach { (dayLabel, x, _) ->
            drawContext.canvas.nativeCanvas.drawText(
                dayLabel,
                x,
                paddingTop + graphH + 18.dp.toPx(),
                textPaint
            )
        }
    }
}

/**
 * Word Types Bar Chart matching Screenshot 5 with Y-axis & X-axis category labels
 */
@Composable
fun WordTypesBarChart(
    bars: List<Pair<String, Float>>,
    primaryColor: Color,
    secondaryColor: Color,
    gridColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val paddingLeft = 44.dp.toPx()
        val paddingRight = 10.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val paddingBottom = 34.dp.toPx()
        val graphW = (w - paddingLeft - paddingRight).coerceAtLeast(10f)
        val graphH = (h - paddingTop - paddingBottom).coerceAtLeast(10f)

        val maxVal = (bars.maxOfOrNull { it.second } ?: 10f).coerceAtLeast(10f) * 1.15f

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = labelColor.toArgb()
            textSize = 10.sp.toPx()
        }

        // Draw 5 horizontal grid lines + Y-axis labels
        for (step in 0..4) {
            val fraction = step / 4f
            val y = paddingTop + graphH - (fraction * graphH)
            drawLine(
                color = gridColor.copy(alpha = 0.7f),
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + graphW, y),
                strokeWidth = 1f
            )
            val label = formatCompactAxisValue(maxVal * fraction)
            textPaint.textAlign = Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText(
                label,
                paddingLeft - 6.dp.toPx(),
                y + 3.5.dp.toPx(),
                textPaint
            )
        }

        val slotWidth = graphW / bars.size.coerceAtLeast(1)
        val barWidth = slotWidth * 0.44f

        bars.forEachIndexed { index, (catLabel, value) ->
            val fraction = (value / maxVal).coerceIn(0f, 1f)
            val barHeight = (fraction * graphH).coerceAtLeast(if (value > 0f) 4.dp.toPx() else 0f)
            val x = paddingLeft + index * slotWidth + (slotWidth - barWidth) / 2f
            val y = paddingTop + graphH - barHeight

            if (barHeight > 0f) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, secondaryColor),
                        startY = y,
                        endY = paddingTop + graphH
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Category label on X-axis (slightly rotated like Screenshot 5)
            val labelX = paddingLeft + index * slotWidth + slotWidth / 2f
            val labelY = paddingTop + graphH + 18.dp.toPx()
            textPaint.textAlign = Paint.Align.CENTER
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(15f, labelX, labelY)
            drawContext.canvas.nativeCanvas.drawText(catLabel, labelX, labelY, textPaint)
            drawContext.canvas.nativeCanvas.restore()
        }
    }
}
