package io.github.nndwn.getgooglefont.compressit.fetcher

import io.github.nndwn.getgooglefont.compressit.net.HttpFetcher
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class FontDownloader {

    fun downloadTtfBytes(googleFontName: String): ByteArray? {
        val encodedFontName = googleFontName.replace(" ", "+")
        val cssUrl = "https://fonts.googleapis.com/css2?family=$encodedFontName"

        val cssContent =
            HttpFetcher.fetchText(cssUrl, userAgent = HttpFetcher.FIREFOX_ANDROID_USER_AGENT, accept = CSS_ACCEPT)
                ?: HttpFetcher.fetchText(cssUrl, accept = CSS_ACCEPT)
                ?: return null

        val ttfUrl = extractTtfUrlFromCss(cssContent) ?: return null
        return downloadBytes(ttfUrl)
    }

    /**
     * Extracts font file URL from Google Fonts CSS response.
     *
     * Prefers `.ttf` / `.otf` (most compatible with Android `Typeface`),
     * fallback to `.woff2` as last resort.
     */
    fun extractTtfUrlFromCss(cssContent: String): String? {
        val urls = FONT_URL_REGEX.findAll(cssContent).map { it.groupValues[2] }.toList()
        if (urls.isEmpty()) return null

        return urls.firstOrNull { it.endsWith(".ttf", ignoreCase = true) }
            ?: urls.firstOrNull { it.endsWith(".otf", ignoreCase = true) }
            ?: urls.firstOrNull { it.endsWith(".woff2", ignoreCase = true) }
    }

    private fun downloadBytes(urlString: String): ByteArray? {
        return try {
            val url: URL = URI.create(urlString).toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { it.readBytes() }
            } else {
                println("   HTTP ${connection.responseCode} for $urlString")
                null
            }
        } catch (e: Exception) {
            println("   HTTP error for $urlString: ${e.message}")
            null
        }
    }

    companion object {
        private const val CSS_ACCEPT = "text/css,*/*;q=0.1"

        private val FONT_URL_REGEX = """url\((['"]?)(https?://[^)]+\.(?:ttf|otf|woff2))\1\)""".toRegex()
    }
}
