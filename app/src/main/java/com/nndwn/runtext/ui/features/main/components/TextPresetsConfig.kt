package com.nndwn.runtext.ui.features.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.ShadowConfig
import com.nndwn.runtext.data.model.StrokeConfig
import com.nndwn.runtext.data.model.TextColorType
import com.nndwn.runtext.data.model.TextConfig
import com.nndwn.runtext.data.model.TextStyleConfig
import com.nndwn.runtext.ui.LocalFonts
import com.nndwn.runtext.ui.component.CardExpanded
import com.nndwn.runtext.ui.features.main.MainUiEvent
import com.nndwn.runtext.ui.theme.dimens
import com.nndwn.runtext.ui.theme.toArgbLong
import com.nndwn.runtext.ui.theme.toComposeColor
import com.nndwn.runtext.ui.utils.rememberFontFamily

data class TextPreset(
  val name: String,
  val settings: TextConfig,
)

@Composable
fun TextPresetConfig(
  modifier: Modifier = Modifier,
  expandedId: String?,
  onToggle: (String) -> Unit,
  onEvent: (MainUiEvent) -> Unit,
) {
  CardExpanded(
    title = stringResource(R.string.set_config_text_presets),
    modifier = modifier,
    idString = "text_presets",
    expandedId = expandedId,
    onToggle = onToggle,
  ) {
    LazyRow(
      modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.dimens.medium),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      items(Presets) { preset ->
        PresetItem(
          preset = preset,
          onClick = { onEvent(MainUiEvent.ApplyPreset(preset.settings)) },
        )
      }
    }
  }
}

