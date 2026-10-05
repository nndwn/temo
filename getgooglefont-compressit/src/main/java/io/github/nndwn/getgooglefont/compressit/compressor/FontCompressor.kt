package io.github.nndwn.getgooglefont.compressit.compressor

import io.github.nndwn.getgooglefont.compressit.model.FontItem
import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZMethod
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.tukaani.xz.LZMA2Options
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.iterator

class FontCompressor {

    private val jsonFormatter = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun compressToZip(entries: Map<String, ByteArray>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.setLevel(Deflater.BEST_COMPRESSION)
            for ((filename, bytes) in entries) {
                val entry = ZipEntry(filename)
                zos.putNextEntry(entry)
                zos.write(bytes)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    /**
     * Compresses the given entries into a 7z archive using LZMA2 (the modern LZMA variant
     * used by 7-Zip / XZ). Added as an optional comparison against the default ZIP output.
     */
    fun compressTo7z(entries: Map<String, ByteArray>, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        SevenZOutputFile(outputFile).use { sevenZ ->
            sevenZ.setContentMethods(
                listOf(
                    SevenZMethodConfiguration(
                        SevenZMethod.LZMA2,
                        LZMA2Options(LZMA2Options.PRESET_MAX),
                    ),
                ),
            )
            for ((filename, bytes) in entries) {
                val entry = SevenZArchiveEntry()
                entry.name = filename
                sevenZ.putArchiveEntry(entry)
                sevenZ.write(bytes)
                sevenZ.closeArchiveEntry()
            }
        }
    }

    fun writeZipToFile(zipBytes: ByteArray, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        outputFile.writeBytes(zipBytes)
    }

    fun writeEntriesToDir(entries: Map<String, ByteArray>, outputDir: File) {
        outputDir.mkdirs()
        for ((filename, bytes) in entries) {
            File(outputDir, filename).writeBytes(bytes)
        }
    }

    fun writeMetadataJson(fonts: List<FontItem>, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        val jsonString = jsonFormatter.encodeToString(fonts)
        outputFile.writeText(jsonString)
    }
}
