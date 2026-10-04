package io.github.nndwn.getgooglefont.compressit.compressor

import io.github.nndwn.getgooglefont.compressit.model.FontItem
import kotlinx.serialization.json.Json
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

    fun writeZipToFile(zipBytes: ByteArray, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        outputFile.writeBytes(zipBytes)
    }

    fun writeMetadataJson(fonts: List<FontItem>, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        val jsonString = jsonFormatter.encodeToString(fonts)
        outputFile.writeText(jsonString)
    }
}
