package com.nndwn.runtext.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.nndwn.runtext.data.font.FontBundleEntryPoint
import com.nndwn.runtext.data.font.FontBundleRepository
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.ScriptCategory
import com.nndwn.runtext.ui.LocalReadyScripts
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.ConcurrentHashMap

private val localFontCache = ConcurrentHashMap<Int, FontFamily>()
private val fileFontCache = ConcurrentHashMap<String, FontFamily>()

private fun fontBundleRepository(context: Context): FontBundleRepository =
  EntryPointAccessors
    .fromApplication(context.applicationContext, FontBundleEntryPoint::class.java)
    .fontBundleRepository()

/**
 * Resolves [fontData] to a [FontFamily] from its bundled resource font or from the
 * already-downloaded script bundle.
 *
 * Returns [FontFamily.Default] while the font is not available locally yet. Pure function —
 * it never triggers a download. Use [rememberFontFamily] from composables so the UI
 * re-resolves automatically once the bundle finishes downloading.
 */
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

  val repository = fontBundleRepository(context)
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

  return FontFamily.Default
}

/**
 * Compose-aware wrapper around [fontFamilyFor] which:
 * - kicks off the on-demand download of [fontData]'s script bundle, and
 * - recomposes (re-resolving the family) as soon as that bundle becomes ready.
 *
 * Reading [LocalReadyScripts] here is what makes preview/display swap from the fallback
 * font to the real typeface right after the download completes, with no manual refresh.
 */
@Composable
fun rememberFontFamily(
  fontData: FontData?,
  fallback: FontFamily = FontFamily.Default,
): FontFamily {
  val context = LocalContext.current
  val readyScripts = LocalReadyScripts.current
  val script = fontData?.scriptCategory

  LaunchedEffect(script, readyScripts) {
    if (script == null || readyScripts.contains(script)) return@LaunchedEffect
    fontBundleRepository(context).ensureBundle(script)
  }

  return remember(fontData, readyScripts) {
    fontData?.let { fontFamilyFor(context, it) } ?: fallback
  }
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
