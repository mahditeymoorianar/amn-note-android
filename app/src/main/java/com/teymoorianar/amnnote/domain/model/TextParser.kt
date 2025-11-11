package com.teymoorianar.amnnote.domain.model

object TextParser {

    data class ParsedBlock(
        val block: TextBlock,
        val contentRange: IntRange?,
        val markers: List<IntRange>,
    )

    fun parse(input: String): List<TextBlock> = analyze(input).map { it.block }

    fun analyze(input: String): List<ParsedBlock> {
        if (input.isEmpty()) return emptyList()

        val blocks = mutableListOf<TextBlock>()
        val ranges = mutableListOf<IntRange?>()
        val markersPerBlock = mutableListOf<MutableList<IntRange>>()

        var direction = TextDirection.NULL
        var bold = false
        var italic = false

        var lineStyle = TextStyle.BODY
        var currentStyle = lineStyle
        var currentLink = ""
        var isStartOfLine = true

        val sb = StringBuilder()
        var currentStart = -1
        var currentEnd = -1
        var currentMarkers = mutableListOf<IntRange>()
        val pendingOpeningMarkers = mutableListOf<IntRange>()

        fun headingStyleFromLevel(level: Int): TextStyle = when (level.coerceAtMost(5)) {
            1 -> TextStyle.HEADING_1
            2 -> TextStyle.HEADING_2
            3 -> TextStyle.HEADING_3
            4 -> TextStyle.HEADING_4
            else -> TextStyle.HEADING_5
        }

        fun attachMarker(range: IntRange) {
            if (sb.isEmpty()) {
                pendingOpeningMarkers.add(range)
            } else {
                currentMarkers.add(range)
            }
        }

        fun beginBlockIfNeeded(sourceIndex: Int) {
            if (sb.isEmpty()) {
                if (pendingOpeningMarkers.isNotEmpty()) {
                    currentMarkers.addAll(pendingOpeningMarkers)
                    pendingOpeningMarkers.clear()
                }
                currentStart = sourceIndex
            }
        }

        fun appendChar(sourceIndex: Int, char: Char) {
            beginBlockIfNeeded(sourceIndex)
            sb.append(char)
            currentEnd = sourceIndex
        }

        fun flush() {
            if (sb.isNotEmpty()) {
                val styleForBlock = currentStyle
                val linkForBlock = if (styleForBlock == TextStyle.LINK) currentLink else ""
                val block = TextBlock(
                    text = sb.toString(),
                    direction = direction,
                    bold = bold,
                    italic = italic,
                    style = styleForBlock,
                    link = linkForBlock,
                )
                blocks.add(block)
                val range = if (currentStart >= 0 && currentEnd >= currentStart) {
                    IntRange(currentStart, currentEnd)
                } else {
                    null
                }
                ranges.add(range)
                markersPerBlock.add(currentMarkers.toMutableList())
                sb.setLength(0)
                currentMarkers = mutableListOf()
                currentStart = -1
                currentEnd = -1
            }
        }

        fun addClosingMarker(range: IntRange) {
            if (markersPerBlock.isNotEmpty()) {
                markersPerBlock.last().add(range)
            } else {
                pendingOpeningMarkers.add(range)
            }
        }

        val escapableCharacters = setOf('*', '[', ']', '(', ')', '#', '-', '\\')

        var i = 0
        val length = input.length

        while (i < length) {
            if (isStartOfLine) {
                var k = i

                while (k < length) {
                    when {
                        input.startsWith("\\rtl", startIndex = k) -> {
                            direction = TextDirection.RTL
                            pendingOpeningMarkers.add(IntRange(k, k + 3))
                            k += 4
                        }
                        input.startsWith("\\ltr", startIndex = k) -> {
                            direction = TextDirection.LTR
                            pendingOpeningMarkers.add(IntRange(k, k + 3))
                            k += 4
                        }
                        else -> break
                    }
                }

                var j = k
                var hashes = 0
                while (j < length && input[j] == '#') {
                    hashes++
                    j++
                }

                if (hashes > 0 && j < length && input[j] == ' ') {
                    lineStyle = headingStyleFromLevel(hashes)
                    currentStyle = lineStyle
                    pendingOpeningMarkers.add(IntRange(k, j))
                    i = j + 1
                    isStartOfLine = false
                    continue
                }

                if (k < length && input[k] == '-' && k + 1 < length && input[k + 1] == ' ') {
                    lineStyle = TextStyle.LIST_ITEM
                    currentStyle = lineStyle
                    pendingOpeningMarkers.add(IntRange(k, k + 1))
                    i = k + 2
                    isStartOfLine = false
                    continue
                }

                lineStyle = TextStyle.BODY
                currentStyle = lineStyle
                i = k
                isStartOfLine = false
                if (i >= length) break
            }

            val c = input[i]

            when (c) {
                '\n' -> {
                    appendChar(i, '\n')
                    flush()
                    i++
                    isStartOfLine = true
                    lineStyle = TextStyle.BODY
                    currentStyle = lineStyle
                    currentLink = ""
                    pendingOpeningMarkers.clear()
                }

                '\\' -> {
                    when {
                        input.startsWith("\\rtl", startIndex = i) -> {
                            flush()
                            direction = TextDirection.RTL
                            attachMarker(IntRange(i, i + 3))
                            i += 4
                        }
                        input.startsWith("\\ltr", startIndex = i) -> {
                            flush()
                            direction = TextDirection.LTR
                            attachMarker(IntRange(i, i + 3))
                            i += 4
                        }
                        i + 1 < length && input[i + 1] in escapableCharacters -> {
                            attachMarker(IntRange(i, i))
                            appendChar(i + 1, input[i + 1])
                            i += 2
                        }
                        else -> {
                            appendChar(i, c)
                            i++
                        }
                    }
                }

                '[' -> {
                    val linkMatch = findLink(input, i)
                    if (linkMatch != null) {
                        flush()
                        attachMarker(IntRange(i, i))
                        currentStyle = TextStyle.LINK
                        currentLink = linkMatch.url

                        var labelIndex = linkMatch.labelStart
                        while (labelIndex < linkMatch.labelEndExclusive) {
                            appendChar(labelIndex, input[labelIndex])
                            labelIndex++
                        }

                        flush()
                        addClosingMarker(IntRange(linkMatch.closingStart, linkMatch.closingEnd))

                        currentStyle = lineStyle
                        currentLink = ""
                        i = linkMatch.endIndex
                    } else {
                        appendChar(i, c)
                        i++
                    }
                }

                '*' -> {
                    if (i + 1 < length && input[i + 1] == '*') {
                        val markerRange = IntRange(i, i + 1)
                        if (bold) {
                            flush()
                            addClosingMarker(markerRange)
                        } else {
                            attachMarker(markerRange)
                        }
                        bold = !bold
                        i += 2
                    } else {
                        val markerRange = IntRange(i, i)
                        if (italic) {
                            flush()
                            addClosingMarker(markerRange)
                        } else {
                            attachMarker(markerRange)
                        }
                        italic = !italic
                        i++
                    }
                }

                else -> {
                    appendChar(i, c)
                    i++
                }
            }
        }

        flush()

        return blocks.indices.map { index ->
            ParsedBlock(
                block = blocks[index],
                contentRange = ranges[index],
                markers = markersPerBlock[index].toList(),
            )
        }
    }

