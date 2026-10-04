package io.github.nndwn.getgooglefont.compressit.parser

import java.io.File

data class ParsedFontSpec(
    val fontName: String
)

class FontsMdParser {

    fun parse(file: File): List<ParsedFontSpec> {
        if (!file.exists()) return emptyList()
        return parseContent(file.readText())
    }

    fun parseContent(content: String): List<ParsedFontSpec> {
        val result = mutableListOf<ParsedFontSpec>()

        content.lines().forEach { rawLine ->
            val line = rawLine.trim()

            if (line.startsWith("- ") || line.startsWith("* ")) {
                val name = line.substring(2).trim()
                if (name.isNotEmpty()) {
                    result.add(ParsedFontSpec(fontName = name))
                }
            }
        }

        return result
    }
}
