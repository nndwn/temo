package io.github.nndwn.getgooglefont.compressit.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScriptCategory {
    LATIN,
    ARABIC,
    JAPANESE,
    CHINESE,
    KOREAN,
    THAI,
    DEVANAGARI,
    KHMER,
    HEBREW
}
