package io.github.nndwn.getgooglefont.compressit.catalog

import io.github.nndwn.getgooglefont.compressit.net.HttpFetcher

/**
 * Katalog resmi Google Fonts, diambil dari endpoint metadata publik.
 *
 * Dipakai sebagai sumber kebenaran (source of truth) untuk **memvalidasi** nama family
 * sebelum font diunduh, sehingga tidak ada nama font yang salah / tidak ada.
 */
class GoogleFontsCatalog(private val metadataUrl: String = DEFAULT_METADATA_URL) {

    private var cachedMetadata: String? = null

    /** Mengembalikan isi metadata mentah (sudah dibersihkan dari prefix XSSI). */
    fun metadata(): String? {
        cachedMetadata?.let { return it }
        val raw = HttpFetcher.fetchText(metadataUrl, accept = "application/json") ?: return null
        val cleaned = raw.removePrefix(XSSI_PREFIX)
        cachedMetadata = cleaned
        return cleaned
    }

    /**
     * Semua nama family resmi. Mengembalikan `null` bila metadata tidak bisa diambil,
     * dan `emptySet()` bila metadata terambil tapi tidak memuat family apa pun.
     */
    fun families(): Set<String>? = metadata()?.let { parseFamilies(it) }

    /** Peta nama family -> daftar desainer (digabung dengan ", "). */
    fun designers(): Map<String, String>? = metadata()?.let { parseDesigners(it) }

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

    companion object {
        const val DEFAULT_METADATA_URL = "https://fonts.google.com/metadata/fonts"

        /** Prefix keamanan XSSI yang dipakai Google di depan payload JSON. */
        const val XSSI_PREFIX = ")]}'"

        private val FAMILY_REGEX = """"family"\s*:\s*"([^"]+)"""".toRegex()
        private val DESIGNERS_REGEX = """"designers"\s*:\s*\[([^]]*)]""".toRegex()
        private val QUOTED_REGEX = """"([^"]+)"""".toRegex()
    }
}
