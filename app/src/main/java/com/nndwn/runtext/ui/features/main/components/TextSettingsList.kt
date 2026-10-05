package com.nndwn.runtext.ui.features.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.ui.component.ColorPickerField
import com.nndwn.runtext.ui.component.ConfigCard
import com.nndwn.runtext.ui.component.SwitchRow
import com.nndwn.runtext.ui.features.main.MainUiEvent
import com.nndwn.runtext.ui.theme.dimens
import com.nndwn.runtext.ui.theme.toComposeColor

@Composable
fun TextSettingsList(
    settings: AppSettings,
    expandedPickerId: String?,
    togglePicker: (String) -> Unit,
    dispatch: (MainUiEvent) -> Unit,
    dispatchAndClosePicker: (MainUiEvent) -> Unit,
    onFontPanelToggle: () -> Unit,
    onFontCategoryPanelToggle: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium),
    ) {
        TextPresetConfig(
            expandedId = expandedPickerId,
            onToggle = togglePicker,
            onEvent = dispatch,
        )
        TextSpeedConfig(
            speed = settings.textConfig.speed,
            onSpeedChange = { dispatch(MainUiEvent.UpdateSpeed(it)) },
        )
        ConfigCard {
            SwitchRow(
                title = stringResource(R.string.set_config_text_mirror),
                subtitle = stringResource(R.string.set_config_text_mirror_desc),
                checked = settings.textConfig.isMirrorMode,
                onCheckedChange = { dispatchAndClosePicker(MainUiEvent.UpdateMirrorMode(it)) },
            )
        }

        ConfigCard {
            SwitchRow(
                title = stringResource(R.string.set_config_text_move),
                subtitle = stringResource(R.string.set_config_text_move_desc),
                checked = settings.textConfig.isMove,
                onCheckedChange = { dispatchAndClosePicker(MainUiEvent.UpdateTextMove(it))},
            )
        }

        ConfigCard {
            SwitchRow(
                title = stringResource(R.string.set_config_text_blink),
                subtitle = stringResource(R.string.set_config_text_blink_desc),
                checked = settings.textConfig.isBlink,
                onCheckedChange = { dispatchAndClosePicker(MainUiEvent.UpdateBlinkMode(it))},
            )
        }

        TextFontStyleConfig(
            config = settings.textConfig.textStyle,
            onOpenStyleFont = onFontPanelToggle,
            onOpenTypeFont = onFontCategoryPanelToggle,
        )

        ConfigCard {
            ColorPickerField(
                label = stringResource(R.string.set_config_color_background),
                color = settings.textConfig.bgColorArgb.toComposeColor(),
                isExpanded = expandedPickerId == "bg",
                onToggleExpand = { togglePicker("bg") },
                onColorChange = { dispatch(MainUiEvent.UpdateBgColor(it)) },
            )
        }
        TextColorPickerConfig(
            label = stringResource(R.string.set_config_text_color_text),
            config = settings.textConfig.textStyle,
            expandedPickerId = expandedPickerId,
            onPickerToggle = togglePicker,
            onEvent = dispatch,
        )
        TextOutlineConfig(
            config = settings.textConfig.stroke,
            expandedPickerId = expandedPickerId,
            onPickerToggle = togglePicker,
            onEvent = dispatch,
        )
        TextShadowConfig(
            config = settings.textConfig.shadow,
            expandedPickerId = expandedPickerId,
            onPickerToggle = togglePicker,
            onEvent = dispatch,
        )
    }
}
