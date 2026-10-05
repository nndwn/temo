package com.nndwn.runtext.ui.features.main.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.AppMode
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.ui.LocalFonts
import com.nndwn.runtext.ui.component.RunningTextRenderer
import com.nndwn.runtext.ui.theme.RuntextTheme
import com.nndwn.runtext.ui.theme.dimens
import com.nndwn.runtext.ui.theme.toComposeColor
import com.nndwn.runtext.utils.DisplayRatioManager

@Composable
fun PreviewAndStart(
  modifier: Modifier = Modifier,
  settings: AppSettings,
  onNavigateToDisplay: () -> Unit,
) {
  val context = LocalContext.current
  DisplayRatioManager.init(context)

  val shape = MaterialTheme.shapes.medium
  val listFont = LocalFonts.current
  val colorTransition by animateColorAsState(
    targetValue = if (settings.mode == AppMode.RUNNING_TEXT) settings.textConfig.bgColorArgb.toComposeColor() else Color.Black,
    animationSpec = tween(500),
    label = "Transition Color")

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .clip(shape)
          .border(
            width = MaterialTheme.dimens.borderMedium,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            shape = shape,
          )
    ) {
      Box(
        modifier = Modifier.fillMaxWidth()
          .aspectRatio(DisplayRatioManager.ratio)
          .background(colorTransition)
          .pointerInput(Unit) {},
        contentAlignment = Alignment.Center,
      ) {
        if (settings.mode == AppMode.RUNNING_TEXT) {
          RunningTextRenderer(
            text = settings.lastText,
            settings = settings.textConfig,
            fonts = listFont,
            editor = true,
          )
        } else {
          MorseFlashPreview(
            text = settings.lastText,
            settings = settings.morseConfig,
          )
        }
      }

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(
              indication = ripple(),
              interactionSource = remember { MutableInteractionSource() },
            ) {
              onNavigateToDisplay()
            }
            .padding(
              horizontal = MaterialTheme.dimens.medium,
              vertical = MaterialTheme.dimens.small,
            ),
        contentAlignment = Alignment.Center,
      ) {
        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().padding(MaterialTheme.dimens.small),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_play),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(MaterialTheme.dimens.iconMedium),
          )
          Spacer(modifier = Modifier.width(MaterialTheme.dimens.extraSmall))
          Text(
            text = stringResource(R.string.btn_start),
            style =
              MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
              ),
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  RuntextTheme {
    PreviewAndStart(
      settings = AppSettings().copy(mode = AppMode.MORSE_CODE),
      onNavigateToDisplay = {},
    )
  }
}
