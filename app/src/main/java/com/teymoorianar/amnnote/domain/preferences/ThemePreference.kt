package com.teymoorianar.amnnote.domain.preferences

/**
 * Represents the color theme preference selected by the user.
 */
enum class ThemePreference {
    /** Dark mode renders UI surfaces using the dark color palette. */
    DARK,

    /** Light mode renders UI surfaces using the light color palette. */
    LIGHT;

    /** Returns `true` when the preference corresponds to the dark color scheme. */
    val isDarkMode: Boolean
        get() = this == DARK

    companion object {
        /**
         * Parses the persisted [value] into a [ThemePreference], defaulting to [DARK]
         * when the provided value is `null` or unrecognized.
         */
        fun fromValue(value: String?): ThemePreference =
            entries.firstOrNull { it.name == value } ?: DARK
    }
}
