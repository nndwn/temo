package io.github.nndwn.getgooglefont.compressit.fetcher

import org.junit.Assert.assertNotNull
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
}
