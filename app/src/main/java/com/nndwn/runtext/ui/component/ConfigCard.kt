package com.nndwn.runtext.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nndwn.runtext.ui.theme.dimens

@Composable
fun ConfigCard(
  modifier: Modifier = Modifier,
  padding : PaddingValues = PaddingValues(MaterialTheme.dimens.medium),
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(
      verticalArrangement = verticalArrangement,
      horizontalAlignment = horizontalAlignment,
      modifier = Modifier.padding(padding),
      content = content,
    )
  }
}
