package com.teymoorianar.amnnote.domain.model

data class TextBlock (
    var text: String,
    var direction: TextDirection,
    var bold: Boolean,
    var italic: Boolean,
    var style: TextStyle,
    var link: String,
)

enum class TextStyle {
    BODY, POWER, SUBTITLE, LINK,
    HEADING_1, HEADING_2, HEADING_3, HEADING_4, HEADING_5
}
enum class TextDirection {
    LTR, RTL, NULL
}