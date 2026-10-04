package io.github.nndwn.getgooglefont.compressit.parser

import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import java.io.File

data class ParsedFontSpec(
    val fontName: String,
    val scriptCategory: ScriptCategory
)

class FontsMdParser {

    fun parse(file: File): List<ParsedFontSpec> {
        if (!file.exists()) return emptyList()
        return parseContent(file.readText())
    }

    fun parseContent(content: String): List<ParsedFontSpec> {
        val result = mutableListOf<ParsedFontSpec>()
        var currentScript = ScriptCategory.LATIN

        content.lines().forEach { rawLine ->
            val line = rawLine.trim()

            if (line.startsWith("##")) {
                val header = line.removePrefix("##").trim().uppercase()
                currentScript = try {
                    ScriptCategory.valueOf(header)
                } catch (_: Exception) {
                    ScriptCategory.LATIN
                }
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                val name = line.substring(2).trim()
                if (name.isNotEmpty()) {
                    result.add(ParsedFontSpec(fontName = name, scriptCategory = currentScript))
                }
            }
        }

        return result
    }
}
