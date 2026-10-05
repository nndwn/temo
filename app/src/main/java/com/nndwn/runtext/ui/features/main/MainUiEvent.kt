package com.nndwn.runtext.ui.features.main

import androidx.annotation.StringRes
import com.nndwn.runtext.data.model.AppMode
import com.nndwn.runtext.data.model.ScriptCategory
import com.nndwn.runtext.data.model.TextColorType
import com.nndwn.runtext.data.model.TextConfig

sealed interface MainUiEvent {

  // ── Navigation Events ──
  data object NavigateToDisplay : MainUiEvent

  data object NavigateBack : MainUiEvent

  data class Toast(@field:StringRes val message: Int) : MainUiEvent

  data object ExportAndShareVideo : MainUiEvent

  // ── General / Text Input ──
  data class ApplyPreset(val settings: TextConfig) : MainUiEvent

  data class UpdateText(val text: String) : MainUiEvent

  data object ClearText : MainUiEvent

  data class UpdateMode(val mode: AppMode) : MainUiEvent

  data class UpdateSpeed(val speed: Float) : MainUiEvent

  data class UpdateBgColor(val colorArgb: Long) : MainUiEvent

  data class UpdateMirrorMode(val mirror: Boolean) : MainUiEvent

  data class UpdateTextMove(val move : Boolean) : MainUiEvent
  data class UpdateBlinkMode(val blink: Boolean) : MainUiEvent



  // ── Text Style Events ──
  data class UpdateTextColor(val colorArgb: Long) : MainUiEvent

  data class UpdateFontTypeCategory(val type : ScriptCategory) : MainUiEvent

  data class UpdateTextColorType(val type: TextColorType) : MainUiEvent

  data class UpdateGradientColors(val colors: List<Long>) : MainUiEvent

  data class UpdateGradientDistance(val distance: Float) : MainUiEvent

  data class ToggleGradientHorizontal(val isHorizontal: Boolean) : MainUiEvent

  data class UpdateFontType(val fontId: String) : MainUiEvent

  data class UpdateGoogleFontName(val fontName: String) : MainUiEvent

  // ── Stroke Events ──
  data class ToggleStroke(val isEnabled: Boolean) : MainUiEvent

  data class UpdateStrokeWidth(val width: Float) : MainUiEvent

  data class UpdateStrokeColor(val colorArgb: Long) : MainUiEvent

  // ── Shadow Events ──
  data class ToggleShadow(val isEnabled: Boolean) : MainUiEvent

  data class UpdateShadowColor(val colorArgb: Long) : MainUiEvent

  data class UpdateShadowRadius(val radius: Float) : MainUiEvent

  data class UpdateShadowRotation(val rotation: Float) : MainUiEvent

  data class UpdateMorseWpm(val wpm: Int) : MainUiEvent

  data class UpdateBgColorMorse(val colorArgb: Long) : MainUiEvent

  data class UpdateFlashScreen(val isFlashScreen: Boolean) : MainUiEvent

  data class UpdateTorchEnabled(val isTorchEnabled: Boolean) : MainUiEvent

  data class UpdateSoundEnabled(val isSoundEnabled: Boolean) : MainUiEvent

  data class UpdateVibrateEnabled(val isVibrateEnabled: Boolean) : MainUiEvent
}
