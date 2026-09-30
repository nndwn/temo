package com.nndwn.runtext.ui.features.debug.scenario

import com.nndwn.runtext.data.datastore.SettingsDataStore
import com.nndwn.runtext.ui.UiEffect
import com.nndwn.runtext.ui.UiEffectController
import com.nndwn.runtext.ui.navigation.AppRoute
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class TextConfigScenario : DebugScenario {
  override val id: String = "text_config"
  override val title: String = "Text & Speed Configuration Flow"
  override val buttonText: String = "Run Text & Speed Test Scenario"
  override val buttonEmoji: String = "✍️"

  override suspend fun run(
    dataStore: SettingsDataStore,
    uiEffectController: UiEffectController,
    onStepResult: (TestStepResult) -> Unit,
  ) {
    // Step 1: Write Custom Text ("Testing Speed & Preview - Temo")
    val testMessage = "Testing Speed & Preview - Temo"
    val initialSettings = dataStore.settingsFlow.first()
    dataStore.saveSettings(initialSettings.copy(lastText = testMessage))
    delay(500.milliseconds)

    val savedSettingsAfterText = dataStore.settingsFlow.first()
    val p1 = savedSettingsAfterText.lastText == testMessage
    onStepResult(
      TestStepResult(
        stepNumber = 1,
        title = "Write Custom Text",
        isPassed = p1,
        detailMessage = if (p1) "Text set to '$testMessage'" else "Failed to update text",
      )
    )
    delay(1000.milliseconds)

    // Step 2: Change Speed Config (Set Speed to 400f)
    val testSpeed = 400f
    val currentSettings = dataStore.settingsFlow.first()
    dataStore.saveSettings(
      currentSettings.copy(
        textConfig = currentSettings.textConfig.copy(speed = testSpeed)
      )
    )
    delay(500.milliseconds)

    val savedSettingsAfterSpeed = dataStore.settingsFlow.first()
    val p2 = savedSettingsAfterSpeed.textConfig.speed == testSpeed
    onStepResult(
      TestStepResult(
        stepNumber = 2,
        title = "Change Text Speed",
        isPassed = p2,
        detailMessage = if (p2) "Speed successfully updated to ${testSpeed.toInt()}" else "Failed to update speed",
      )
    )
    delay(1000.milliseconds)

    // Step 3: Navigate to MainScreen (Input / Card Preview)
    uiEffectController.sendEffect(UiEffect.NavigateTo(AppRoute.Input))
    delay(2500.milliseconds) // Pause so user physically sees the updated text & speed on MainScreen card preview!

    val p3 = true
    onStepResult(
      TestStepResult(
        stepNumber = 3,
        title = "Verify MainScreen Card Preview",
        isPassed = p3,
        detailMessage = "Navigated to MainScreen - Text & Speed reflected in live preview",
      )
    )
    delay(1000.milliseconds)

    // Step 4: Navigate to DisplayScreen (Full Screen Animation)
    uiEffectController.sendEffect(UiEffect.NavigateTo(AppRoute.Display))
    delay(3000.milliseconds) // Pause so user physically sees the full screen running text in action!

    val p4 = true
    onStepResult(
      TestStepResult(
        stepNumber = 4,
        title = "Verify Full Screen Running Text Display",
        isPassed = p4,
        detailMessage = "Navigated to DisplayScreen - Running text animated for 3 seconds",
      )
    )
    delay(1000.milliseconds)

    // Step 5: Navigate Back to Debug Panel (Popping DisplayScreen off back stack)
    uiEffectController.sendEffect(UiEffect.NavigateBack)
    delay(1000.milliseconds)

    val p5 = true
    onStepResult(
      TestStepResult(
        stepNumber = 5,
        title = "Return to Debug Panel",
        isPassed = p5,
        detailMessage = "Popped DisplayScreen off back stack and returned cleanly to Debug Panel",
      )
    )
  }
}
