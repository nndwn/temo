package io.github.nndwn.getgooglefont.compressit.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class FontsMdParserTest {

    private val parser = FontsMdParser()

    @Test
    fun testParseContentWithBullets() {
        val sampleMd = """
            # Fonts Input List

            - Roboto
            - Open Sans
            - Dela Gothic One
            - Kosugi Maru
        """.trimIndent()

        val parsed = parser.parseContent(sampleMd)
        assertEquals(4, parsed.size)

        assertEquals("Roboto", parsed[0].fontName)
        assertEquals("Open Sans", parsed[1].fontName)
        assertEquals("Dela Gothic One", parsed[2].fontName)
        assertEquals("Kosugi Maru", parsed[3].fontName)
    }
}
