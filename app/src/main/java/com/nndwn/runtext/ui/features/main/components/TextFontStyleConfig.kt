package com.nndwn.runtext.ui.features.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.ScriptCategory
import com.nndwn.runtext.data.model.TextStyleConfig
import com.nndwn.runtext.ui.LocalFonts
import com.nndwn.runtext.ui.LocalReadyScripts
import com.nndwn.runtext.ui.component.CardExpanded
import com.nndwn.runtext.ui.component.ConfigCard
import com.nndwn.runtext.ui.component.SlideUpPanel
import com.nndwn.runtext.ui.component.SlideUpPanelState
import com.nndwn.runtext.ui.theme.dimens
import com.nndwn.runtext.ui.utils.rememberFontFamily
@Composable
fun TextFontStyleConfig(
  config: TextStyleConfig,
  onOpenStyleFont: () -> Unit,
  onOpenTypeFont: () -> Unit,
) {
  val fonts = LocalFonts.current
  val readyScripts = LocalReadyScripts.current
  val currentFont =
    remember(config.fontId, fonts, readyScripts, config.fontCategory) {
      fonts.find { it.idFont == config.fontId }
    }

  ConfigCard(
    padding = PaddingValues(0.dp),
  ) {

    SelectMenu(
      shape = MaterialTheme.shapes.medium,
      title = stringResource(R.string.set_config_text_style_category),
      value = config.fontCategory.displayName,
      valueStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurface
      ),
      onClick = onOpenTypeFont
    )
    HorizontalDivider(
      modifier = Modifier.fillMaxWidth(),
      thickness = MaterialTheme.dimens.borderSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
    )

    SelectMenu(
      modifier = Modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.medium,
      title = stringResource(R.string.set_config_text_style),
      value = currentFont?.displayName ?: config.fontId,
      valueStyle =
        MaterialTheme.typography.titleSmall.copy(
          fontFamily =
            rememberFontFamily(
              currentFont,
              MaterialTheme.typography.titleSmall.fontFamily ?: FontFamily.Default,
            )
        ),
      onClick = onOpenStyleFont
    )
  }
}

@Composable
fun SelectMenu(
  modifier: Modifier = Modifier,
  shape : Shape,
  title : String,
  value : String,
  valueStyle : TextStyle,
  onClick : ()-> Unit,
){
  Column(
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .clickable(
        indication = ripple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
      )
      .padding(MaterialTheme.dimens.medium)
  ) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    Text(
      text = value,
      style = valueStyle,
    )
  }
}

@Composable
fun SelectorCategoryFont(
  currentCategory: ScriptCategory,
  onUpdateCategory: (ScriptCategory) -> Unit,
  showPanel: Boolean,
  onDismiss: () -> Unit,
) {
  val readyScripts = LocalReadyScripts.current

  SlideUpPanel(
    state =
      SlideUpPanelState(
        visible = showPanel,
        enabledDragToDismiss = true,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
      ),
    onDismiss = onDismiss,
  ) {
    Text(
      style = MaterialTheme.typography.titleLarge,
      text = stringResource(R.string.set_config_text_style_category),
      modifier = Modifier.align(Alignment.CenterHorizontally).padding(MaterialTheme.dimens.medium),
    )
    HorizontalDivider(
      modifier = Modifier.fillMaxWidth(),
      thickness = MaterialTheme.dimens.borderSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
    )

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(
        items = ScriptCategory.entries,
        key = { it.name },
      ) { category ->
        val isReady = category == ScriptCategory.LATIN || readyScripts.contains(category)
        val isSelected = category == currentCategory

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .clickable(
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                  onUpdateCategory(category)
                  onDismiss()
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
            Column {
              Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              if (isReady) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Downloaded",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              } else {
                Icon(
                  imageVector = Icons.Default.CloudDownload,
                  contentDescription = "Download on demand",
                  tint = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SelectorFonts(
  settings: AppSettings,
  fonts: List<FontData>,
  onUpdateFontType: (String) -> Unit,
  onUpdateFontCategory: (ScriptCategory) -> Unit,
  showPanelFonts: Boolean,
  dismissPanel: () -> Unit,
) {
  val readyScripts = LocalReadyScripts.current
  val selectedCategory = settings.textConfig.textStyle.fontCategory

  val filteredFonts =
    remember(selectedCategory, fonts, readyScripts) {
      val matching = fonts.filter { it.scriptCategory == selectedCategory }
      matching.ifEmpty { fonts }
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
      modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.dimens.small),
      thickness = MaterialTheme.dimens.borderSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
    )

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(
        count = filteredFonts.size,
        key = { index -> filteredFonts[index].idFont },
      ) { index ->
        val item = filteredFonts[index]
        val isReady = item.scriptCategory == ScriptCategory.LATIN ||
          !item.localResName.isNullOrEmpty() ||
          readyScripts.contains(item.scriptCategory)
        val isSelected = item.idFont == settings.textConfig.textStyle.fontId

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
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              style = MaterialTheme.typography.bodyLarge.copy(fontFamily = rememberFontFamily(item)),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )

            if (!isReady) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = "Download",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.outline,
                )
                Icon(
                  imageVector = Icons.Default.CloudDownload,
                  contentDescription = "Download on demand",
                  tint = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.size(18.dp),
                )
              }
            } else if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }
    }
  }
}
