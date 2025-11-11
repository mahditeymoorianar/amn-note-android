package com.teymoorianar.amnnote.ui.note

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit = {},
    onTextTap: () -> Unit = {}
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val inlineStyles = remember(linkColor) {
        InlineStyles(
            bold = SpanStyle(fontWeight = FontWeight.Bold),
            italic = SpanStyle(fontStyle = FontStyle.Italic),
            link = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
            image = SpanStyle(fontStyle = FontStyle.Italic, color = linkColor)
        )
    }

    val blocks = remember(text, inlineStyles) {
        parseMarkdown(text, inlineStyles)
    }

    val textColor = MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val headingStyle = when (block.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.headlineSmall
                        3 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    }
                    Text(
                        text = block.text,
                        modifier = Modifier.clickable { onTextTap() },
                        style = headingStyle,
                        color = textColor
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    ClickableText(
                        modifier = Modifier.fillMaxWidth(),
                        text = block.text,
                        style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
                    ) { offset ->
                        val url = block.text.getStringAnnotations("URL", offset, offset)
                            .firstOrNull()?.item
                        val image = block.text.getStringAnnotations("IMAGE", offset, offset)
                            .firstOrNull()?.item
                        when {
                            url != null -> onLinkClick(url)
                            image != null -> onLinkClick(image)
                            else -> onTextTap()
                        }
                    }
                }

                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "\u2022",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ClickableText(
                            modifier = Modifier.weight(1f),
                            text = block.text,
                            style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
                        ) { offset ->
                            val url = block.text.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.item
                            val image = block.text.getStringAnnotations("IMAGE", offset, offset)
                                .firstOrNull()?.item
                            when {
                                url != null -> onLinkClick(url)
                                image != null -> onLinkClick(image)
                                else -> onTextTap()
                            }
                        }
                    }
                }

                is MarkdownBlock.Image -> {
                    MarkdownImage(block, onLinkClick)
                }
            }
        }
    }
}

@Composable
private fun MarkdownImage(block: MarkdownBlock.Image, onLinkClick: (String) -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(block.url)
                .crossfade(true)
                .build(),
            contentDescription = block.alt,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onLinkClick(block.url) },
            contentScale = ContentScale.FillWidth
        )
        if (!block.alt.isNullOrBlank()) {
            Text(
                text = block.alt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ClickableText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = AnnotatedString(block.url),
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
        ) {
            onLinkClick(block.url)
        }
    }
}

private data class InlineStyles(
    val bold: SpanStyle,
    val italic: SpanStyle,
    val link: SpanStyle,
    val image: SpanStyle
)

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: AnnotatedString) : MarkdownBlock()
    data class Paragraph(val text: AnnotatedString) : MarkdownBlock()
    data class Bullet(val text: AnnotatedString) : MarkdownBlock()
    data class Image(val url: String, val alt: String?) : MarkdownBlock()
}

private fun parseMarkdown(
    rawText: String,
    inlineStyles: InlineStyles
): List<MarkdownBlock> {
    if (rawText.isBlank()) return emptyList()

    val normalized = rawText.replace("\r\n", "\n")
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraphBuilder = StringBuilder()

    fun flushParagraph() {
        if (paragraphBuilder.isNotEmpty()) {
            val text = paragraphBuilder.toString().trimEnd()
            if (text.isNotEmpty()) {
                blocks.add(
                    MarkdownBlock.Paragraph(buildAnnotatedString {
                        appendMarkdownInline(text, inlineStyles)
                    })
                )
            }
            paragraphBuilder.clear()
        }
    }

    normalized.split('\n').forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> {
                flushParagraph()
            }

            isImageLine(trimmed) -> {
                flushParagraph()
                parseImageLine(trimmed)?.let { blocks.add(it) }
            }

            isHeading(trimmed) -> {
                flushParagraph()
                val level = trimmed.takeWhile { it == '#' }.length.coerceAtMost(6)
                val content = trimmed.drop(level).trimStart()
                val text = buildAnnotatedString {
                    appendMarkdownInline(content, inlineStyles)
                }
                blocks.add(MarkdownBlock.Heading(level, text))
            }

            isBullet(trimmed) -> {
                flushParagraph()
                val content = trimmed.drop(2).trimStart()
                val text = buildAnnotatedString {
                    appendMarkdownInline(content, inlineStyles)
                }
                blocks.add(MarkdownBlock.Bullet(text))
            }

            else -> {
                if (paragraphBuilder.isNotEmpty()) {
                    paragraphBuilder.append('\n')
                }
                paragraphBuilder.append(line)
            }
        }
    }

    flushParagraph()
    return blocks
}