@Composable
private fun PresetItem(
  preset: TextPreset,
  onClick: () -> Unit,
) {
  val fonts = LocalFonts.current
  val fontData =
    remember(preset.settings.textStyle.fontId, fonts) {
      fonts.find { it.idFont == preset.settings.textStyle.fontId }
    }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
      ),
  ) {
    Box(
      modifier =
        Modifier.size(80.dp)
          .clip(MaterialTheme.shapes.medium)
          .background(preset.settings.bgColorArgb.toComposeColor())
          .border(
            width = MaterialTheme.dimens.borderMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = MaterialTheme.shapes.medium,
          )
          .padding(MaterialTheme.dimens.extraSmall),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Aa",
        fontFamily =
          rememberFontFamily(fontData, MaterialTheme.typography.titleLarge.fontFamily ?: FontFamily.Default),
        color =
          if (preset.settings.textStyle.colorType == TextColorType.SOLID) {
            preset.settings.textStyle.colorArgb.toComposeColor()
          } else {
            preset.settings.textStyle.gradientColorsArgb.firstOrNull()?.toComposeColor()
              ?: MaterialTheme.colorScheme.onSurface
          },
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge,
      )
    }
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
    Text(
      text = preset.name,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

val Presets =
  listOf(
    TextPreset(
      name = "Caution Alert",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFFFFCC00).toArgbLong(), // Caution Yellow
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.SOLID,
              colorArgb = Color(0xFF111111).toArgbLong(),
              fontId = "ARCHIVO_BLACK",
            ),
          stroke =
            StrokeConfig(
              isEnabled = true,
              width = 1f,
              colorArgb = Color(0xFFFFCC00).toArgbLong(),
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFF090909).toArgbLong(),
              radius = 7f,
              rotation = 9f,
            ),
        ),
    ),
    TextPreset(
      name = "Retro Arcade",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF0F051D).toArgbLong(), // Dark Arcade Purple
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.GRADIENT,
              gradientColorsArgb =
                listOf(
                  Color(0xFF00E5FF).toArgbLong(), // Neon Cyan
                  Color(0xFF00FF66).toArgbLong(), // Arcade Lime Green
                ),
              fontId = "PRESS_START_2P",
              isGradientHorizontal = true,
            ),
          stroke =
            StrokeConfig(
              isEnabled = true,
              width = 2f,
              colorArgb = Color(0xFF1F0038).toArgbLong(),
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFF00FF66).toArgbLong(),
              radius = 12f,
              rotation = 45f,
            ),
        ),
    ),
    TextPreset(
      name = "Comic Pop",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF2979FF).toArgbLong(), // Bright Blue
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.SOLID,
              colorArgb = Color(0xFFFFEA00).toArgbLong(), // Yellow
              fontId = "BANGERS",
            ),
          stroke =
            StrokeConfig(
              isEnabled = true,
              width = 4f,
              colorArgb = Color(0xFF000000).toArgbLong(),
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFF000000).toArgbLong(),
              radius = 2f,
              rotation = 45f,
            ),
        ),
    ),
    TextPreset(
      name = "Cute Pastel",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF3A2E39).toArgbLong(),
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.GRADIENT,
              gradientColorsArgb =
                listOf(
                  Color(0xFFF8BBD0).toArgbLong(), // Soft Pink
                  Color(0xFFE1BEE7).toArgbLong(), // Soft Purple
                ),
              fontId = "PACIFICO",
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFFFC1E1E).toArgbLong(),
              radius = 6f,
              rotation = 57f,
            ),
        ),
    ),
    TextPreset(
      name = "Electric EDM",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF4A00E0).toArgbLong(),
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.SOLID,
              colorArgb = Color(0xFF8E2DE2).toArgbLong(),
              fontId = "ANTON",
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFF000000).toArgbLong(),
              radius = 4f,
              rotation = 90f,
            ),
        ),
    ),
    TextPreset(
      name = "Cyber Neon",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF0D0E15).toArgbLong(),
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.GRADIENT,
              gradientColorsArgb =
                listOf(
                  Color(0xFFFF007F).toArgbLong(), // Neon Pink
                  Color(0xFF00F0FF).toArgbLong(), // Neon Cyan
                ),
              fontId = "ORBITRON",
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFFFF007F).toArgbLong(),
              radius = 16f,
            ),
        ),
    ),
    TextPreset(
      name = "Sunset Vibes",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF181124).toArgbLong(),
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.GRADIENT,
              gradientColorsArgb =
                listOf(
                  Color(0xFFFF512F).toArgbLong(), // Orange
                  Color(0xFFDD2476).toArgbLong(), // Sunset Pink
                ),
              fontId = "BEBAS_NEUE",
              isGradientHorizontal = true,
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFFFF512F).toArgbLong(),
              radius = 10f,
              rotation = 45f,
            ),
        ),
    ),
    TextPreset(
      name = "Gold Luxury",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF0F0F0F).toArgbLong(),
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.GRADIENT,
              gradientColorsArgb =
                listOf(
                  Color(0xFFFFE082).toArgbLong(), // Light Gold
                  Color(0xFFC5A059).toArgbLong(), // Deep Gold
                ),
              fontId = "ABRIL_FATFACE",
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFFFFD54F).toArgbLong(),
              radius = 8f,
            ),
        ),
    ),
    TextPreset(
      name = "Frozen Ice",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF0A192F).toArgbLong(), // Deep Navy
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.GRADIENT,
              gradientColorsArgb =
                listOf(
                  Color(0xFFE0F7FA).toArgbLong(), // Ice Blue Light
                  Color(0xFF80DEEA).toArgbLong(), // Cyan Ice
                ),
              fontId = "RIGHTEOUS",
            ),
          stroke =
            StrokeConfig(
              isEnabled = true,
              width = 2f,
              colorArgb = Color(0xFF00BCD4).toArgbLong(),
            ),
          shadow =
            ShadowConfig(
              isEnabled = true,
              colorArgb = Color(0xFF80DEEA).toArgbLong(),
              radius = 12f,
            ),
        ),
    ),
    TextPreset(
      name = "Minimal Dark",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFF121212).toArgbLong(),
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.SOLID,
              colorArgb = Color(0xFFFFFFFF).toArgbLong(),
              fontId = "INTER",
            ),
        ),
    ),
    TextPreset(
      name = "Minimal Light",
      settings =
        TextConfig(
          bgColorArgb = Color(0xFFFFFFFF).toArgbLong(),
          textStyle =
            TextStyleConfig(
              colorType = TextColorType.SOLID,
              colorArgb = Color(0xFF121212).toArgbLong(),
              fontId = "MONTSERRAT",
            ),
        ),
    ),
  )
