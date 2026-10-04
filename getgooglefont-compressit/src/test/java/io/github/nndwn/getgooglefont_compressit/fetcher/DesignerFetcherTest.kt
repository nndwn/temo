package io.github.nndwn.getgooglefont.compressit.fetcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DesignerFetcherTest {

    private val fetcher = DesignerFetcher()

    @Test
    fun testExtractDesignerFromJson() {
        val sampleJson = """
            {
              "name": "Roboto",
              "designer": "Christian Robertson",
              "license": "OFL",
              "category": "SANS_SERIF"
            }
        """.trimIndent()

        val designer = fetcher.extractDesignerFromJson(sampleJson)
        assertEquals("Christian Robertson", designer)
    }

    @Test
    fun testFetchDesignerOnlineForRoboto() {
        val designer = fetcher.fetchDesigner("Roboto")
        println("Roboto designer fetched: $designer")
        assertNotNull("Designer should be fetched for Roboto", designer)
        assertEquals("Christian Robertson", designer)
    }
}
