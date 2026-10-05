package io.github.nndwn.getgooglefont.compressit.fetcher

import io.github.nndwn.getgooglefont.compressit.net.HttpFetcher
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class FontDownloader {

    fun downloadTtfBytes(googleFontName: String): ByteArray? {
        val encodedFontName = googleFontName.replace(" ", "+")
        val cssUrl = "https://fonts.googleapis.com/css2?family=$encodedFontName"

        // NOTE: the User-Agent decides which format Google serves. A modern browser UA gets
        // per-unicode-range WOFF2 subsets, which Android's Typeface CANNOT load. A UA that does
        // not advertise web-font support gets a single, complete TrueType file instead.
        val cssContent =
            HttpFetcher.fetchText(cssUrl, userAgent = TRUETYPE_USER_AGENT, accept = CSS_ACCEPT)
                ?: return null

        val ttfUrl = extractTtfUrlFromCss(cssContent) ?: run {
            println("   No TrueType/OpenType URL in CSS for $googleFontName")
            return null
        }

        val bytes = downloadBytes(ttfUrl) ?: return null
        if (!isSupportedFontFormat(bytes)) {
            println(
                "   Rejected '${ttfUrl.substringAfterLast('/')}' for $googleFontName: not a raw " +
                    "TrueType/OpenType font (magic=${magicTag(bytes)}). Android Typeface cannot read " +
                    "web fonts such as WOFF/WOFF2, so this font would silently fail to render.",
            )
            return null
        }
        return bytes
    }

    /**
     * Extracts the font file URL from a Google Fonts CSS response.
     *
     * Only real `.ttf` / `.otf` are returned on purpose. Google serves WOFF2 subsets to modern
     * browsers and those bytes are unusable by Android `Typeface`, so they are never selected.
     */
    fun extractTtfUrlFromCss(cssContent: String): String? {
        val urls = FONT_URL_REGEX.findAll(cssContent).map { it.groupValues[2] }.toList()
        if (urls.isEmpty()) return null

        return urls.firstOrNull { it.endsWith(".ttf", ignoreCase = true) }
            ?: urls.firstOrNull { it.endsWith(".otf", ignoreCase = true) }
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

        /**
         * Deliberately *not* a modern browser User-Agent (see [downloadTtfBytes]). Google Fonts
         * only returns a single, complete TrueType file when the UA does not announce WOFF/WOFF2
         * support — which is exactly what Android understands.
         */
        const val TRUETYPE_USER_AGENT = "Java/1.8.0_292"

        private val FONT_URL_REGEX = """url\((['"]?)(https?://[^)]+\.(?:ttf|otf|woff2))\1\)""".toRegex()

        private const val TAG_TRUETYPE = 0x00010000
        private const val TAG_OTTO = 0x4F54544F // 'OTTO' — CFF/OpenType
        private const val TAG_TRUE = 0x74727565 // 'true' — legacy Apple TrueType
        private const val TAG_TTCF = 0x74746366 // 'ttcf' — TrueType collection

        /** `true` when [bytes] is a font container that Android's `Typeface` can actually load. */
        fun isSupportedFontFormat(bytes: ByteArray): Boolean {
            val tag = magicTagValue(bytes) ?: return false
            return tag == TAG_TRUETYPE || tag == TAG_OTTO || tag == TAG_TRUE || tag == TAG_TTCF
        }

        /** Readable view of the first four bytes (e.g. `wOF2`) for diagnostics. */
        fun magicTag(bytes: ByteArray): String {
            if (bytes.size < 4) return "<short>"
            return buildString {
                for (i in 0 until 4) {
                    val c = bytes[i].toInt() and 0xFF
                    append(if (c in 32..126) c.toChar() else '.')
                }
            }
        }

        private fun magicTagValue(bytes: ByteArray): Int? {
            if (bytes.size < 4) return null
            var tag = 0
            for (i in 0 until 4) {
                tag = (tag shl 8) or (bytes[i].toInt() and 0xFF)
            }
            return tag
        }
    }
}