private fun AnnotatedString.Builder.appendMarkdownInline(
    text: String,
    inlineStyles: InlineStyles
) {
    var index = 0
    while (index < text.length) {
        when {
            text.startsWith("![", index) -> {
                val closing = text.indexOf(']', index + 2)
                val openParen = if (closing != -1) text.indexOf('(', closing) else -1
                val closingParen = if (openParen != -1) text.indexOf(')', openParen) else -1
                if (closing != -1 && openParen == closing + 1 && closingParen != -1) {
                    val alt = text.substring(index + 2, closing)
                    val url = text.substring(openParen + 1, closingParen)
                    val start = length
                    val display = if (alt.isNotBlank()) alt else "Image"
                    append(display)
                    addStyle(inlineStyles.image, start, start + display.length)
                    addStringAnnotation("IMAGE", url, start, start + display.length)
                    index = closingParen + 1
                    continue
                }
            }

            text.startsWith("[", index) -> {
                val closing = text.indexOf(']', index + 1)
                if (closing != -1 && closing + 1 < text.length && text[closing + 1] == '(') {
                    val closingParen = text.indexOf(')', closing + 2)
                    if (closingParen != -1) {
                        val label = text.substring(index + 1, closing)
                        val url = text.substring(closing + 2, closingParen)
                        val start = length
                        appendMarkdownInline(label, inlineStyles)
                        val end = length
                        addStyle(inlineStyles.link, start, end)
                        addStringAnnotation("URL", url, start, end)
                        index = closingParen + 1
                        continue
                    }
                }
            }

            text.startsWith("**", index) || text.startsWith("__", index) -> {
                val delimiter = if (text.startsWith("**", index)) "**" else "__"
                val closing = text.indexOf(delimiter, index + 2)
                if (closing != -1) {
                    val start = length
                    val inner = text.substring(index + 2, closing)
                    appendMarkdownInline(inner, inlineStyles)
                    val end = length
                    addStyle(inlineStyles.bold, start, end)
                    index = closing + 2
                    continue
                }
            }

            text.startsWith("*", index) || text.startsWith("_", index) -> {
                val delimiter = text[index].toString()
                if (!text.startsWith(delimiter.repeat(2), index)) {
                    val closing = text.indexOf(delimiter, index + 1)
                    if (closing != -1) {
                        val start = length
                        val inner = text.substring(index + 1, closing)
                        appendMarkdownInline(inner, inlineStyles)
                        val end = length
                        addStyle(inlineStyles.italic, start, end)
                        index = closing + 1
                        continue
                    }
                }
            }
        }

        append(text[index])
        index++
    }
}

private fun isHeading(line: String): Boolean = line.startsWith("#") && line.takeWhile { it == '#' }.length in 1..6

private fun isBullet(line: String): Boolean =
    (line.startsWith("- ") || line.startsWith("* "))

private fun isImageLine(line: String): Boolean = line.startsWith("![") && line.contains("](")

private fun parseImageLine(line: String): MarkdownBlock.Image? {
    val closing = line.indexOf(']')
    if (closing == -1 || closing + 1 >= line.length || line[closing + 1] != '(') return null
    val closingParen = line.indexOf(')', closing + 2)
    if (closingParen == -1) return null
    val alt = line.substring(2, closing)
    val url = line.substring(closing + 2, closingParen)
    return MarkdownBlock.Image(url = url, alt = alt)
}
*** End of File
