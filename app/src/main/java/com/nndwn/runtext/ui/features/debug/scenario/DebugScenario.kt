package com.nndwn.runtext.ui.features.debug.scenario

import com.nndwn.runtext.data.datastore.SettingsDataStore
import com.nndwn.runtext.ui.UiEffectController

data class TestStepResult(
  val stepNumber: Int,
  val title: String,
  val isPassed: Boolean,
  val detailMessage: String,
)

interface DebugScenario {
  val id: String
  val title: String
  val buttonText: String
  val buttonEmoji: String

  suspend fun run(
    dataStore: SettingsDataStore,
    uiEffectController: UiEffectController,
    onStepResult: (TestStepResult) -> Unit,
  )
}
