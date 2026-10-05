package io.github.nndwn.getgooglefont.compressit.fetcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FontDownloaderTest {

    private val downloader = FontDownloader()

    @Test
    fun testExtractTtfUrlFromCss() {
        val cssSample = """
            @font-face {
              font-family: 'Roboto';
              font-style: normal;
              font-weight: 400;
              src: url(https://fonts.gstatic.com/s/roboto/v30/KFOmCnqEu92Fr1Mu4mxP.ttf) format('truetype');
            }
        """.trimIndent()

        val url = downloader.extractTtfUrlFromCss(cssSample)
        assertNotNull(url)
        assertTrue(url!!.endsWith(".ttf"))
    }

    @Test
    fun `extractTtfUrlFromCss mengabaikan WOFF2`() {
        val woff2OnlyCss = """
            /* arabic */
            @font-face {
              font-family: 'Cairo';
              src: url(https://fonts.gstatic.com/s/cairo/v31/abc.woff2) format('woff2');
              unicode-range: U+0600-06FF;
            }
        """.trimIndent()

        assertNull(downloader.extractTtfUrlFromCss(woff2OnlyCss))
    }

    @Test
    fun `isSupportedFontFormat hanya menerima TrueType dan OpenType`() {
        val trueType = byteArrayOf(0x00, 0x01, 0x00, 0x00, 0x00, 0x10)
        val otto = "OTTO".toByteArray(Charsets.US_ASCII) + byteArrayOf(0x00)
        val woff2 = "wOF2".toByteArray(Charsets.US_ASCII) + byteArrayOf(0x00)
        val woff = "wOFF".toByteArray(Charsets.US_ASCII) + byteArrayOf(0x00)

        assertTrue(FontDownloader.isSupportedFontFormat(trueType))
        assertTrue(FontDownloader.isSupportedFontFormat(otto))
        assertFalse(FontDownloader.isSupportedFontFormat(woff2))
        assertFalse(FontDownloader.isSupportedFontFormat(woff))
        assertFalse(FontDownloader.isSupportedFontFormat(byteArrayOf(0x00, 0x01)))

        assertEquals("wOF2", FontDownloader.magicTag(woff2))
    }
}