    fun encode(blocks: List<TextBlock>): String {
        val sb = StringBuilder()

        var currentDirection = TextDirection.NULL
        var currentBold = false
        var currentItalic = false
        var startOfLine = true

        fun headingLevel(style: TextStyle): Int = when (style) {
            TextStyle.HEADING_1 -> 1
            TextStyle.HEADING_2 -> 2
            TextStyle.HEADING_3 -> 3
            TextStyle.HEADING_4 -> 4
            TextStyle.HEADING_5 -> 5
            else -> 0
        }

        fun appendDirection(target: TextDirection) {
            if (target == TextDirection.NULL) return

            if (target != currentDirection) {
                when (target) {
                    TextDirection.RTL -> sb.append("\\rtl")
                    TextDirection.LTR -> sb.append("\\ltr")
                    else -> Unit
                }
                currentDirection = target
            }
        }

        fun appendBoldItalic(targetBold: Boolean, targetItalic: Boolean) {
            val needBold = targetBold != currentBold
            val needItalic = targetItalic != currentItalic

            when {
                needBold && needItalic -> {
                    sb.append("***")
                    currentBold = !currentBold
                    currentItalic = !currentItalic
                }

                needBold -> {
                    sb.append("**")
                    currentBold = !currentBold
                }

                needItalic -> {
                    sb.append("*")
                    currentItalic = !currentItalic
                }
            }
        }

        for (block in blocks) {
            var text = block.text
            val endsWithNewline = text.endsWith('\n')

            if (endsWithNewline) {
                text = text.dropLast(1)
            }

            if (startOfLine) {
                appendDirection(block.direction)

                val level = headingLevel(block.style)
                if (level > 0) {
                    repeat(level) { sb.append('#') }
                    sb.append(' ')
                } else if (block.style == TextStyle.LIST_ITEM) {
                    sb.append("- ")
                }
            } else {
                if (block.direction != currentDirection && block.direction != TextDirection.NULL) {
                    appendDirection(block.direction)
                }
            }

            appendBoldItalic(block.bold, block.italic)

            if (block.style == TextStyle.LINK && block.link.isNotEmpty()) {
                sb.append('[')
                sb.append(escapeContent(text))
                sb.append(']')
                sb.append('(')
                sb.append(block.link)
                sb.append(')')
            } else {
                sb.append(escapeContent(text))
            }

            if (endsWithNewline) {
                sb.append('\n')
                startOfLine = true
            } else {
                startOfLine = false
            }
        }

        return sb.toString()
    }

