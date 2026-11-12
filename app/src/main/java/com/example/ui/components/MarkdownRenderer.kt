package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent

@Composable
fun MarkdownRenderer(
    content: String,
    fontSize: Float = 17f,
    lineSpacing: Float = 1.5f,
    fontFamily: FontFamily = FontFamily.Serif,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    val lines = content.split("\n")

    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### "),
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSize * 1.25f).sp,
                        lineHeight = (fontSize * 1.25f * lineSpacing).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## "),
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSize * 1.4f).sp,
                        lineHeight = (fontSize * 1.4f * lineSpacing).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# "),
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSize * 1.6f).sp,
                        lineHeight = (fontSize * 1.6f * lineSpacing).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                    )
                }
                trimmed.startsWith("> ") -> {
                    // Blockquote
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GoldAccent)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.removePrefix("> ")),
                            fontFamily = fontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * lineSpacing).sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = "•",
                            fontSize = fontSize.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(2)),
                            fontFamily = fontFamily,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * lineSpacing).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val prefix = trimmed.substringBefore(" ")
                    val text = trimmed.substringAfter(" ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = prefix,
                            fontSize = fontSize.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(text),
                            fontFamily = fontFamily,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * lineSpacing).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                trimmed == "---" || trimmed == "***" -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(trimmed),
                        fontFamily = fontFamily,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * lineSpacing).sp,
                        textAlign = textAlign,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            when {
                // Bold: **text**
                text.startsWith("**", cursor) -> {
                    val end = text.indexOf("**", cursor + 2)
                    if (end != -1) {
                        val boldText = text.substring(cursor + 2, end)
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(boldText)
                        pop()
                        cursor = end + 2
                    } else {
                        append(text[cursor])
                        cursor++
                    }
                }
                // Italic: *text*
                text.startsWith("*", cursor) -> {
                    val end = text.indexOf("*", cursor + 1)
                    if (end != -1) {
                        val italicText = text.substring(cursor + 1, end)
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(italicText)
                        pop()
                        cursor = end + 1
                    } else {
                        append(text[cursor])
                        cursor++
                    }
                }
                // Strikethrough: ~~text~~
                text.startsWith("~~", cursor) -> {
                    val end = text.indexOf("~~", cursor + 2)
                    if (end != -1) {
                        val strikeText = text.substring(cursor + 2, end)
                        pushStyle(SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                        append(strikeText)
                        pop()
                        cursor = end + 2
                    } else {
                        append(text[cursor])
                        cursor++
                    }
                }
                // Underline: <u>text</u>
                text.startsWith("<u>", cursor) -> {
                    val end = text.indexOf("</u>", cursor + 3)
                    if (end != -1) {
                        val uText = text.substring(cursor + 3, end)
                        pushStyle(SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline))
                        append(uText)
                        pop()
                        cursor = end + 4
                    } else {
                        append(text[cursor])
                        cursor++
                    }
                }
                else -> {
                    append(text[cursor])
                    cursor++
                }
            }
        }
    }
}
