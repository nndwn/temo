package io.github.nndwn.getgooglefont.compressit.catalog

import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import io.github.nndwn.getgooglefont.compressit.net.HttpFetcher

/**
 * Official Google Fonts catalog fetched from the public metadata endpoint.
 *
 * Serves as the source of truth for **validating** family names before downloading,
 * ensuring no invalid or missing font names are processed.
 */
class GoogleFontsCatalog(private val metadataUrl: String = DEFAULT_METADATA_URL) {

    private var cachedMetadata: String? = null

    /** Returns raw metadata payload (cleaned of the XSSI security prefix). */
    fun metadata(): String? {
        cachedMetadata?.let { return it }
        val raw = HttpFetcher.fetchText(metadataUrl, accept = "application/json") ?: return null
        val cleaned = raw.removePrefix(XSSI_PREFIX)
        cachedMetadata = cleaned
        return cleaned
    }

    /**
     * Set of all official family names. Returns `null` if metadata cannot be fetched,
     * or `emptySet()` if metadata is fetched but contains no families.
     */
    fun families(): Set<String>? = metadata()?.let { parseFamilies(it) }

    /** Map of family name -> joined designer names (comma separated). */
    fun designers(): Map<String, String>? = metadata()?.let { parseDesigners(it) }

    /** Map of family name -> detected ScriptCategory (e.g. LATIN, JAPANESE, KOREAN, etc.). */
    fun scriptCategories(): Map<String, ScriptCategory>? = metadata()?.let { parseScriptCategories(it) }

    fun parseFamilies(metadata: String): Set<String> {
        return FAMILY_REGEX.findAll(metadata).map { it.groupValues[1] }.toCollection(LinkedHashSet())
    }

    fun parseDesigners(metadata: String): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        val families = FAMILY_REGEX.findAll(metadata).toList()

        families.forEachIndexed { index, familyMatch ->
            val blockStart = familyMatch.range.last + 1
            val blockEnd = families.getOrNull(index + 1)?.range?.first ?: metadata.length
            val block = metadata.substring(blockStart, blockEnd)

            val names = DESIGNERS_REGEX.find(block)?.groupValues?.get(1).orEmpty()
            val designers = QUOTED_REGEX.findAll(names).map { it.groupValues[1].trim() }.filter { it.isNotEmpty() }
            val joined = designers.joinToString(", ")
            if (joined.isNotEmpty()) {
                result[familyMatch.groupValues[1]] = joined
            }
        }
        return result
    }

    fun parseScriptCategories(metadata: String): Map<String, ScriptCategory> {
        val result = LinkedHashMap<String, ScriptCategory>()
        val families = FAMILY_REGEX.findAll(metadata).toList()

        families.forEachIndexed { index, familyMatch ->
            val familyName = familyMatch.groupValues[1]
            val blockStart = familyMatch.range.last + 1
            val blockEnd = families.getOrNull(index + 1)?.range?.first ?: metadata.length
            val block = metadata.substring(blockStart, blockEnd)

            val primaryScript = PRIMARY_SCRIPT_REGEX.find(block)?.groupValues?.get(1).orEmpty()
            val subsetsBlock = SUBSETS_REGEX.find(block)?.groupValues?.get(1).orEmpty()
            val subsets = QUOTED_REGEX.findAll(subsetsBlock).map { it.groupValues[1].lowercase() }.toSet()

            result[familyName] = detectCategory(primaryScript, subsets)
        }
        return result
    }

    private fun detectCategory(primaryScript: String, subsets: Set<String>): ScriptCategory {
        val primary = primaryScript.lowercase()
        return when {
            primary == "jpan" || "japanese" in subsets -> ScriptCategory.JAPANESE
            primary == "kore" || "korean" in subsets -> ScriptCategory.KOREAN
            primary in listOf("hans", "hant") || "chinese-simplified" in subsets || "chinese-traditional" in subsets -> ScriptCategory.CHINESE
            primary == "arab" || "arabic" in subsets -> ScriptCategory.ARABIC
            primary == "deva" || "devanagari" in subsets -> ScriptCategory.DEVANAGARI
            primary == "thai" || "thai" in subsets -> ScriptCategory.THAI
            primary == "khmr" || "khmer" in subsets -> ScriptCategory.KHMER
            primary == "hebr" || "hebrew" in subsets -> ScriptCategory.HEBREW
            else -> ScriptCategory.LATIN
        }
    }

    companion object {
        const val DEFAULT_METADATA_URL = "https://fonts.google.com/metadata/fonts"

        /** XSSI security prefix prepended by Google to JSON payloads. */
        const val XSSI_PREFIX = ")]}'"

        private val FAMILY_REGEX = """"family"\s*:\s*"([^"]+)"""".toRegex()
        private val DESIGNERS_REGEX = """"designers"\s*:\s*\[([^]]*)]""".toRegex()
        private val PRIMARY_SCRIPT_REGEX = """"primaryScript"\s*:\s*"([^"]+)"""".toRegex()
        private val SUBSETS_REGEX = """"subsets"\s*:\s*\[([^]]*)]""".toRegex()
        private val QUOTED_REGEX = """"([^"]+)"""".toRegex()
    }
}