    private data class LinkMatch(
        val label: String,
        val url: String,
        val labelStart: Int,
        val labelEndExclusive: Int,
        val closingStart: Int,
        val closingEnd: Int,
        val endIndex: Int,
    )

    private fun findLink(text: String, start: Int): LinkMatch? {
        val length = text.length

        val closeBracket = text.indexOf(']', startIndex = start + 1)
        if (closeBracket == -1) return null
        if (closeBracket + 1 >= length || text[closeBracket + 1] != '(') return null

        val closeParen = text.indexOf(')', startIndex = closeBracket + 2)
        if (closeParen == -1) return null

        val labelStart = start + 1
        val labelEndExclusive = closeBracket
        val url = text.substring(closeBracket + 2, closeParen).trim()

        if (text.substring(labelStart, labelEndExclusive).contains('\n') || url.contains('\n')) {
            return null
        }

        return LinkMatch(
            label = text.substring(labelStart, labelEndExclusive),
            url = url,
            labelStart = labelStart,
            labelEndExclusive = labelEndExclusive,
            closingStart = closeBracket,
            closingEnd = closeParen,
            endIndex = closeParen + 1,
        )
    }

    private fun escapeContent(text: String): String {
        if (text.isEmpty()) return text

        val sb = StringBuilder(text.length)
        var startOfLine = true

        text.forEach { char ->
            val needsEscape = when (char) {
                '\\', '*', '[', ']', '(', ')', '{', '}', '`' -> true
                '#', '-' -> startOfLine
                else -> false
            }

            if (needsEscape) {
                sb.append('\\')
            }

            sb.append(char)

            startOfLine = char == '\n'
        }

        return sb.toString()
    }
}
