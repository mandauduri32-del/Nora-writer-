package com.example.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditorToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onHeading: (Int) -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrikethrough: () -> Unit,
    onQuote: () -> Unit,
    onBulletList: () -> Unit,
    onNumberList: () -> Unit,
    onDivider: () -> Unit,
    onMoveParaUp: () -> Unit,
    onMoveParaDown: () -> Unit,
    onDuplicatePara: () -> Unit,
    onDeletePara: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHeadingMenu by remember { mutableStateOf(false) }
    var showParagraphMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Undo & Redo
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(36.dp).testTag("editor_undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(36.dp).testTag("editor_redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            // Divider vertical
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )

            // Headings Menu
            Box {
                IconButton(
                    onClick = { showHeadingMenu = true },
                    modifier = Modifier.size(36.dp).testTag("editor_heading_button")
                ) {
                    Icon(imageVector = Icons.Default.Title, contentDescription = "Heading", tint = MaterialTheme.colorScheme.onSurface)
                }

                DropdownMenu(
                    expanded = showHeadingMenu,
                    onDismissRequest = { showHeadingMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Heading 1 (# )", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        onClick = {
                            showHeadingMenu = false
                            onHeading(1)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Heading 2 (## )", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                        onClick = {
                            showHeadingMenu = false
                            onHeading(2)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Heading 3 (### )", fontSize = 13.sp) },
                        onClick = {
                            showHeadingMenu = false
                            onHeading(3)
                        }
                    )
                }
            }

            // Bold
            ToolbarIconButton(
                icon = Icons.Default.FormatBold,
                description = "Tebal",
                testTag = "editor_bold_button",
                onClick = onBold
            )

            // Italic
            ToolbarIconButton(
                icon = Icons.Default.FormatItalic,
                description = "Miring",
                testTag = "editor_italic_button",
                onClick = onItalic
            )

            // Underline
            ToolbarIconButton(
                icon = Icons.Default.FormatUnderlined,
                description = "Garis Bawah",
                testTag = "editor_underline_button",
                onClick = onUnderline
            )

            // Strikethrough
            ToolbarIconButton(
                icon = Icons.Default.FormatStrikethrough,
                description = "Coret",
                testTag = "editor_strike_button",
                onClick = onStrikethrough
            )

            // Quote
            ToolbarIconButton(
                icon = Icons.Default.FormatQuote,
                description = "Kutipan",
                testTag = "editor_quote_button",
                onClick = onQuote
            )

            // Bullet List
            ToolbarIconButton(
                icon = Icons.Default.FormatListBulleted,
                description = "Daftar Berpoin",
                testTag = "editor_bullet_button",
                onClick = onBulletList
            )

            // Numbered List
            ToolbarIconButton(
                icon = Icons.Default.FormatListNumbered,
                description = "Daftar Bernomor",
                testTag = "editor_number_button",
                onClick = onNumberList
            )

            // Horizontal Line
            ToolbarIconButton(
                icon = Icons.Default.HorizontalRule,
                description = "Garis Pemisah",
                testTag = "editor_divider_button",
                onClick = onDivider
            )

            // Divider vertical
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 24.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )

            // Paragraph Actions Menu
            Box {
                IconButton(
                    onClick = { showParagraphMenu = true },
                    modifier = Modifier.size(36.dp).testTag("editor_para_menu_button")
                ) {
                    Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "Tindakan Paragraf", tint = MaterialTheme.colorScheme.onSurface)
                }

                DropdownMenu(
                    expanded = showParagraphMenu,
                    onDismissRequest = { showParagraphMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Pindahkan Paragraf ke Atas") },
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                        onClick = {
                            showParagraphMenu = false
                            onMoveParaUp()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Pindahkan Paragraf ke Bawah") },
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                        onClick = {
                            showParagraphMenu = false
                            onMoveParaDown()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplikasi Paragraf") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showParagraphMenu = false
                            onDuplicatePara()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Paragraf Ini", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showParagraphMenu = false
                            onDeletePara()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    description: String,
    testTag: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp).testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}
