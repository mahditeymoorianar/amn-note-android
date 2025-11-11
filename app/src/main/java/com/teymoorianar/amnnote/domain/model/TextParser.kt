package com.teymoorianar.amnnote.domain.model

object TextParser {

    fun parse(input: String): List<TextBlock> {
        val blocks = mutableListOf<TextBlock>()

        var direction = TextDirection.NULL
        var bold = false
        var italic = false

        var lineStyle = TextStyle.BODY
        var currentStyle = lineStyle
        var currentLink = ""
        var isStartOfLine = true

        val sb = StringBuilder()

        fun flush() {
            if (sb.isNotEmpty()) {
                val styleForBlock = currentStyle
                val linkForBlock = if (styleForBlock == TextStyle.LINK) currentLink else ""
                blocks.add(
                    TextBlock(
                        text = sb.toString(),
                        direction = direction,
                        bold = bold,
                        italic = italic,
                        style = styleForBlock,
                        link = linkForBlock,
                    )
                )
                sb.setLength(0)
            }
        }

        fun headingStyleFromLevel(level: Int): TextStyle =
            when (level.coerceAtMost(5)) {
                1 -> TextStyle.HEADING_1
                2 -> TextStyle.HEADING_2
                3 -> TextStyle.HEADING_3
                4 -> TextStyle.HEADING_4
                else -> TextStyle.HEADING_5
            }

        var i = 0
        val length = input.length

        while (i < length) {
            // Handle beginning-of-line logic: direction markers + heading markup.
            if (isStartOfLine) {
                var k = i

                // Apply any number of \rtl / \ltr at the start of the line.
                lineStartLoop@ while (k < length) {
                    when {
                        input.startsWith("\\rtl", startIndex = k) -> {
                            direction = TextDirection.RTL
                            k += 4
                        }
                        input.startsWith("\\ltr", startIndex = k) -> {
                            direction = TextDirection.LTR
                            k += 4
                        }
                        else -> break@lineStartLoop
                    }
                }

                // Detect heading: one or more '#' followed by a single space.
                var j = k
                var hashes = 0
                while (j < length && input[j] == '#') {
                    hashes++
                    j++
                }

                if (hashes > 0 && j < length && input[j] == ' ') {
                    lineStyle = headingStyleFromLevel(hashes)
                    currentStyle = lineStyle
                    // Skip '#'... and the single space.
                    i = j + 1
                } else {
                    lineStyle = TextStyle.BODY
                    currentStyle = lineStyle
                    i = k
                }

                isStartOfLine = false
                if (i >= length) break
            }

            val c = input[i]

            when (c) {
                '\n' -> {
                    // End of line: include the newline, then flush and prepare for next line.
                    sb.append('\n')
                    flush()
                    i++
                    isStartOfLine = true
                    lineStyle = TextStyle.BODY
                    currentStyle = lineStyle
                    currentLink = ""
                }

                '\\' -> {
                    // Direction markers in the middle of a line.
                    when {
                        input.startsWith("\\rtl", startIndex = i) -> {
                            flush()
                            direction = TextDirection.RTL
                            i += 4
                        }
                        input.startsWith("\\ltr", startIndex = i) -> {
                            flush()
                            direction = TextDirection.LTR
                            i += 4
                        }
                        else -> {
                            sb.append(c)
                            i++
                        }
                    }
                }

                '[' -> {
                    // Try to parse a markdown link [label](url)
                    val linkMatch = findLink(input, i)
                    if (linkMatch != null) {
                        flush()
                        currentStyle = TextStyle.LINK
                        currentLink = linkMatch.url
                        sb.append(linkMatch.label)
                        flush()
                        currentStyle = lineStyle
                        currentLink = ""
                        i = linkMatch.endIndex
                    } else {
                        sb.append(c)
                        i++
                    }
                }

                '*' -> {
                    // Bold / italic toggles: '**' for bold, '*' for italic.
                    if (i + 1 < length && input[i + 1] == '*') {
                        flush()
                        bold = !bold
                        i += 2
                    } else {
                        flush()
                        italic = !italic
                        i++
                    }
                }

                else -> {
                    sb.append(c)
                    i++
                }
            }
        }

        flush()
        return blocks
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
                // toggle both using ***
                needBold && needItalic -> {
                    sb.append("***")
                    currentBold = !currentBold
                    currentItalic = !currentItalic
                }

                // toggle only bold using **
                needBold -> {
                    sb.append("**")
                    currentBold = !currentBold
                }

                // toggle only italic using *
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
                // direction markers come first at the beginning of each line
                appendDirection(block.direction)

                // then heading markup if this line is a heading
                val level = headingLevel(block.style)
                if (level > 0) {
                    repeat(level) { sb.append('#') }
                    sb.append(' ')
                }
            } else {
                // direction changes in the middle of a line
                if (block.direction != currentDirection && block.direction != TextDirection.NULL) {
                    appendDirection(block.direction)
                }
            }

            // apply inline bold/italic toggles before writing the text
            appendBoldItalic(block.bold, block.italic)

            // content itself
            if (block.style == TextStyle.LINK && block.link.isNotEmpty()) {
                sb.append('[')
                sb.append(text)
                sb.append(']')
                sb.append('(')
                sb.append(block.link)
                sb.append(')')
            } else {
                sb.append(text)
            }

            // handle line breaks
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
        val endIndex: Int,
    )

    private fun findLink(text: String, start: Int): LinkMatch? {
        val length = text.length

        val closeBracket = text.indexOf(']', startIndex = start + 1)
        if (closeBracket == -1) return null
        if (closeBracket + 1 >= length || text[closeBracket + 1] != '(') return null

        val closeParen = text.indexOf(')', startIndex = closeBracket + 2)
        if (closeParen == -1) return null

        val label = text.substring(start + 1, closeBracket)
        val url = text.substring(closeBracket + 2, closeParen).trim()

        // For simplicity, reject links that span multiple lines.
        if (label.contains('\n') || url.contains('\n')) return null

        return LinkMatch(label = label, url = url, endIndex = closeParen + 1)
    }
}