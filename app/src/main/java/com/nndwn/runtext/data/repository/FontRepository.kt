package com.nndwn.runtext.data.repository

import android.content.Context
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.FontData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

@Singleton
class FontRepository @Inject constructor(@ApplicationContext private val context: Context) {
  private val _fonts = MutableStateFlow<List<FontData>>(emptyList())
  val fonts: StateFlow<List<FontData>> = _fonts.asStateFlow()

  private val json = Json {
    ignoreUnknownKeys = true
  }

  init {
    loadFonts()
  }

  private fun loadFonts() {
    try {
      val jsonString = context.resources.openRawResource(R.raw.fonts).bufferedReader().use { it.readText() }
      val allFonts = json.decodeFromString<List<FontData>>(jsonString)
      _fonts.value = allFonts
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun getFontById(id: String): FontData? {
    return _fonts.value.find { it.idFont == id }
  }
}
