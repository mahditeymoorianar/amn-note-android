package com.teymoorianar.amnnote.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection as ComposeTextDirection
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teymoorianar.amnnote.domain.model.TextDirection
import com.teymoorianar.amnnote.domain.model.TextParser
import com.teymoorianar.amnnote.domain.model.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormattedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: ComposeTextStyle = ComposeTextStyle.Default,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
    activeMarkerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    inactiveMarkerColor: Color = Color.Transparent,
) {
    val typography = MaterialTheme.typography
    val baseTextColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val parseResult = remember(value.text) { TextParser.analyze(value.text) }
    val selectionStart = value.selection.min
    val activeBlockIndex = remember(parseResult, selectionStart) {
        findActiveBlock(parseResult, selectionStart)
    }

    val visualTransformation = remember(
        parseResult,
        activeBlockIndex,
        typography,
        activeMarkerColor,
    ) {
        MarkerVisualTransformation(
            parsedBlocks = parseResult,
            activeBlockIndex = activeBlockIndex,
            typography = typography,
            activeMarkerColor = activeMarkerColor,
            linkColor = Color(0xFFEA10FF),
        )
    }

    // Trim Android font padding globally at the text level (correct place)
    val mergedTextStyle = textStyle.merge(
        ComposeTextStyle(
            color = baseTextColor,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = cursorBrush,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        maxLines = if (singleLine) 1 else maxLines,
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value.text,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                isError = isError,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                supportingText = supportingText,
                prefix = null,
                suffix = null,
                shape = shape,
                colors = colors,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    )
}

private fun findActiveBlock(
    blocks: List<TextParser.ParsedBlock>,
    position: Int,
): Int {
    if (blocks.isEmpty() || position < 0) return -1

    blocks.forEachIndexed { index, block ->
        val ranges = buildList {
            block.contentRange?.let { add(it) }
            addAll(block.markers)
        }
        if (ranges.any { position in it }) {
            return index
        }
    }
    return -1
}

private fun buildDisplayAnnotatedString(
    text: String,
    parsedBlocks: List<TextParser.ParsedBlock>,
    activeBlockIndex: Int,
    typography: androidx.compose.material3.Typography,
    activeMarkerColor: Color,
    inactiveMarkerColor: Color,
    linkColor: Color,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(text)

    parsedBlocks.forEachIndexed { index, parsed ->
        val block = parsed.block
        val contentRange = parsed.contentRange

        if (contentRange != null && contentRange.first <= contentRange.last) {
            val baseStyle = spanStyleFor(block.style, typography, linkColor)
            if (baseStyle != null) {
                builder.addStyle(baseStyle, contentRange.first, contentRange.last + 1)
            }

            val inlineStyle = SpanStyle(
                fontWeight = if (block.bold) FontWeight.Bold else null,
                fontStyle = if (block.italic) FontStyle.Italic else null,
            )
            if (inlineStyle != SpanStyle()) {
                builder.addStyle(inlineStyle, contentRange.first, contentRange.last + 1)
            }

            val paragraphDirection = when (block.direction) {
                TextDirection.LTR -> ComposeTextDirection.Ltr
                TextDirection.RTL -> ComposeTextDirection.Rtl
                TextDirection.NULL -> null
            }
            if (paragraphDirection != null) {
                builder.addStyle(
                    ParagraphStyle(textDirection = paragraphDirection),
                    contentRange.first,
                    contentRange.last + 1,
                )
            }

            if (block.style == TextStyle.LIST_ITEM) {
                // Compact list paragraph: indent + smaller lineHeight + trimmed leading
                builder.addStyle(
                    ParagraphStyle(
                        textIndent = TextIndent(
                            firstLine = 16.sp,
                            restLine = 16.sp,
                        ),
                        lineHeight = 18.sp,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Proportional,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    contentRange.first,
                    contentRange.last + 1,
                )
            }
        }

        val markerColor =
            if (index == activeBlockIndex) activeMarkerColor else inactiveMarkerColor

        parsed.markers.forEach { range ->
            if (range.first <= range.last && range.last < text.length) {
                builder.addStyle(
                    SpanStyle(color = markerColor),
                    range.first,
                    range.last + 1,
                )
            }
        }
    }

    return builder.toAnnotatedString()
}

private fun spanStyleFor(
    style: TextStyle,
    typography: androidx.compose.material3.Typography,
    linkColor: Color,
): SpanStyle? = when (style) {
    TextStyle.BODY -> typography.bodyLarge.toSpanStyle()
    TextStyle.LIST_ITEM -> typography.bodyLarge.toSpanStyle() // no lineHeight here
    TextStyle.POWER -> typography.titleMedium.toSpanStyle()
    TextStyle.SUBTITLE -> typography.titleSmall.toSpanStyle()
    TextStyle.LINK -> typography.bodyLarge.toSpanStyle().copy(color = linkColor)
    TextStyle.HEADING_1 -> typography.headlineLarge.toSpanStyle()
    TextStyle.HEADING_2 -> typography.headlineMedium.toSpanStyle()
    TextStyle.HEADING_3 -> typography.headlineSmall.toSpanStyle()
    TextStyle.HEADING_4 -> typography.titleLarge.toSpanStyle()
    TextStyle.HEADING_5 -> typography.titleMedium.toSpanStyle()
}

class MarkerVisualTransformation(
    private val parsedBlocks: List<TextParser.ParsedBlock>,
    private val activeBlockIndex: Int,
    private val typography: Typography,
    private val activeMarkerColor: Color,
    private val linkColor: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val src = text.text
        val n = src.length

        // 1) Mark characters that belong to markers of NON-active blocks (to be hidden)
        val hide = BooleanArray(n)
        parsedBlocks.forEachIndexed { idx, parsed ->
            if (idx != activeBlockIndex) {
                parsed.markers.forEach { range ->
                    val start = range.first.coerceAtLeast(0)
                    val end = (range.last + 1).coerceAtMost(n)
                    for (i in start until end) hide[i] = true
                }
            }
        }

        // 2) Build transformed text and original -> transformed offset map
        val originalToTransformed = IntArray(n + 1)
        val builder = AnnotatedString.Builder()
        var destIndex = 0

        for (i in 0 until n) {
            originalToTransformed[i] = destIndex
            if (!hide[i]) {
                builder.append(src[i])
                destIndex++
            }
        }
        originalToTransformed[n] = destIndex
        val destLen = destIndex

        // 3) Add styles (mapped indices)
        parsedBlocks.forEachIndexed { idx, parsed ->
            val block = parsed.block
            val contentRange = parsed.contentRange

            if (contentRange != null && contentRange.first <= contentRange.last) {
                val start = originalToTransformed[contentRange.first]
                val end = originalToTransformed[contentRange.last + 1]
                if (start < end) {
                    val baseStyle = spanStyleFor(block.style, typography, linkColor)
                    if (baseStyle != null) builder.addStyle(baseStyle, start, end)

                    val inlineStyle = SpanStyle(
                        fontWeight = if (block.bold) FontWeight.Bold else null,
                        fontStyle = if (block.italic) FontStyle.Italic else null,
                    )
                    if (inlineStyle != SpanStyle()) builder.addStyle(inlineStyle, start, end)

                    val paragraphDirection = when (block.direction) {
                        TextDirection.LTR -> ComposeTextDirection.Ltr
                        TextDirection.RTL -> ComposeTextDirection.Rtl
                        TextDirection.NULL -> null
                    }
                    if (paragraphDirection != null) {
                        builder.addStyle(
                            ParagraphStyle(textDirection = paragraphDirection),
                            start,
                            end,
                        )
                    }

                    if (block.style == TextStyle.LIST_ITEM) {
                        // Match compact list paragraph here too
                        builder.addStyle(
                            ParagraphStyle(
                                textIndent = TextIndent(
                                    firstLine = 16.sp,
                                    restLine = 16.sp,
                                ),
                                lineHeight = 18.sp,
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Proportional,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            ),
                            start,
                            end,
                        )
                    }
                }
            }

            // Markers: only ACTIVE block markers remain in transformed text
            if (idx == activeBlockIndex) {
                parsed.markers.forEach { range ->
                    val s0 = range.first.coerceAtLeast(0)
                    val e0 = (range.last + 1).coerceAtMost(n)
                    val start = originalToTransformed[s0]
                    val end = originalToTransformed[e0]
                    if (start < end) {
                        builder.addStyle(
                            SpanStyle(color = activeMarkerColor),
                            start,
                            end,
                        )
                    }
                }
            }
        }

        // 4) Build transformed -> original offset map
        val transformedToOriginal = IntArray(destLen + 1)
        var i = 0
        for (t in 0..destLen) {
            while (i < n && originalToTransformed[i] < t) i++
            transformedToOriginal[t] = i
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, n)
                return originalToTransformed[o]
            }
            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, destLen)
                return transformedToOriginal[t]
            }
        }

        return TransformedText(builder.toAnnotatedString(), offsetMapping)
    }
}
