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
 * Nama family yang dipakai untuk lookup/unduh di Google Fonts.
 *
 * Dipakai `googleFontName` bila ada; kalau tidak, ambil dari [displayName] tanpa
 * keterangan script di dalam tanda kurung (mis. `"Dela Gothic One (日本語)"` -> `"Dela Gothic One"`).
 */
fun FontItem.resolvedFamilyName(): String {
    val googleName = googleFontName?.trim()
    if (!googleName.isNullOrEmpty()) return googleName
    return displayName.substringBefore(" (").trim()
}
