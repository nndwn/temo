package io.github.nndwn.getgooglefont.compressit.validator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FontValidatorTest {

    private val families = listOf("Roboto", "Open Sans", "Angkor", "Modak", "ZCOOL KuaiLe")
    private val validator = FontValidator(families)

    @Test
    fun `nama yang persis sama dianggap valid`() {
        assertEquals("Roboto", validator.canonicalOf("Roboto"))
        assertEquals("ZCOOL KuaiLe", validator.canonicalOf("ZCOOL KuaiLe"))
    }

    @Test
    fun `huruf besar kecil spasi dan underscore diabaikan`() {
        assertEquals("Open Sans", validator.canonicalOf("open sans"))
        assertEquals("Open Sans", validator.canonicalOf("open_sans"))
        assertEquals("Open Sans", validator.canonicalOf("OPENSANS"))
        assertEquals("ZCOOL KuaiLe", validator.canonicalOf("zcool kuaile"))
    }

    @Test
    fun `nama yang tidak ada mengembalikan null`() {
        assertNull(validator.canonicalOf("Ankor"))
        assertNull(validator.canonicalOf("Font Yang Tidak Ada"))
    }

    @Test
    fun `validasi mengembalikan ejaan kanonik sesuai urutan input`() {
        val report = validator.validate(listOf("roboto", "open_sans", "MODAK"))

        assertTrue(report.isValid)
        assertEquals(listOf("Roboto", "Open Sans", "Modak"), report.valid)
        assertTrue(report.invalid.isEmpty())
    }

    @Test
    fun `validasi menandai nama salah beserta saran terdekat`() {
        val report = validator.validate(listOf("Roboto", "Ankor"))

        assertFalse(report.isValid)
        assertEquals(listOf("Roboto"), report.valid)
        assertEquals(1, report.invalid.size)
        assertEquals("Ankor", report.invalid.first().input)
        assertEquals("Angkor", report.invalid.first().suggestion)
    }

    @Test
    fun `saran null bila tidak ada nama yang mirip`() {
        assertNull(validator.suggest("Qwertyuiop"))
    }

    @Test
    fun `pesan laporan menyebut nama salah dan saran`() {
        val message = validator.validate(listOf("Ankor")).toMessage()

        assertTrue(message.contains("Validation FAILED"))
        assertTrue(message.contains("Ankor"))
        assertTrue(message.contains("Angkor"))
    }

    @Test
    fun `levenshtein menghitung jarak edit dasar`() {
        assertEquals(0, FontValidator.levenshtein("ankor", "ankor", 5))
        assertEquals(1, FontValidator.levenshtein("ankor", "angkor", 5))
        assertEquals(3, FontValidator.levenshtein("kitten", "sitting", 5))
    }
}
