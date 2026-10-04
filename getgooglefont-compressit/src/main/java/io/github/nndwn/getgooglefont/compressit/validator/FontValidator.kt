package io.github.nndwn.getgooglefont.compressit.validator

/** Satu nama font yang tidak ditemukan di katalog Google Fonts. */
data class InvalidFont(val input: String, val suggestion: String?)

/**
 * Hasil validasi daftar nama font.
 *
 * [valid] berisi nama **kanonik** (kapitalisasi resmi Google Fonts) dengan urutan
 * yang sama seperti input, sehingga bisa dipetakan balik per index.
 */
data class ValidationReport(
    val valid: List<String>,
    val invalid: List<InvalidFont>,
) {
    val isValid: Boolean get() = invalid.isEmpty()

    fun toMessage(): String {
        if (isValid) return "Validasi berhasil: ${valid.size} nama font semuanya ada di Google Fonts."

        return buildString {
            appendLine("Validasi GAGAL: ${invalid.size} nama font tidak ditemukan di katalog Google Fonts:")
            invalid.forEach { font ->
                append("  - \"${font.input}\"")
                if (font.suggestion != null) {
                    append("  ->  maksudnya \"${font.suggestion}\"?")
                }
                appendLine()
            }
        }.trimEnd()
    }
}

/**
 * Memvalidasi nama family font terhadap daftar family resmi Google Fonts.
 *
 * Pencocokan bersifat toleran: huruf besar/kecil, spasi, underscore, dan tanda hubung
 * diabaikan (`open_sans` == `Open Sans` == `opensans`). Nama yang cocok selalu
 * dikembalikan dalam ejaan resmi katalog.
 */
class FontValidator(availableFamilies: Collection<String>) {

    private val canonicalByKey: Map<String, String> =
        availableFamilies.associateBy { normalize(it) }

    private val canonicalFamilies: List<String> = availableFamilies.toList()

    /** Ejaan resmi dari [name], atau `null` bila tidak ada di katalog. */
    fun canonicalOf(name: String): String? = canonicalByKey[normalize(name)]

    fun validate(names: List<String>): ValidationReport {
        val valid = mutableListOf<String>()
        val invalid = mutableListOf<InvalidFont>()

        names.forEach { name ->
            val canonical = canonicalOf(name)
            if (canonical != null) {
                valid += canonical
            } else {
                invalid += InvalidFont(input = name, suggestion = suggest(name))
            }
        }
        return ValidationReport(valid = valid, invalid = invalid)
    }

    /** Saran nama terdekat, atau `null` bila tidak ada yang cukup mirip. */
    fun suggest(name: String): String? {
        val target = normalize(name)
        if (target.isEmpty()) return null

        var best: String? = null
        var bestDistance = MAX_SUGGESTION_DISTANCE + 1

        canonicalFamilies.forEach { family ->
            val candidate = normalize(family)
            if (candidate == target) return@forEach
            val distance = levenshtein(target, candidate, maxOf(bestDistance - 1, 1))
            if (distance in 1..MAX_SUGGESTION_DISTANCE && distance < bestDistance) {
                bestDistance = distance
                best = family
            }
        }
        return best
    }

    companion object {
        private const val MAX_SUGGESTION_DISTANCE = 3

        /** Kunci pencocokan: huruf kecil tanpa spasi/underscore/tanda hubung. */
        fun normalize(raw: String): String = raw.lowercase().filter { it.isLetterOrDigit() }

        /** Levenshtein dengan batas [max]; hasil > [max] boleh tidak akurat. */
        fun levenshtein(a: String, b: String, max: Int): Int {
            if (a == b) return 0
            if (a.isEmpty()) return b.length
            if (b.isEmpty()) return a.length

            var previous = IntArray(b.length + 1) { it }
            var current = IntArray(b.length + 1)

            for (i in 1..a.length) {
                current[0] = i
                var rowMin = current[0]

                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    current[j] = minOf(current[j - 1] + 1, previous[j] + 1, previous[j - 1] + cost)
                    if (current[j] < rowMin) rowMin = current[j]
                }

                if (rowMin > max) return rowMin
                val swap = previous
                previous = current
                current = swap
            }
            return previous[b.length]
        }
    }
}
