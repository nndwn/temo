package io.github.nndwn.getgooglefont.compressit.net

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/** Perkakas HTTP sederhana yang dipakai bersama oleh fetcher di modul ini. */
object HttpFetcher {

    const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    const val FIREFOX_ANDROID_USER_AGENT =
        "Mozilla/5.0 (Android 14; Mobile; rv:100.0) Gecko/100.0 Firefox/100.0"

    const val HTML_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"

    fun fetchText(
        urlString: String,
        userAgent: String = DESKTOP_USER_AGENT,
        accept: String = HTML_ACCEPT,
        logPrefix: String = "   ",
    ): String? {
        return try {
            val url: URL = URI.create(urlString).toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 20_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", userAgent)
            connection.setRequestProperty("Accept", accept)
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
            connection.instanceFollowRedirects = true

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                println("${logPrefix}HTTP $responseCode for $urlString")
                null
            }
        } catch (e: Exception) {
            println("${logPrefix}HTTP error for $urlString: ${e.message}")
            null
        }
    }
}
