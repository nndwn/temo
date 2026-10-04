package io.github.nndwn.getgooglefont.compressit.validator

import io.github.nndwn.getgooglefont.compressit.catalog.GoogleFontsCatalog
import io.github.nndwn.getgooglefont.compressit.model.FontItem
import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import io.github.nndwn.getgooglefont.compressit.model.resolvedFamilyName
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tes integrasi untuk memvalidasi input nyata project ([fonts.json] dan [fonts.md])
 * terhadap katalog live Google Fonts.
 */
class FontValidatorIntegrationTest {

    @Test
    fun `semua font di app res raw fonts json harus 100 persen valid di katalog Google Fonts`() {
        val rootDir = findProjectRootDir()
        val fontsJsonFile = File(rootDir, "app/src/main/res/raw/fonts.json")
        assertTrue("File fonts.json harus ada", fontsJsonFile.exists())

        val json = Json { ignoreUnknownKeys = true }
        val fonts: List<FontItem> = json.decodeFromString(fontsJsonFile.readText())
        assertFalse("fonts.json tidak boleh kosong", fonts.isEmpty())

        val catalog = GoogleFontsCatalog()
        val families = catalog.families()
        assertNotNull("Katalog Google Fonts harus berhasil dimuat", families)

        val validator = FontValidator(families!!)
        val requested = fonts.map { it.resolvedFamilyName() }
        val report = validator.validate(requested)

        assertTrue(
            "Semua font di fonts.json harus valid!\n${report.toMessage()}",
            report.isValid
        )
        assertEquals(fonts.size, report.valid.size)
    }

    @Test
    fun `semua nama font di fonts md harus 100 persen valid di katalog Google Fonts`() {
        val rootDir = findProjectRootDir()
        val fontsMdFile = File(rootDir, "fonts.md")
        assertTrue("File fonts.md harus ada", fontsMdFile.exists())

        // Ambil baris bullet list "- Nama Font"
        val namesFromMd = fontsMdFile.readLines()
            .map { it.trim() }
            .filter { it.startsWith("- ") }
            .map { it.removePrefix("- ").trim() }
            .filter { it.isNotEmpty() }

        assertFalse("fonts.md harus berisi daftar bullet font", namesFromMd.isEmpty())

        val catalog = GoogleFontsCatalog()
        val families = catalog.families()
        assertNotNull("Katalog Google Fonts harus berhasil dimuat", families)

        val validator = FontValidator(families!!)
        val report = validator.validate(namesFromMd)

        assertTrue(
            "Semua font di fonts.md harus valid!\n${report.toMessage()}",
            report.isValid
        )
    }

    @Test
    fun `validasi harus menolak nama typo Ankor dan memberikan saran Angkor`() {
        val catalog = GoogleFontsCatalog()
        val families = catalog.families()
        assertNotNull(families)

        val validator = FontValidator(families!!)
        val report = validator.validate(listOf("Ankor"))

        assertFalse("Nama 'Ankor' harus ditolak karena typo", report.isValid)
        assertEquals(1, report.invalid.size)
        assertEquals("Ankor", report.invalid.first().input)
        assertEquals("Angkor", report.invalid.first().suggestion)
    }

    @Test
    fun `resolvedFamilyName mengabaikan keterangan script dalam tanda kurung`() {
        val itemWithParen = FontItem(
            idFont = "DELA_GOTHIC_ONE",
            displayName = "Dela Gothic One (日本語)",
            scriptCategory = ScriptCategory.JAPANESE,
        )
        assertEquals("Dela Gothic One", itemWithParen.resolvedFamilyName())

        val itemExplicit = FontItem(
            idFont = "DELA_GOTHIC_ONE",
            displayName = "Dela Gothic One (日本語)",
            scriptCategory = ScriptCategory.JAPANESE,
            googleFontName = "Dela Gothic One",
        )
        assertEquals("Dela Gothic One", itemExplicit.resolvedFamilyName())
    }

    private fun findProjectRootDir(): File {
        var dir = File(".").canonicalFile
        if (dir.name == "getgooglefont-compressit") {
            dir = dir.parentFile
        }
        return dir
    }
}
