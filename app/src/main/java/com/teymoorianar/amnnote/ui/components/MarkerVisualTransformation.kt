//package com.teymoorianar.amnnote.ui.components
//
//import androidx.compose.material3.Typography
//import androidx.compose.ui.graphics.Color
//import com.teymoorianar.amnnote.domain.model.TextParser
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.ParagraphStyle
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.OffsetMapping
//import androidx.compose.ui.text.input.TransformedText
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextDirection as ComposeTextDirection
//import androidx.compose.ui.text.style.TextIndent
//import androidx.compose.ui.unit.sp
//import com.teymoorianar.amnnote.domain.model.TextDirection
//import com.teymoorianar.amnnote.domain.model.TextStyle
//
//class MarkerVisualTransformation(
//    private val parsedBlocks: List<TextParser.ParsedBlock>,
//    private val activeBlockIndex: Int,
//    private val typography: Typography,
//    private val activeMarkerColor: Color,
//    private val linkColor: Color,
//) : VisualTransformation {
//
//    override fun filter(text: AnnotatedString): TransformedText {
//        val src = text.text
//        val n = src.length
//
//        // 1) Mark characters that belong to markers of NON-active blocks (to be hidden)
//        val hide = BooleanArray(n)
//        parsedBlocks.forEachIndexed { idx, parsed ->
//            if (idx != activeBlockIndex) {
//                parsed.markers.forEach { range ->
//                    val start = range.first.coerceAtLeast(0)
//                    val end = (range.last + 1).coerceAtMost(n)
//                    for (i in start until end) {
//                        hide[i] = true
//                    }
//                }
//            }
//        }
//
//        // 2) Build transformed text and original -> transformed offset map
//        val originalToTransformed = IntArray(n + 1)
//        val builder = AnnotatedString.Builder()
//        var destIndex = 0
//
//        for (i in 0 until n) {
//            originalToTransformed[i] = destIndex
//            if (!hide[i]) {
//                builder.append(src[i])
//                destIndex++
//            }
//        }
//        originalToTransformed[n] = destIndex
//        val destLen = destIndex
//
//        // 3) Add styles (similar to your buildDisplayAnnotatedString but using mapped indices)
//        parsedBlocks.forEachIndexed { idx, parsed ->
//            val block = parsed.block
//            val contentRange = parsed.contentRange
//
//            if (contentRange != null && contentRange.first <= contentRange.last) {
//                val start = originalToTransformed[contentRange.first]
//                val end = originalToTransformed[contentRange.last + 1]
//                if (start < end) {
//                    val baseStyle = spanStyleFor(block.style, typography, linkColor)
//                    if (baseStyle != null) {
//                        builder.addStyle(baseStyle, start, end)
//                    }
//
//                    val inlineStyle = SpanStyle(
//                        fontWeight = if (block.bold) FontWeight.Bold else null,
//                        fontStyle = if (block.italic) FontStyle.Italic else null,
//                    )
//                    if (inlineStyle != SpanStyle()) {
//                        builder.addStyle(inlineStyle, start, end)
//                    }
//
//                    val paragraphDirection = when (block.direction) {
//                        TextDirection.LTR -> ComposeTextDirection.Ltr
//                        TextDirection.RTL -> ComposeTextDirection.Rtl
//                        TextDirection.NULL -> null
//                    }
//                    if (paragraphDirection != null) {
//                        builder.addStyle(
//                            ParagraphStyle(textDirection = paragraphDirection),
//                            start,
//                            end,
//                        )
//                    }
//
//                    if (block.style == TextStyle.LIST_ITEM) {
//                        builder.addStyle(
//                            ParagraphStyle(
//                                textIndent = TextIndent(
//                                    firstLine = 16.sp,
//                                    restLine = 16.sp,
//                                )
//                            ),
//                            start,
//                            end,
//                        )
//                    }
//                }
//            }
//
//            // Markers: only the ACTIVE block markers remain in transformed text
//            if (idx == activeBlockIndex) {
//                parsed.markers.forEach { range ->
//                    val s0 = range.first.coerceAtLeast(0)
//                    val e0 = (range.last + 1).coerceAtMost(n)
//                    val start = originalToTransformed[s0]
//                    val end = originalToTransformed[e0]
//                    if (start < end) {
//                        builder.addStyle(
//                            SpanStyle(color = activeMarkerColor),
//                            start,
//                            end,
//                        )
//                    }
//                }
//            }
//        }
//
//        // 4) Build transformed -> original offset map
//        val transformedToOriginal = IntArray(destLen + 1)
//        var i = 0
//        for (t in 0..destLen) {
//            while (i < n && originalToTransformed[i] < t) {
//                i++
//            }
//            transformedToOriginal[t] = i
//        }
//
//        val offsetMapping = object : OffsetMapping {
//            override fun originalToTransformed(offset: Int): Int {
//                val o = offset.coerceIn(0, n)
//                return originalToTransformed[o]
//            }
//
//            override fun transformedToOriginal(offset: Int): Int {
//                val t = offset.coerceIn(0, destLen)
//                return transformedToOriginal[t]
//            }
//        }
//
//        return TransformedText(builder.toAnnotatedString(), offsetMapping)
//    }
//}
