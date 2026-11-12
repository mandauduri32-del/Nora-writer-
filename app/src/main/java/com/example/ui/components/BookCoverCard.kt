package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BookEntity
import com.example.ui.theme.GoldAccent
import java.io.File

/**
 * Signature Lite Writer / Novel Desk default book cover with stylized quill feather silhouette
 */
@Composable
fun QuillFeatherGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark layered background gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF221A30), Color(0xFF150F22))
            )
        )

        // Subtle geometric background facet lines (matching Screenshot 2)
        val facet1 = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, h * 0.42f)
            lineTo(w, h)
            lineTo(0f, h * 0.65f)
            close()
        }
        drawPath(facet1, color = Color(0xFF2A203C).copy(alpha = 0.55f))

        val facet2 = Path().apply {
            moveTo(w * 0.2f, 0f)
            lineTo(w, h * 0.25f)
            lineTo(w * 0.7f, h)
            lineTo(0f, h * 0.85f)
            close()
        }
        drawPath(facet2, color = Color(0xFF1E162B).copy(alpha = 0.45f))

        // Quill feather silhouette
        val featherColor = Color(0xFF4C415C)
        val featherHighlight = Color(0xFF5D5070)
        val spineColor = Color(0xFF6B5D82)

        // Central spine (curved arc from bottom-left towards top-right)
        val spine = Path().apply {
            moveTo(w * 0.36f, h * 0.82f)
            quadraticTo(w * 0.48f, h * 0.50f, w * 0.72f, h * 0.18f)
        }
        drawPath(spine, color = spineColor, style = Stroke(width = 3.5f))

        // Left feather vanes (broad curved wing with diagonal slits)
        val leftVane = Path().apply {
            moveTo(w * 0.72f, h * 0.18f)
            cubicTo(w * 0.48f, h * 0.22f, w * 0.26f, h * 0.38f, w * 0.24f, h * 0.64f)
            lineTo(w * 0.38f, h * 0.72f)
            quadraticTo(w * 0.46f, h * 0.50f, w * 0.72f, h * 0.18f)
            close()
        }
        drawPath(leftVane, color = featherColor)

        // Right feather vanes
        val rightVane = Path().apply {
            moveTo(w * 0.72f, h * 0.18f)
            cubicTo(w * 0.78f, h * 0.30f, w * 0.75f, h * 0.52f, w * 0.56f, h * 0.66f)
            lineTo(w * 0.44f, h * 0.64f)
            quadraticTo(w * 0.56f, h * 0.44f, w * 0.72f, h * 0.18f)
            close()
        }
        drawPath(rightVane, color = featherHighlight)

        // Diagonal slits cutting through the vanes
        val slitColor = Color(0xFF181224)
        for (i in 1..4) {
            val t = i / 5.2f
            val slitX = w * (0.32f + t * 0.34f)
            val slitY = h * (0.64f - t * 0.38f)
            drawLine(
                color = slitColor,
                start = Offset(slitX, slitY),
                end = Offset(slitX - w * 0.08f, slitY + h * 0.045f),
                strokeWidth = 2.8f
            )
        }
    }
}

/**
 * 3-Column Grid Book Item matching Screenshot 2
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookGridItem(
    book: BookEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    onExport: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasCustomCover = book.coverUri.isNotBlank() && File(book.coverUri).exists()
    var showContextMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showContextMenu = true }
                )
                .testTag("book_grid_item_${book.id}"),
            horizontalAlignment = Alignment.Start
        ) {
            // Book Cover Card (Ratio ~2:3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1326))
            ) {
                if (hasCustomCover) {
                    AsyncImage(
                        model = File(book.coverUri),
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    QuillFeatherGraphic(modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title below cover in 1 line with ellipsis
            Text(
                text = book.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Buka Naskah") },
                leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                onClick = { showContextMenu = false; onClick() }
            )
            DropdownMenuItem(
                text = { Text(if (book.isFavorite) "Hapus dari Favorit" else "Tandai Favorit") },
                leadingIcon = {
                    Icon(
                        if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null
                    )
                },
                onClick = { showContextMenu = false; onToggleFavorite() }
            )
            DropdownMenuItem(
                text = { Text("Bagikan / Ekspor") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = { showContextMenu = false; onExport() }
            )
            DropdownMenuItem(
                text = { Text("Pindahkan ke Tempat Sampah") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = { showContextMenu = false; onDelete() }
            )
        }
    }
}

@Composable
fun BookCoverCard(
    book: BookEntity,
    totalWords: Int,
    chapterCount: Int,
    isGridView: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val hasCustomCover = book.coverUri.isNotBlank() && File(book.coverUri).exists()

    val fallbackColor = try {
        Color(android.graphics.Color.parseColor(book.coverColorHex))
    } catch (e: Exception) {
        Color(0xFF1E293B)
    }

    val statusText = when (book.status) {
        "IDE" -> "Ide Cerita"
        "REVISI" -> "Revisi"
        "SELESAI" -> "Selesai"
        else -> "Sedang Ditulis"
    }

    val statusColor = when (book.status) {
        "IDE" -> Color(0xFF818CF8)
        "REVISI" -> Color(0xFFFBBF24)
        "SELESAI" -> Color(0xFF34D399)
        else -> Color(0xFF60A5FA)
    }

    if (isGridView) {
        // Grid Card with Portrait Book Ratio
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .testTag("book_card_${book.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Book Cover
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(fallbackColor)
                ) {
                    if (hasCustomCover) {
                        AsyncImage(
                            model = File(book.coverUri),
                            contentDescription = book.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Protective gradient at bottom of cover
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.2f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                    } else {
                        // Artistic Graphic Cover
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(fallbackColor.copy(alpha = 0.85f), fallbackColor)
                                    )
                                )
                        )
                    }

                    // Simulated Book Spine (Left 5dp line)
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.25f))
                            .align(Alignment.CenterStart)
                    )

                    // Pin Badge
                    if (book.isPinned) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Disematkan",
                            tint = GoldAccent,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.TopStart)
                                .padding(start = 7.dp, top = 6.dp)
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (book.isFavorite) Color(0xFFEF4444) else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Title & Author on Cover
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = book.title,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = book.author,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Info below cover
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = book.genre,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (chapterCount > 0) "$chapterCount bab" else "Naskah",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (totalWords > 0) {
                            WordCountBadge(count = totalWords)
                        }
                    }
                }
            }
        }
    } else {
        // List Card
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .testTag("book_card_${book.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Miniature Book Cover
                Box(
                    modifier = Modifier
                        .size(width = 50.dp, height = 72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(fallbackColor)
                ) {
                    if (hasCustomCover) {
                        AsyncImage(
                            model = File(book.coverUri),
                            contentDescription = book.title,
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
                                )
                        )
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp).align(Alignment.Center)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.35f))
                            .align(Alignment.CenterStart)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Metadata Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (book.isPinned) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Disematkan",
                                tint = GoldAccent,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = book.title,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${book.author} • ${book.genre}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                        if (chapterCount > 0) {
                            Text(
                                text = "$chapterCount bab",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (totalWords > 0) {
                            WordCountBadge(count = totalWords)
                        }
                    }
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi Buku",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (book.isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit") },
                            onClick = {
                                showMenu = false
                                onToggleFavorite()
                            },
                            leadingIcon = {
                                Icon(
                                    if (book.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ubah Rincian & Sampul") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Ekspor Naskah Buku") },
                            onClick = {
                                showMenu = false
                                onExport()
                            },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Pindahkan ke Tempat Sampah", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}
