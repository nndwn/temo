package com.nndwn.runtext.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.nndwn.runtext.R
import com.nndwn.runtext.ui.theme.dimens

@Composable
fun ActionDialog(
  showPanel: Boolean,
  title: String,
  description: String,
  primaryButtonLabel: String,
  onClickPrimary: () -> Unit,
  onDismiss: () -> Unit,
  @DrawableRes iconResId: Int? = null,
  secondaryButtonLabel: String = stringResource(R.string.btn_maybe_later),
) {
  SlideUpPanel(
    state = SlideUpPanelState(visible = showPanel, containerColor = MaterialTheme.colorScheme.secondaryContainer)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(MaterialTheme.dimens.medium),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
      ) {
        if (iconResId != null) {
          Icon(
            painter = painterResource(iconResId),
            contentDescription = title,
            modifier = Modifier.size(MaterialTheme.dimens.iconLarge),
          )
        }
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleLarge,
          textAlign = TextAlign.Center,
        )
      }

      Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
      )

      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
      ) {
        Button(
          colors =
            ButtonDefaults.buttonColors()
              .copy(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
              ),
          onClick = onClickPrimary,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = primaryButtonLabel,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = MaterialTheme.dimens.small),
          )
        }
        TextButton(
          modifier = Modifier.fillMaxWidth(),
          onClick = onDismiss,
          colors =
            ButtonDefaults.outlinedButtonColors()
              .copy(contentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)),
        ) {
          Text(
            text = secondaryButtonLabel,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = MaterialTheme.dimens.small),
          )
        }
      }
    }
  }
}
