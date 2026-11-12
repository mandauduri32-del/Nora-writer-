package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyWritingLogEntity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StatsBarChart(
    logs: List<DailyWritingLogEntity>,
    targetWords: Int = 1000,
    modifier: Modifier = Modifier
) {
    val maxCount = (logs.maxOfOrNull { it.wordsWritten } ?: targetWords).coerceAtLeast(targetWords).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas Menulis 7 Hari Terakhir",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Target: $targetWords kata/hari",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bars Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val parseSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayNameSdf = SimpleDateFormat("EEE", Locale("id", "ID"))

            // Render bars from past to present
            val displayLogs = logs.reversed()

            displayLogs.forEach { log ->
                val dayLabel = try {
                    val date = parseSdf.parse(log.dateStr)
                    if (date != null) dayNameSdf.format(date).replace(".", "") else log.dateStr.takeLast(2)
                } catch (e: Exception) {
                    log.dateStr.takeLast(2)
                }

                val fraction = (log.wordsWritten.toFloat() / maxCount.toFloat()).coerceIn(0.04f, 1f)
                val animatedHeight by animateFloatAsState(targetValue = fraction, label = "bar_height")

                val isGoalMet = log.wordsWritten >= targetWords
                val barColor = if (isGoalMet) Color(0xFF10B981) else MaterialTheme.colorScheme.primary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Word count above bar
                    Text(
                        text = if (log.wordsWritten > 0) "${log.wordsWritten}" else "0",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(barColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Day of week label
                    Text(
                        text = dayLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
