package io.github.nndwn.getgooglefont.compressit.compressor

import io.github.nndwn.getgooglefont.compressit.model.FontItem
import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

class FontCompressorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val compressor = FontCompressor()

    @Test
    fun testCompressToZip() {
        val sampleFontData = "mock ttf content".toByteArray()
        val entries = mapOf("roboto.ttf" to sampleFontData, "open_sans.ttf" to sampleFontData)

        val zipBytes = compressor.compressToZip(entries)
        assertTrue(zipBytes.isNotEmpty())

        val entryNames = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entryNames.add(entry.name)
                entry = zis.nextEntry
            }
        }

        assertEquals(2, entryNames.size)
        assertTrue(entryNames.contains("roboto.ttf"))
        assertTrue(entryNames.contains("open_sans.ttf"))
    }

    @Test
    fun testCompressTo7z() {
        val sampleFontData = "mock ttf content".toByteArray()
        val entries = mapOf("roboto.ttf" to sampleFontData, "open_sans.ttf" to sampleFontData)

        val targetFile = tempFolder.newFile("compressed_fonts.7z")
        compressor.compressTo7z(entries, targetFile)
        assertTrue(targetFile.length() > 0)

        val entryNames = mutableListOf<String>()
        SevenZFile.builder().setFile(targetFile).get().use { szf ->
            var entry = szf.nextEntry
            while (entry != null) {
                entryNames.add(entry.name)
                entry = szf.nextEntry
            }
        }

        assertEquals(2, entryNames.size)
        assertTrue(entryNames.contains("roboto.ttf"))
        assertTrue(entryNames.contains("open_sans.ttf"))
    }

    @Test
    fun testWriteEntriesToDir() {
        val entries = mapOf(
            "roboto.ttf" to "roboto bytes".toByteArray(),
            "open_sans.ttf" to "open sans bytes".toByteArray(),
        )

        val dir = tempFolder.newFolder("fonts")
        compressor.writeEntriesToDir(entries, dir)

        assertTrue(File(dir, "roboto.ttf").exists())
        assertTrue(File(dir, "open_sans.ttf").exists())
        assertEquals("roboto bytes", File(dir, "roboto.ttf").readText())
        assertEquals("open sans bytes", File(dir, "open_sans.ttf").readText())
    }

    @Test
    fun testWriteMetadataJsonWithDesigner() {
        val fonts = listOf(
            FontItem(
                idFont = "ROBOTO",
                displayName = "Roboto",
                scriptCategory = ScriptCategory.LATIN,
                googleFontName = "Roboto",
                designer = "Christian Robertson"
            )
        )

        val targetFile = tempFolder.newFile("test_fonts.json")
        compressor.writeMetadataJson(fonts, targetFile)

        val writtenContent = targetFile.readText()
        println("Written JSON content:\n$writtenContent")

        assertTrue(writtenContent.contains("Christian Robertson"))
        assertTrue(writtenContent.contains("designer"))
    }
}
