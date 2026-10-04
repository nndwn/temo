package io.github.nndwn.getgooglefont.compressit.fetcher

import io.github.nndwn.getgooglefont.compressit.net.HttpFetcher

class DesignerFetcher {

    fun fetchDesigner(fontName: String): String {
        // Method 1: Fetch from Google Fonts official metadata endpoint
        val metadataJson = fetchGoogleFontsMetadataJson()
        if (metadataJson != null) {
            val designer = extractDesignerFromMetadataList(fontName, metadataJson)
            if (!designer.isNullOrBlank()) {
                println("Dynamically fetched designer for $fontName from Google Fonts API: $designer")
                return designer
            }
        }

        // Method 2: Try dynamic network fetch from GitHub METADATA.json
        val cleanName = fontName.replace(" ", "").lowercase()
        val licenses = listOf("ofl", "apache", "ufl")
        val branches = listOf("main", "master")

        for (branch in branches) {
            for (license in licenses) {
                val urlString = "https://raw.githubusercontent.com/google/fonts/$branch/$license/$cleanName/METADATA.json"
                val jsonContent = HttpFetcher.fetchText(urlString) ?: continue
                val designer = extractDesignerFromJson(jsonContent)
                if (!designer.isNullOrBlank() && designer != "UNKNOWN") {
                    println("Dynamically fetched designer for $fontName from GitHub: $designer")
                    return designer
                }
            }
        }

        // Method 3: Try dynamic web fetch from Google Fonts specimen page
        val specimenUrl = "https://fonts.google.com/specimen/${fontName.replace(" ", "+")}"
        val pageContent = HttpFetcher.fetchText(specimenUrl)
        if (pageContent != null) {
            val designer = extractDesignerFromSpecimen(pageContent)
            if (!designer.isNullOrBlank()) {
                println("Dynamically fetched designer for $fontName from Specimen Page: $designer")
                return designer
            }
        }

        println("Could not dynamically fetch designer for $fontName, returning 'Unknown'")
        return "Unknown"
    }

    private var googleFontsMetadataCache: String? = null

    private fun fetchGoogleFontsMetadataJson(): String? {
        if (googleFontsMetadataCache != null) return googleFontsMetadataCache
        val rawContent = HttpFetcher.fetchText("https://fonts.google.com/metadata/fonts", accept = "application/json")
        println("Fetched Google Fonts metadata rawContent length: ${rawContent?.length ?: 0}")
        if (rawContent != null) {
            val cleanContent = if (rawContent.startsWith(")]}'")) rawContent.substring(4) else rawContent
            googleFontsMetadataCache = cleanContent
        }
        return googleFontsMetadataCache
    }

    fun extractDesignerFromMetadataList(fontName: String, metadataJson: String): String? {
        val familyRegex = """"family"\s*:\s*"$fontName"""".toRegex(RegexOption.IGNORE_CASE)
        val match = familyRegex.find(metadataJson)
        if (match == null) {
            println("familyRegex did not match '$fontName'")
            return null
        }

        val familyIndex = match.range.first
        val snippet = metadataJson.substring(familyIndex, (familyIndex + 4000).coerceAtMost(metadataJson.length))

        val designerMatch = """"designer"\s*:\s*"([^"]+)"""".toRegex().find(snippet)
            ?: """"designers"\s*:\s*\[\s*"([^"]+)"""".toRegex().find(snippet)
            ?: """"designer":\{"name":"([^"]+)"""".toRegex().find(snippet)
            ?: """"designer"\s*:\s*\{[^}]*"name"\s*:\s*"([^"]+)"""".toRegex().find(snippet)

        return designerMatch?.groupValues?.get(1)?.trim()
    }

    fun extractDesignerFromJson(jsonContent: String): String? {
        val regex = """"designer"\s*:\s*"([^"]+)"""".toRegex()
        val match = regex.find(jsonContent)
        return match?.groupValues?.get(1)?.trim()
    }

    fun extractDesignerFromSpecimen(pageHtml: String): String? {
        val regexes = listOf(
            """"designer"\s*:\s*"([^"]+)"""".toRegex(),
            """"designer":\{"name":"([^"]+)"""".toRegex(),
            """"author"\s*:\s*"([^"]+)"""".toRegex(),
            """Designed by\s+<[^>]+>([^<]+)</""".toRegex(RegexOption.IGNORE_CASE),
            """Designed by\s+([A-Z][a-zA-Z\s]+)""".toRegex()
        )
        for (regex in regexes) {
            val match = regex.find(pageHtml)
            if (match != null) {
                val found = match.groupValues[1].trim()
                if (found.isNotEmpty()) {
                    println("Found designer with regex ${regex.pattern}: $found")
                    return found
                }
            }
        }
        return null
    }
}
