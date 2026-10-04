package io.github.nndwn.getgooglefont.compressit.catalog

import io.github.nndwn.getgooglefont.compressit.model.ScriptCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleFontsCatalogTest {

    private val catalog = GoogleFontsCatalog()

    private val sampleMetadata = """
        {
          "familyMetadataList": [
            {
              "family": "Roboto",
              "displayName": null,
              "category": "Sans Serif",
              "subsets": [ "latin" ],
              "fonts": { "400": { "thickness": 2 } },
              "designers": [ "Christian Robertson", "ParaType" ],
              "lastModified": "2025-09-16"
            },
            {
              "family": "Angkor",
              "displayName": null,
              "category": "Display",
              "subsets": [ "khmer" ],
              "fonts": { "400": { "thickness": null } },
              "designers": [ "Danh Hong" ],
              "lastModified": "2025-09-08"
            },
            {
              "family": "ZCOOL KuaiLe",
              "displayName": null,
              "subsets": [ "chinese-simplified" ],
              "designers": [ "Liu Bingke", "Yang Kang", "Wu Shaojie" ],
              "lastModified": "2025-09-04"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `parseFamilies mengambil semua nama family`() {
        assertEquals(setOf("Roboto", "Angkor", "ZCOOL KuaiLe"), catalog.parseFamilies(sampleMetadata))
    }

    @Test
    fun `parseDesigners menggabungkan desainer per family`() {
        val designers = catalog.parseDesigners(sampleMetadata)

        assertEquals("Christian Robertson, ParaType", designers["Roboto"])
        assertEquals("Danh Hong", designers["Angkor"])
        assertEquals("Liu Bingke, Yang Kang, Wu Shaojie", designers["ZCOOL KuaiLe"])
    }

    @Test
    fun `parseScriptCategories mendeteksi ScriptCategory dari subsets`() {
        val scripts = catalog.parseScriptCategories(sampleMetadata)

        assertEquals(ScriptCategory.LATIN, scripts["Roboto"])
        assertEquals(ScriptCategory.KHMER, scripts["Angkor"])
        assertEquals(ScriptCategory.CHINESE, scripts["ZCOOL KuaiLe"])
    }

    @Test
    fun `metadata live memuat font yang kita pakai dan menolak typo`() {
        val families = catalog.families()
        assertNotNull("Katalog Google Fonts harus bisa diambil", families)

        val all = families!!
        assertTrue("Roboto harus ada", all.contains("Roboto"))
        assertTrue("Angkor harus ada", all.contains("Angkor"))
        assertFalse("Ankor tidak boleh ada (typo)", all.contains("Ankor"))
        assertTrue("Katalog harus berisi banyak family", all.size > 1000)
    }

    @Test
    fun `designer live untuk Roboto sesuai katalog`() {
        val designers = catalog.designers()
        assertNotNull(designers)

        val roboto = designers!!["Roboto"]
        assertNotNull(roboto)
        assertTrue(roboto!!.contains("Christian Robertson"))
    }
}
