package io.github.nndwn.getgooglefont.compressit.model

import kotlinx.serialization.Serializable

@Serializable
data class FontItem(
    val idFont: String,
    val displayName: String,
    val scriptCategory: ScriptCategory,
    val googleFontName: String? = null,
    val localResName: String? = null,
    val designer: String? = null
)

/**
 * Returns the family name used for looking up and downloading from Google Fonts.
 *
 * Prefers [googleFontName] if present; otherwise strips script annotations in parentheses
 * from [displayName] (e.g. `"Dela Gothic One (日本語)"` -> `"Dela Gothic One"`).
 */
fun FontItem.resolvedFamilyName(): String {
    val googleName = googleFontName?.trim()
    if (!googleName.isNullOrEmpty()) return googleName
    return displayName.substringBefore(" (").trim()
}
