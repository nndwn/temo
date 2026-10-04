package com.nndwn.runtext.data.model

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
  HEBREW,
}

@Serializable
data class FontData(
  val idFont: String,
  val displayName: String,
  val scriptCategory: ScriptCategory,
  val localResName: String? = null,
  val googleFontName: String? = null,
  val designer: String? = null,
)
