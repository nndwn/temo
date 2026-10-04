package io.github.nndwn.getgooglefont.compressit.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

class FontIdMapperTest {

    @Test
    fun testToIdFont() {
        assertEquals("ROBOTO", FontIdMapper.toIdFont("Roboto"))
        assertEquals("OPEN_SANS", FontIdMapper.toIdFont("Open Sans"))
        assertEquals("DELA_GOTHIC_ONE", FontIdMapper.toIdFont("Dela Gothic One"))
        assertEquals("PRESS_START_2P", FontIdMapper.toIdFont("Press Start 2P"))
        assertEquals("DOT_GOTHIC", FontIdMapper.toIdFont("DotGothic16"))
        assertEquals("ABRIL_FATFACE", FontIdMapper.toIdFont("Abril Fatface"))
    }
}
