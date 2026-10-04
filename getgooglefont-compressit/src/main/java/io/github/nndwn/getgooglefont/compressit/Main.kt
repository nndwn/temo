package io.github.nndwn.getgooglefont.compressit

import io.github.nndwn.getgooglefont.compressit.catalog.GoogleFontsCatalog
import io.github.nndwn.getgooglefont.compressit.compressor.FontCompressor
import io.github.nndwn.getgooglefont.compressit.fetcher.DesignerFetcher
import io.github.nndwn.getgooglefont.compressit.fetcher.FontDownloader
import io.github.nndwn.getgooglefont.compressit.mapper.FontIdMapper
import io.github.nndwn.getgooglefont.compressit.model.FontItem
import io.github.nndwn.getgooglefont.compressit.parser.FontsMdParser
import io.github.nndwn.getgooglefont.compressit.parser.ParsedFontSpec
import io.github.nndwn.getgooglefont.compressit.validator.FontValidator
import java.io.File
import kotlin.system.exitProcess

fun main() {
    println("=== Starting Google Font Downloader & Compressor Task ===")

    var projectRootDir = File(".").canonicalFile
    if (projectRootDir.name == "getgooglefont-compressit") {
        projectRootDir = projectRootDir.parentFile
    }

    val inputMdFile = File(projectRootDir, "fonts.md")
    val rawFolder = File(projectRootDir, "app/src/main/res/raw")
    val fontFolder = File(projectRootDir, "app/src/main/res/font")
    val outputJsonFile = File(rawFolder, "fonts.json")

    if (!inputMdFile.exists()) {
        println("Error: Input file fonts.md not found at ${inputMdFile.absolutePath}")
        exitProcess(1)
    }

    val fontSpecs: List<ParsedFontSpec> = FontsMdParser().parse(inputMdFile)
    if (fontSpecs.isEmpty()) {
        println("Error: Input file fonts.md is empty or contains no font entries.")
        exitProcess(1)
    }

    println("Input fonts.md loaded: ${fontSpecs.size} font entries found.")

    // ------------------------------------------------------------------
    // STEP 1 - VALIDATION.
    // No files are downloaded before all font names are verified to exist
    // in the official Google Fonts catalog.
    // ------------------------------------------------------------------
    val canonicalNames = validateFontSpecs(fontSpecs) ?: run {
        println()
        println("=== Task Aborted: no files were downloaded ===")
        exitProcess(1)
    }

    val designerFetcher = DesignerFetcher()
    val fontDownloader = FontDownloader()
    val compressor = FontCompressor()

    val zipEntries = mutableMapOf<String, ByteArray>()
    val generatedFontItems = mutableListOf<FontItem>()

    println()
    println("--- Step 2/3: Gathering font files ---")

    for (i in fontSpecs.indices) {
        val spec = fontSpecs[i]
        val canonicalName = canonicalNames[i]
        val idFont = FontIdMapper.toIdFont(canonicalName)

        println("Processing [$idFont] $canonicalName (${spec.scriptCategory})...")

        // 1. Fetch designer
        val designer = designerFetcher.fetchDesigner(canonicalName)
        println("   Designer: $designer")

        // 2. Check local res name in app/src/main/res/font/
        val localResName = FontIdMapper.findLocalResName(canonicalName, fontFolder)

        // 3. Obtain TTF bytes (local res or online download)
        val filename = "${idFont.lowercase()}.ttf"
        var ttfBytes: ByteArray? = null

        if (!localResName.isNullOrEmpty()) {
            val localFile = File(fontFolder, "$localResName.ttf")
            if (localFile.exists()) {
                ttfBytes = localFile.readBytes()
                println("   Font loaded from local res: ${localFile.name}")
            }
        }

        if (ttfBytes == null) {
            println("   Downloading font from Google Fonts: $canonicalName...")
            ttfBytes = fontDownloader.downloadTtfBytes(canonicalName)
        }

        if (ttfBytes != null) {
            zipEntries[filename] = ttfBytes
            println("   Added to zip entries: $filename (${ttfBytes.size} bytes)")
        } else {
            println("   Warning: Could not obtain TTF bytes for $canonicalName")
        }

        // 4. Create FontItem entry
        val fontItem = FontItem(
            idFont = idFont,
            displayName = canonicalName,
            scriptCategory = spec.scriptCategory,
            localResName = localResName,
            googleFontName = canonicalName,
            designer = designer
        )
        generatedFontItems.add(fontItem)
    }

    // ------------------------------------------------------------------
    // STEP 3 - WRITING OUTPUT.
    // ------------------------------------------------------------------
    println()
    println("--- Step 3/3: Writing output ---")

    if (zipEntries.isNotEmpty()) {
        val outputZipFile = File(rawFolder, "compressed_fonts.zip")
        val zipBytes = compressor.compressToZip(zipEntries)
        compressor.writeZipToFile(zipBytes, outputZipFile)
        println("Successfully created ZIP file: ${outputZipFile.absolutePath} (${zipBytes.size} bytes)")
    }

    // Generate fonts.json from fonts.md
    compressor.writeMetadataJson(generatedFontItems, outputJsonFile)
    println("Successfully generated fonts.json from fonts.md at: ${outputJsonFile.absolutePath}")

    println()
    println("=== Task Completed Successfully ===")
}

/**
 * Validates all font family names in fonts.md against the official Google Fonts catalog.
 *
 * @return list of official canonical family names matching spec order, or `null` if validation fails.
 */
private fun validateFontSpecs(fontSpecs: List<ParsedFontSpec>): List<String>? {
    println()
    println("--- Step 1/3: Validating font names in fonts.md against Google Fonts catalog ---")

    val availableFamilies = GoogleFontsCatalog().families()
    if (availableFamilies.isNullOrEmpty()) {
        println("Error: Failed to fetch Google Fonts catalog (${GoogleFontsCatalog.DEFAULT_METADATA_URL}).")
        println("       Validation is intentionally strict to prevent downloading invalid fonts.")
        return null
    }
    println("Google Fonts catalog loaded: ${availableFamilies.size} families")

    val requestedNames = fontSpecs.map { it.fontName }
    val report = FontValidator(availableFamilies).validate(requestedNames)

    if (!report.isValid) {
        println(report.toMessage())
        println()
        println("Please fix the font names in fonts.md and re-run the task.")
        return null
    }

    requestedNames.forEachIndexed { index, requested ->
        val canonical = report.valid[index]
        val note = if (canonical == requested) "" else "  (normalized from \"$requested\")"
        println("  OK  $canonical$note")
    }
    println(report.toMessage())

    return report.valid
}
