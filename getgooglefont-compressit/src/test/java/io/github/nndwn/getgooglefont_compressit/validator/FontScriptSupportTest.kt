package io.github.nndwn.getgooglefont.compressit.validator

import io.github.nndwn.getgooglefont.compressit.model.FontItem
import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Font
import java.awt.FontFormatException
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

class FontScriptSupportTest {

    @Test
    fun `pengujian dukungan karakter sampel pada file font lokal dan zip`() {
        val rootDir = findProjectRootDir()
        val zipFile = File(rootDir, "app/src/main/res/raw/compressed_fonts.zip")
        val jsonFile = File(rootDir, "app/src/main/res/raw/fonts.json")

        assertTrue("File compressed_fonts.zip harus ada", zipFile.exists())
        assertTrue("File fonts.json harus ada", jsonFile.exists())

        val json = Json { ignoreUnknownKeys = true }
        val fonts: List<FontItem> = json.decodeFromString(jsonFile.readText())
        val fontById = fonts.associateBy { it.idFont.lowercase() }

        val zipBytes = zipFile.readBytes()
        var verifiedCount = 0

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val filename = entry.name
                val fontId = filename.removeSuffix(".ttf").lowercase()
                val fontItem = fontById[fontId]

                if (fontItem != null) {
                    val fontBytes = zis.readBytes()
                    try {
                        val font = Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(fontBytes))
                        val sampleChar = sampleCharFor(fontItem.scriptCategory)
                        val canDisplay = font.canDisplay(sampleChar.code)

                        assertTrue(
                            "Font [${fontItem.idFont}] (${fontItem.displayName}) dengan ScriptCategory [${fontItem.scriptCategory}] " +
                                "harus dapat menampilkan karakter '$sampleChar' (U+${Integer.toHexString(sampleChar.code).uppercase()})",
                            canDisplay
                        )
                        verifiedCount++
                    } catch (_: FontFormatException) {
                        // File format web font (seperti WOFF2) di-skip di AWT JVM test,
                        // karena WOFF2 didukung langsung oleh Android Skia/Typeface.
                    }
                }

                entry = zis.nextEntry
            }
        }

        assertTrue("Harus berhasil memverifikasi minimal 20 font TTF lokal/mentah", verifiedCount >= 20)
        println("Berhasil memverifikasi dukungan glak karakter untuk $verifiedCount font TTF.")
    }

    @Test
    fun `sampleCharFor mengembalikan karakter sampel unik per ScriptCategory`() {
        assertEquals('A', sampleCharFor(ScriptCategory.LATIN))
        assertEquals('あ', sampleCharFor(ScriptCategory.JAPANESE))
        assertEquals('한', sampleCharFor(ScriptCategory.KOREAN))
        assertEquals('汉', sampleCharFor(ScriptCategory.CHINESE))
        assertEquals('ع', sampleCharFor(ScriptCategory.ARABIC))
        assertEquals('अ', sampleCharFor(ScriptCategory.DEVANAGARI))
        assertEquals('ก', sampleCharFor(ScriptCategory.THAI))
        assertEquals('ក', sampleCharFor(ScriptCategory.KHMER))
        assertEquals('א', sampleCharFor(ScriptCategory.HEBREW))
    }

    private fun sampleCharFor(scriptCategory: ScriptCategory): Char {
        return when (scriptCategory) {
            ScriptCategory.LATIN -> 'A'
            ScriptCategory.JAPANESE -> 'あ'
            ScriptCategory.KOREAN -> '한'
            ScriptCategory.CHINESE -> '汉'
            ScriptCategory.ARABIC -> 'ع'
            ScriptCategory.DEVANAGARI -> 'अ'
            ScriptCategory.THAI -> 'ก'
            ScriptCategory.KHMER -> 'ក'
            ScriptCategory.HEBREW -> 'א'
        }
    }

    private fun findProjectRootDir(): File {
        var dir = File(".").canonicalFile
        if (dir.name == "getgooglefont-compressit") {
            dir = dir.parentFile
        }
        return dir
    }
}
