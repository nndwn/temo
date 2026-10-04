package io.github.nndwn.getgooglefont.compressit.mapper

import java.io.File

object FontIdMapper {

    /**
     * Converts a font family name (e.g. "Open Sans", "Dela Gothic One", "DotGothic16")
     * into an idFont in UPPER_SNAKE_CASE format (e.g. "OPEN_SANS", "DELA_GOTHIC_ONE").
     */
    fun toIdFont(fontName: String): String {
        val customMap = mapOf(
            "DotGothic16" to "DOT_GOTHIC"
        )
        customMap[fontName]?.let { return it }

        val withSpaces = fontName.replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
        return withSpaces
            .uppercase()
            .replace(Regex("[^A-Z0-9]+"), "_")
            .trim('_')
    }

    /**
     * Finds matching local font resource name in `app/src/main/res/font/` if present.
     *
     * Example: "Bebas Neue" -> "bebasneue_regular", "Lato" -> "lato_bold".
     */
    fun findLocalResName(fontName: String, fontFolder: File): String? {
        if (!fontFolder.exists() || !fontFolder.isDirectory) return null

        val cleanFontName = fontName.lowercase().filter { it.isLetterOrDigit() }
        val files = fontFolder.listFiles() ?: return null

        for (file in files) {
            if (!file.isFile) continue
            val nameWithoutExt = file.nameWithoutExtension
            val cleanFileName = nameWithoutExt.lowercase().filter { it.isLetterOrDigit() }

            if (cleanFileName.startsWith(cleanFontName) || cleanFontName.startsWith(cleanFileName)) {
                return nameWithoutExt
            }
        }

        return null
    }
}
