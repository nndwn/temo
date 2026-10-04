package io.github.nndwn.getgooglefont.compressit.validator

/** Represents a font name that was not found in the Google Fonts catalog. */
data class InvalidFont(val input: String, val suggestion: String?)

/**
 * Result of validating a list of font names.
 *
 * [valid] contains canonical names (official Google Fonts capitalization) in the same
 * order as the input, allowing 1:1 mapping by index.
 */
data class ValidationReport(
    val valid: List<String>,
    val invalid: List<InvalidFont>,
) {
    val isValid: Boolean get() = invalid.isEmpty()

    fun toMessage(): String {
        if (isValid) return "Validation succeeded: all ${valid.size} font names exist in Google Fonts."

        return buildString {
            appendLine("Validation FAILED: ${invalid.size} font name(s) not found in Google Fonts catalog:")
            invalid.forEach { font ->
                append("  - \"${font.input}\"")
                if (font.suggestion != null) {
                    append("  ->  did you mean \"${font.suggestion}\"?")
                }
                appendLine()
            }
        }.trimEnd()
    }
}

/**
 * Validates font family names against the official Google Fonts catalog.
 *
 * Matching is lenient: case, spaces, underscores, and hyphens are ignored
 * (`open_sans` == `Open Sans` == `opensans`). Matched names are always returned
 * in their official catalog spelling.
 */
class FontValidator(availableFamilies: Collection<String>) {

    private val canonicalByKey: Map<String, String> =
        availableFamilies.associateBy { normalize(it) }

    private val canonicalFamilies: List<String> = availableFamilies.toList()

    /** Official spelling of [name], or `null` if not present in catalog. */
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

    /** Nearest suggested name, or `null` if no close match is found. */
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

        /** Matching key: lowercase alphanumeric characters only. */
        fun normalize(raw: String): String = raw.lowercase().filter { it.isLetterOrDigit() }

        /** Levenshtein distance bounded by [max]; results > [max] may be approximate. */
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
