package io.github.nndwn.getgooglefont.compressit.parser

import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class FontsMdParserTest {

    private val parser = FontsMdParser()

    @Test
    fun testParseContentWithHeadersAndBullets() {
        val sampleMd = """
            # Fonts List

            ## Latin
            - Roboto
            - Open Sans

            ## Japanese
            - Dela Gothic One
            - Kosugi Maru
        """.trimIndent()

        val parsed = parser.parseContent(sampleMd)
        assertEquals(4, parsed.size)

        assertEquals("Roboto", parsed[0].fontName)
        assertEquals(ScriptCategory.LATIN, parsed[0].scriptCategory)

        assertEquals("Open Sans", parsed[1].fontName)
        assertEquals(ScriptCategory.LATIN, parsed[1].scriptCategory)

        assertEquals("Dela Gothic One", parsed[2].fontName)
        assertEquals(ScriptCategory.JAPANESE, parsed[2].scriptCategory)

        assertEquals("Kosugi Maru", parsed[3].fontName)
        assertEquals(ScriptCategory.JAPANESE, parsed[3].scriptCategory)
    }
}
