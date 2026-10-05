package com.nndwn.runtext.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.nndwn.runtext.R
import com.nndwn.runtext.ui.theme.Palette
import com.nndwn.runtext.ui.theme.toArgbLong
import kotlinx.serialization.Serializable

/** Enum for app display mode selection. */
@Serializable
enum class AppMode(@param:StringRes val displayName: Int, @param:DrawableRes val icon: Int) {
  RUNNING_TEXT(R.string.btn_text_running_text, R.drawable.ic_runtext),
  MORSE_CODE(R.string.btn_text_morse_code, R.drawable.ic_flash),
}

@Serializable
enum class TextColorType(@param:StringRes val displayName: Int, @param:DrawableRes val icon: Int) {
  SOLID(R.string.set_config_text_color_type_solid, R.drawable.ic_color_solid),
  GRADIENT(R.string.set_config_text_color_type_gradient, R.drawable.ic_color_gradient),
}

@Serializable
data class AppSettings(
  val lastText: String = "",
  val mode: AppMode = AppMode.RUNNING_TEXT,
  val textConfig: TextConfig = TextConfig(),
  val morseConfig: MorseConfig = MorseConfig(),
)

@Serializable
data class TextConfig(
  val speed: Float = 150f,
  val bgColorArgb: Long = Palette.White.toArgbLong(),
  val isMirrorMode: Boolean = false,
  val isBlink : Boolean = false,
  val isMove : Boolean = true,
  val textStyle: TextStyleConfig = TextStyleConfig(),
  val stroke: StrokeConfig = StrokeConfig(),
  val shadow: ShadowConfig = ShadowConfig(),
)

@Serializable
data class TextStyleConfig(
  val colorArgb: Long = Palette.PitchBlack.toArgbLong(),
  val colorType: TextColorType = TextColorType.SOLID,
  val gradientColorsArgb: List<Long> =
    listOf(
      Palette.Yellow.toArgbLong(),
      Palette.NeonPink.toArgbLong(),
    ),
  val gradientDistance: Float = 0.5f,
  val isGradientHorizontal: Boolean = false,
  val fontCategory : ScriptCategory = ScriptCategory.LATIN,
  val fontId: String = "ANTON",
  val googleFontName: String = "",
)

@Serializable
data class StrokeConfig(
  val isEnabled: Boolean = false,
  val width: Float = 1f,
  val colorArgb: Long = Color.Black.toArgbLong(),
)

@Serializable
data class ShadowConfig(
  val isEnabled: Boolean = false,
  val colorArgb: Long = Color.Black.copy(alpha = 0.75f).toArgbLong(),
  val radius: Float = 8f,
  val rotation: Float = 45f,
)

@Serializable
data class MorseConfig(
  val morseWpm: Int = 15,
  val bgColorMorse: Long = Palette.White.toArgbLong(),
  val isFlashScreen: Boolean = true,
  val isTorchEnabled: Boolean = false,
  val isSoundEnabled: Boolean = true,
  val isVibrateEnabled: Boolean = false,
)
