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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDirection as ComposeTextDirection
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
    val textColor = colors.textColor(
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
    ).value

    val parseResult = remember(value.text) { TextParser.analyze(value.text) }
    val selectionStart = value.selection.min
    val activeBlockIndex = remember(parseResult, selectionStart) {
        findActiveBlock(parseResult, selectionStart)
    }

    val annotatedString = remember(
        value.text,
        parseResult,
        activeBlockIndex,
        typography,
        activeMarkerColor,
        inactiveMarkerColor,
    ) {
        buildDisplayAnnotatedString(
            text = value.text,
            parsedBlocks = parseResult,
            activeBlockIndex = activeBlockIndex,
            typography = typography,
            activeMarkerColor = activeMarkerColor,
            inactiveMarkerColor = inactiveMarkerColor,
        )
    }

    val mergedTextStyle = textStyle.merge(ComposeTextStyle(color = textColor))

    BasicTextField(
        value = TextFieldValue(
            annotatedString = annotatedString,
            selection = value.selection,
            composition = value.composition,
        ),
        onValueChange = { newValue ->
            onValueChange(
                TextFieldValue(
                    text = newValue.text,
                    selection = newValue.selection,
                    composition = newValue.composition,
                )
            )
        },
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
        decorationBox = { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value.text,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
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

@Composable
private fun buildDisplayAnnotatedString(
    text: String,
    parsedBlocks: List<TextParser.ParsedBlock>,
    activeBlockIndex: Int,
    typography: androidx.compose.material3.Typography,
    activeMarkerColor: Color,
    inactiveMarkerColor: Color,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    builder.append(text)

    parsedBlocks.forEachIndexed { index, parsed ->
        val block = parsed.block
        val contentRange = parsed.contentRange

        if (contentRange != null && contentRange.first <= contentRange.last) {
            val baseStyle = spanStyleFor(block.style, typography)
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

            paragraphDirection?.let {
                builder.addParagraphStyle(
                    ParagraphStyle(textDirection = it),
                    contentRange.first,
                    contentRange.last + 1,
                )
            }

            if (block.style == TextStyle.LIST_ITEM) {
                builder.addParagraphStyle(
                    ParagraphStyle(textIndent = TextIndent(firstLine = 16.sp, restLine = 16.sp)),
                    contentRange.first,
                    contentRange.last + 1,
                )
            }
        }

        val markerColor = if (index == activeBlockIndex) activeMarkerColor else inactiveMarkerColor
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

@Composable
private fun spanStyleFor(
    style: TextStyle,
    typography: androidx.compose.material3.Typography,
): SpanStyle? = when (style) {
    TextStyle.BODY, TextStyle.LIST_ITEM -> typography.bodyLarge.toSpanStyle()
    TextStyle.POWER -> typography.titleMedium.toSpanStyle()
    TextStyle.SUBTITLE -> typography.titleSmall.toSpanStyle()
    TextStyle.LINK -> typography.bodyLarge.toSpanStyle().copy(color = MaterialTheme.colorScheme.primary)
    TextStyle.HEADING_1 -> typography.headlineLarge.toSpanStyle()
    TextStyle.HEADING_2 -> typography.headlineMedium.toSpanStyle()
    TextStyle.HEADING_3 -> typography.headlineSmall.toSpanStyle()
    TextStyle.HEADING_4 -> typography.titleLarge.toSpanStyle()
    TextStyle.HEADING_5 -> typography.titleMedium.toSpanStyle()
}
