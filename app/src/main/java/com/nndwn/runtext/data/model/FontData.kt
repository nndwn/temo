package com.nndwn.runtext.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScriptCategory(val displayName: String) {
  LATIN("Latin"),
  ARABIC("Arabic (عربي)"),
  JAPANESE("Japanese (日本語)"),
  CHINESE("Chinese (中文)"),
  KOREAN("Korean (한국어)"),
  THAI("Thai (ไทย)"),
  DEVANAGARI("Devanagari (हिन्दी)"),
  KHMER("Khmer (ភាសាខ្មែរ)"),
  HEBREW("Hebrew (עברית)"),
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
