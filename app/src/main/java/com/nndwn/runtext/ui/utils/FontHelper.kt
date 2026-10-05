package com.nndwn.runtext.ui.utils

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.nndwn.runtext.data.font.FontBundleEntryPoint
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.ScriptCategory
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.ConcurrentHashMap

private val localFontCache = ConcurrentHashMap<Int, FontFamily>()
private val fileFontCache = ConcurrentHashMap<String, FontFamily>()

fun fontFamilyFor(context: Context, fontData: FontData): FontFamily {
  val localResName = fontData.localResName
  if (!localResName.isNullOrEmpty()) {
    val resId = context.resources.getIdentifier(localResName, "font", context.packageName)
    if (resId != 0) {
      return localFontCache.getOrPut(resId) {
        FontFamily(Font(resId))
      }
    }
  }

  val repository = EntryPointAccessors
    .fromApplication(context.applicationContext, FontBundleEntryPoint::class.java)
    .fontBundleRepository()

  val script = fontData.scriptCategory
  val file = repository.cachedFontFile(script, fontData.idFont)
  if (file != null) {
    val path = file.absolutePath
    fileFontCache[path]?.let { return it }
    val family = repository.fontFamilyFromCache(script, fontData.idFont)
    if (family != null) {
      fileFontCache[path] = family
      return family
    }
  }

  repository.ensureBundle(script)
  return FontFamily.Default
}

fun String.detectPrimaryScript(): ScriptCategory {
  if (isEmpty()) return ScriptCategory.LATIN

  for (i in indices) {
    val char = this[i]
    if (char.code in 0x0000..0x007F) continue

    val block = Character.UnicodeBlock.of(char) ?: continue

    val category =
      when (block) {
        Character.UnicodeBlock.ARABIC,
        Character.UnicodeBlock.ARABIC_SUPPLEMENT,
        Character.UnicodeBlock.ARABIC_EXTENDED_A,
        Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A,
        Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B -> ScriptCategory.ARABIC

        Character.UnicodeBlock.HIRAGANA,
        Character.UnicodeBlock.KATAKANA,
        Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS -> ScriptCategory.JAPANESE

        Character.UnicodeBlock.HANGUL_SYLLABLES,
        Character.UnicodeBlock.HANGUL_JAMO,
        Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO -> ScriptCategory.KOREAN

        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A -> ScriptCategory.CHINESE

        Character.UnicodeBlock.THAI -> ScriptCategory.THAI
        Character.UnicodeBlock.DEVANAGARI -> ScriptCategory.DEVANAGARI
        Character.UnicodeBlock.KHMER -> ScriptCategory.KHMER
        Character.UnicodeBlock.HEBREW -> ScriptCategory.HEBREW
        else -> null
      }

    if (category != null) return category
  }
  return ScriptCategory.LATIN
}
