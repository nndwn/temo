package com.nndwn.runtext.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nndwn.runtext.R
import com.nndwn.runtext.ui.LocalSizeWidth
import com.nndwn.runtext.ui.theme.RuntextTheme

@Composable
fun DialogReview(
  showPanel: Boolean,
  onDismiss: () -> Unit = {},
  onClickReviewApp: () -> Unit = {},
) {
  val appName = stringResource(R.string.app_name)

  ActionDialog(
    showPanel = showPanel,
    iconResId = R.drawable.ic_star,
    title = stringResource(R.string.title_dialog_review, appName),
    description = stringResource(R.string.text_dialog_review, appName),
    primaryButtonLabel = stringResource(R.string.btn_rate_app_now),
    onClickPrimary = onClickReviewApp,
    onDismiss = onDismiss,
  )
}

@Preview(device = "id:medium_phone")
@Composable
private fun Preview() {
  var show by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxSize()) { Button(onClick = { show = !show }) { Text("Show and Hide") } }

  CompositionLocalProvider(LocalSizeWidth provides WindowWidthSizeClass.Compact) {
    RuntextTheme { DialogReview(true) }
  }
}
