package io.github.nndwn.getgooglefont.compressit

import io.github.nndwn.getgooglefont.compressit.catalog.GoogleFontsCatalog
import io.github.nndwn.getgooglefont.compressit.compressor.FontCompressor
import io.github.nndwn.getgooglefont.compressit.fetcher.DesignerFetcher
import io.github.nndwn.getgooglefont.compressit.fetcher.FontDownloader
import io.github.nndwn.getgooglefont.compressit.mapper.FontIdMapper
import io.github.nndwn.getgooglefont.compressit.model.FontBundleInfo
import io.github.nndwn.getgooglefont.compressit.model.FontBundleManifest
import io.github.nndwn.getgooglefont.compressit.model.FontItem
import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import io.github.nndwn.getgooglefont.compressit.parser.FontsMdParser
import io.github.nndwn.getgooglefont.compressit.parser.ParsedFontSpec
import io.github.nndwn.getgooglefont.compressit.validator.FontValidator
import java.io.File
import java.security.MessageDigest
import kotlin.system.exitProcess
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    println("=== Starting Google Font Downloader & Compressor Task ===")

    val zipEnabled = args.contains("--zip")
    val lzmaEnabled = args.contains("--lzma")
    val valueFlags = setOf("--bundle-version", "--base-url")
    val positionalArgs = positionalArgs(args, valueFlags)

    val bundleVersion = valueArg(args, "--bundle-version") ?: "fonts-1"
    val baseUrl = valueArg(args, "--base-url") ?: "https://github.com/nndwn/temo/releases/download"

    if (positionalArgs.size < 2) {
        println("Error: Missing required arguments.")
        println("Usage: MainKt <inputMdFilePath> <rawOutputDirPath> [localFontDirPath] [downloadedFontsDirPath] [--zip] [--lzma] [--bundle-version <v>] [--base-url <url>]")
        exitProcess(1)
    }

    if (lzmaEnabled && !zipEnabled) {
        println("Warning: --lzma has no effect without --zip.")
    }

    val inputMdFile = File(positionalArgs[0])
    val rawFolder = File(positionalArgs[1])
    val fontFolder = if (positionalArgs.size > 2) File(positionalArgs[2]) else File(rawFolder.parentFile, "font")
    val downloadedFontsDir = if (positionalArgs.size > 3) File(positionalArgs[3]) else File("build/downloaded-fonts")
    val bundlesDir = File(downloadedFontsDir.parentFile, "font-bundles")
    val outputJsonFile = File(rawFolder, "fonts.json")
    val outputBundlesJsonFile = File(rawFolder, "bundles.json")

    println("Input MD file: ${inputMdFile.absolutePath}")
    println("Output RAW dir: ${rawFolder.absolutePath}")
    println("Local FONT dir: ${fontFolder.absolutePath}")
    println("Downloaded fonts dir: ${downloadedFontsDir.absolutePath}")
    println("Font bundles dir: ${bundlesDir.absolutePath}")
    println("Bundle version: $bundleVersion")
    println("Base URL: $baseUrl")

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
    // STEP 1 - VALIDATION & CATALOG FETCHING.
    // Verifies all font names against the Google Fonts catalog.
    // ------------------------------------------------------------------
    val catalog = GoogleFontsCatalog()
    val canonicalNames = validateFontSpecs(fontSpecs, catalog) ?: run {
        println()
        println("=== Task Aborted: no files were downloaded ===")
        exitProcess(1)
    }

    // Automatically detect script categories from official catalog metadata
    val detectedScriptCategories = catalog.scriptCategories() ?: emptyMap()

    val designerFetcher = DesignerFetcher()
    val fontDownloader = FontDownloader()
    val compressor = FontCompressor()

    val downloadedEntries = mutableMapOf<String, ByteArray>()
    val downloadedByScript = mutableMapOf<ScriptCategory, MutableMap<String, ByteArray>>()
    val generatedFontItems = mutableListOf<FontItem>()

    println()
    println("--- Step 2/3: Gathering font files & metadata ---")

    for (i in fontSpecs.indices) {
        val canonicalName = canonicalNames[i]
        val idFont = FontIdMapper.toIdFont(canonicalName)
        val scriptCategory = detectedScriptCategories[canonicalName] ?: ScriptCategory.LATIN

        println("Processing [$idFont] $canonicalName (Detected script: $scriptCategory)...")

        // 1. Fetch designer
        val designer = designerFetcher.fetchDesigner(canonicalName)
        println("   Designer: $designer")

        // 2. Check local res name in app/src/main/res/font/
        val localResName = FontIdMapper.findLocalResName(canonicalName, fontFolder)

        // 3. Obtain TTF bytes (local res or online download)
        val filename = "${idFont.lowercase()}.ttf"
        var ttfBytes: ByteArray? = null
        var downloaded = false

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
            downloaded = ttfBytes != null
        }

        if (ttfBytes != null && downloaded) {
            downloadedEntries[filename] = ttfBytes
            downloadedByScript.getOrPut(scriptCategory) { mutableMapOf() }[filename] = ttfBytes
            println("   Added to downloaded bundle: $filename (${ttfBytes.size} bytes)")
        } else if (ttfBytes != null) {
            println("   Skipped from downloaded bundle (already local): $filename")
        } else {
            println("   Warning: Could not obtain TTF bytes for $canonicalName")
        }

        // 4. Create FontItem entry
        val fontItem = FontItem(
            idFont = idFont,
            displayName = canonicalName,
            scriptCategory = scriptCategory,
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

    if (downloadedEntries.isNotEmpty()) {
        compressor.writeEntriesToDir(downloadedEntries, downloadedFontsDir)
        println("Successfully wrote ${downloadedEntries.size} downloaded font files to: ${downloadedFontsDir.absolutePath}")

        writeBundles(
            downloadedByScript = downloadedByScript,
            bundlesDir = bundlesDir,
            compressor = compressor,
            baseUrl = baseUrl,
            bundleVersion = bundleVersion,
            outputBundlesJsonFile = outputBundlesJsonFile,
        )

        if (zipEnabled) {
            val outputZipFile = File(rawFolder, "compressed_fonts.zip")
            val zipBytes = compressor.compressToZip(downloadedEntries)
            compressor.writeZipToFile(zipBytes, outputZipFile)
            println("Successfully created ZIP file: ${outputZipFile.absolutePath} (${zipBytes.size} bytes)")

            if (lzmaEnabled) {
                val output7zFile = File(rawFolder, "compressed_fonts.7z")
                compressor.compressTo7z(downloadedEntries, output7zFile)
                val lzmaBytes = output7zFile.length()
                println("Successfully created LZMA2 7z file: ${output7zFile.absolutePath} ($lzmaBytes bytes)")

                val savingsPercent = if (zipBytes.isNotEmpty()) {
                    (1.0 - lzmaBytes.toDouble() / zipBytes.size.toDouble()) * 100.0
                } else {
                    0.0
                }
                println("LZMA2 vs ZIP: $lzmaBytes vs ${zipBytes.size} bytes (${"%.2f".format(savingsPercent)}% smaller)")
            }
        }
    } else {
        println("No downloaded fonts to bundle. All fonts are already available locally in res/font.")
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
private fun validateFontSpecs(fontSpecs: List<ParsedFontSpec>, catalog: GoogleFontsCatalog): List<String>? {
    println()
    println("--- Step 1/3: Validating font names in fonts.md against Google Fonts catalog ---")

    val availableFamilies = catalog.families()
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

private fun positionalArgs(args: Array<String>, valueFlags: Set<String>): List<String> {
    val result = mutableListOf<String>()
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (arg.startsWith("--")) {
            if (arg in valueFlags) i++
        } else {
            result.add(arg)
        }
        i++
    }
    return result
}

private fun valueArg(args: Array<String>, name: String): String? {
    val i = args.indexOf(name)
    return if (i >= 0 && i + 1 < args.size) args[i + 1] else null
}

private fun writeBundles(
    downloadedByScript: Map<ScriptCategory, Map<String, ByteArray>>,
    bundlesDir: File,
    compressor: FontCompressor,
    baseUrl: String,
    bundleVersion: String,
    outputBundlesJsonFile: File,
) {
    if (downloadedByScript.isEmpty()) {
        println("No downloadable font bundles to generate.")
        return
    }

    bundlesDir.mkdirs()
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val bundles = downloadedByScript.entries.associate { (script, entries) ->
        val zipFileName = "${script.name.lowercase()}.zip"
        val zipFile = File(bundlesDir, zipFileName)
        val zipBytes = compressor.compressToZip(entries)
        zipFile.writeBytes(zipBytes)
        val sha = sha256(zipBytes)
        println("Created bundle: ${zipFile.absolutePath} (${zipBytes.size} bytes, sha256=$sha)")
        script.name to FontBundleInfo(
            file = zipFileName,
            sha256 = sha,
        )
    }

    val manifest = FontBundleManifest(
        version = bundleVersion,
        baseUrl = baseUrl,
        bundles = bundles,
    )
    outputBundlesJsonFile.parentFile?.mkdirs()
    outputBundlesJsonFile.writeText(json.encodeToString(manifest))
    println("Successfully generated bundles.json at: ${outputBundlesJsonFile.absolutePath}")
}

private fun sha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}
