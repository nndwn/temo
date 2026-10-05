package com.nndwn.runtext.ui.features.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.ScriptCategory
import com.nndwn.runtext.data.model.TextStyleConfig
import com.nndwn.runtext.ui.component.CardExpanded
import com.nndwn.runtext.ui.component.ConfigCard
import com.nndwn.runtext.ui.component.SlideUpPanel
import com.nndwn.runtext.ui.component.SlideUpPanelState
import com.nndwn.runtext.ui.features.main.LocalFonts
import com.nndwn.runtext.ui.features.main.LocalReadyScripts
import com.nndwn.runtext.ui.theme.dimens
import com.nndwn.runtext.ui.utils.fontFamilyFor

val ScriptCategory.displayName: String
  get() = when (this) {
    ScriptCategory.LATIN -> "Latin"
    ScriptCategory.ARABIC -> "Arabic (عربي)"
    ScriptCategory.JAPANESE -> "Japanese (日本語)"
    ScriptCategory.CHINESE -> "Chinese (中文)"
    ScriptCategory.KOREAN -> "Korean (한국어)"
    ScriptCategory.THAI -> "Thai (ไทย)"
    ScriptCategory.DEVANAGARI -> "Devanagari (हिन्दी)"
    ScriptCategory.KHMER -> "Khmer (ភាសាខ្មែរ)"
    ScriptCategory.HEBREW -> "Hebrew (עברית)"
  }

@Composable
fun TextFontStyleConfig(
  config: TextStyleConfig,
  expandId: String?,
  onToggle: (String) -> Unit,
  onOpenStyleFont: () -> Unit,
  onOpenTypeFont: () -> Unit,
) {
  val context = LocalContext.current
  val fonts = LocalFonts.current
  val readyScripts = LocalReadyScripts.current
  val currentFont =
    remember(config.fontId, fonts, readyScripts, config.fontCategory) {
      fonts.find { it.idFont == config.fontId }
    }

  CardExpanded(
    title = stringResource(R.string.set_config_text_style),
    idString = "text_style",
    expandedId = expandId,
    onToggle = onToggle,
  ) {
    Column(
      modifier = Modifier.padding(vertical = MaterialTheme.dimens.medium),
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
    ) {
      ConfigCard(
        modifier =
          Modifier.fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
              indication = ripple(),
              interactionSource = remember { MutableInteractionSource() },
            ) {
              onOpenTypeFont()
            }
      ) {
        Text(stringResource(R.string.set_config_text_style_category), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
        Text(
          text = config.fontCategory.displayName,
          style = MaterialTheme.typography.titleLarge,
        )
      }

      ConfigCard(
        modifier =
          Modifier.fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
              indication = ripple(),
              interactionSource = remember { MutableInteractionSource() },
            ) {
              onOpenStyleFont()
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
  val context = LocalContext.current
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

    LazyRow(
      modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.dimens.medium),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.extraSmall),
    ) {
      items(
        items = ScriptCategory.entries,
        key = { it.name },
      ) { category ->
        val isSelected = category == selectedCategory
        FilterChip(
          selected = isSelected,
          onClick = { onUpdateFontCategory(category) },
          label = { Text(category.displayName) },
        )
      }
    }

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
              style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamilyFor(context, item)),
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
