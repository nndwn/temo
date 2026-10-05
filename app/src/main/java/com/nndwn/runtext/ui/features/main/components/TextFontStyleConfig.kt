package com.nndwn.runtext.ui.features.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.ScriptCategory
import com.nndwn.runtext.data.model.TextStyleConfig
import com.nndwn.runtext.ui.component.ConfigCard
import com.nndwn.runtext.ui.component.SlideUpPanel
import com.nndwn.runtext.ui.component.SlideUpPanelState
import com.nndwn.runtext.ui.features.main.LocalFonts
import com.nndwn.runtext.ui.features.main.LocalReadyScripts
import com.nndwn.runtext.ui.theme.dimens
import com.nndwn.runtext.ui.utils.detectPrimaryScript
import com.nndwn.runtext.ui.utils.fontFamilyFor

@Composable
fun TextFontStyleConfig(
  config: TextStyleConfig,
  onClick: () -> Unit,
) {
  val context = LocalContext.current
  val fonts = LocalFonts.current
  val readyScripts = LocalReadyScripts.current
  val currentFont =
    remember(config.fontId, fonts, readyScripts) {
      fonts.find { it.idFont == config.fontId }
    }

  ConfigCard(
    modifier =
      Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium).clickable(
        indication = ripple(),
        interactionSource = remember { MutableInteractionSource() },
      ) {
        onClick()
      }
  ) {
    Text(stringResource(R.string.set_config_text_style), style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
    Text(
      text = currentFont?.displayName ?: config.fontId,
      style =
        MaterialTheme.typography.titleLarge.copy(
          fontFamily = currentFont?.let { fontFamilyFor(context, it) } ?: MaterialTheme.typography.titleLarge.fontFamily
        ),
    )
  }
}

@Composable
fun SelectorFonts(
  settings: AppSettings,
  fonts: List<FontData>,
  onUpdateFontType: (String) -> Unit,
  showPanelFonts: Boolean,
  dismissPanel: () -> Unit,
) {
  val context = LocalContext.current
  val readyScripts = LocalReadyScripts.current

  val activeScript =
    remember(settings.lastText) {
      settings.lastText.detectPrimaryScript()
    }

  val sortedFonts =
    remember(activeScript, fonts, readyScripts) {
      if (activeScript == ScriptCategory.LATIN) {
        fonts
      } else {
        val (matchingFonts, otherFonts) =
          fonts.partition {
            it.scriptCategory == activeScript
          }
        matchingFonts + otherFonts
      }
    }

  SlideUpPanel(
    state =
      SlideUpPanelState(
        visible = showPanelFonts,
        enabledDragToDismiss = true,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
      ),
    onDismiss = dismissPanel,
  ) {
    Text(
      style = MaterialTheme.typography.titleLarge,
      text = stringResource(R.string.set_config_text_style),
      modifier = Modifier.align(Alignment.CenterHorizontally).padding(MaterialTheme.dimens.medium),
    )
    HorizontalDivider(
      modifier = Modifier.fillMaxWidth(),
      thickness = MaterialTheme.dimens.borderSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
    )

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(
        count = sortedFonts.size,
        key = { index -> sortedFonts[index].idFont },
      ) { index ->
        val item = sortedFonts[index]
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .clickable(
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                  onUpdateFontType(item.idFont)
                  dismissPanel()
                },
              )
              .padding(MaterialTheme.dimens.medium),
          contentAlignment = Alignment.CenterStart,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = item.displayName,
              fontWeight = FontWeight.Normal,
              style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamilyFor(context, item)),
            )
          }
        }
      }
    }
  }
}
