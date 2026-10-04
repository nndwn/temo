package io.github.nndwn.getgooglefont.compressit.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FontItemSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Test
    fun testSerializationWithDesigner() {
        val font = FontItem(
            idFont = "ROBOTO",
            displayName = "Roboto",
            scriptCategory = ScriptCategory.LATIN,
            googleFontName = "Roboto",
            designer = "Christian Robertson"
        )

        val jsonString = json.encodeToString(font)
        println("Serialized JSON:\n$jsonString")

        val decoded = json.decodeFromString<FontItem>(jsonString)
        assertEquals("ROBOTO", decoded.idFont)
        assertEquals("Roboto", decoded.displayName)
        assertEquals(ScriptCategory.LATIN, decoded.scriptCategory)
        assertEquals("Roboto", decoded.googleFontName)
        assertEquals("Christian Robertson", decoded.designer)
    }

    @Test
    fun testDeserializationWithoutDesignerDefaultsToNull() {
        val legacyJson = """
            {
                "idFont": "OPEN_SANS",
                "displayName": "Open Sans",
                "scriptCategory": "LATIN",
                "googleFontName": "Open Sans"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<FontItem>(legacyJson)
        assertEquals("OPEN_SANS", decoded.idFont)
        assertEquals("Open Sans", decoded.displayName)
        assertEquals(ScriptCategory.LATIN, decoded.scriptCategory)
        assertEquals("Open Sans", decoded.googleFontName)
        assertNull(decoded.designer)
    }
}
