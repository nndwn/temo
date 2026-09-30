package com.nndwn.runtext.ui.features.debug.scenario

import com.nndwn.runtext.data.datastore.SettingsDataStore
import com.nndwn.runtext.ui.UiEffect
import com.nndwn.runtext.ui.UiEffectController
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class SupportDialogScenario : DebugScenario {
  override val id: String = "support_dialog"
  override val title: String = "Support Dialog Flow"
  override val buttonText: String = "Run Automated Support Test Scenarios"
  override val buttonEmoji: String = "☕"

  override suspend fun run(
    dataStore: SettingsDataStore,
    uiEffectController: UiEffectController,
    onStepResult: (TestStepResult) -> Unit,
  ) {
    // Step 1: Initial Reset
    dataStore.setTippedStatus(false)
    dataStore.recordSupportDialogShown()
    uiEffectController.sendEffect(UiEffect.ShowDebugSupportDialog(false))
    delay(500.milliseconds)
    val initialSupport = dataStore.shouldShowSupportDialog.first()
    val initialTipped = dataStore.hasTipped.first()
    val p1 = !initialSupport && !initialTipped
    onStepResult(
      TestStepResult(
        stepNumber = 1,
        title = "Initial Support State Reset",
        isPassed = p1,
        detailMessage = if (p1) "hasTipped is false & support dialog is inactive" else "Failed to reset support state",
      )
    )
    delay(1500.milliseconds)

    // Step 2: Force Set 15 Mins Support Time (1st UI Dialog Pop-up)
    dataStore.debugForceShowSupportDialog()
    delay(500.milliseconds)
    val supportActive = dataStore.shouldShowSupportDialog.first()
    val p2 = supportActive
    if (p2) {
      uiEffectController.sendEffect(UiEffect.ShowDebugSupportDialog(true))
    }
    onStepResult(
      TestStepResult(
        stepNumber = 2,
        title = "Simulate 15 Mins Usage Time (1st UI Dialog Popped Up)",
        isPassed = p2,
        detailMessage = if (p2) "Dialog Support popped up on screen" else "Support dialog did not trigger",
      )
    )
    delay(2000.milliseconds) // Pause so user physically sees DialogSupport on screen!

    // Step 3: Simulate 'Lain Waktu' (Dismiss Support Dialog & Reset Cooldown)
    dataStore.recordSupportDialogShown()
    uiEffectController.sendEffect(UiEffect.ShowDebugSupportDialog(false))
    delay(500.milliseconds)
    val afterDismissSupport = dataStore.shouldShowSupportDialog.first()
    val supportTimeAfterDismiss = dataStore.accumulatedSupportTime.first()
    val p3 = !afterDismissSupport && supportTimeAfterDismiss == 0L
    onStepResult(
      TestStepResult(
        stepNumber = 3,
        title = "Simulate User Tapping 'Lain Waktu / Maybe Later' (UI Dialog Closed)",
        isPassed = p3,
        detailMessage =
          if (p3) "Dialog closed, support cooldown reset to 0ms" else "Failed to reset support time on dismiss",
      )
    )
    delay(1500.milliseconds)

    // Step 4: Simulate Next 15 Mins Usage Time (2nd UI Dialog Pop-up)
    dataStore.debugForceShowSupportDialog()
    delay(500.milliseconds)
    val supportActiveAgain = dataStore.shouldShowSupportDialog.first()
    val p4 = supportActiveAgain
    if (p4) {
      uiEffectController.sendEffect(UiEffect.ShowDebugSupportDialog(true))
    }
    onStepResult(
      TestStepResult(
        stepNumber = 4,
        title = "Simulate Next 15 Mins Usage Time (2nd UI Dialog Popped Up)",
        isPassed = p4,
        detailMessage =
          if (p4) "Dialog Support popped up on screen again" else "Support dialog did not re-trigger",
      )
    )
    delay(2000.milliseconds) // Pause so user physically sees DialogSupport on screen again!

    // Step 5: Simulate User Tapping 'Treat me to a coffee' (Record Tipped & Close UI Dialog)
    dataStore.setTippedStatus(true)
    uiEffectController.sendEffect(UiEffect.ShowDebugSupportDialog(false))
    delay(500.milliseconds)
    val hasTippedVal = dataStore.hasTipped.first()
    val promptAfterTip = dataStore.shouldShowSupportDialog.first()
    val p5 = hasTippedVal && !promptAfterTip
    onStepResult(
      TestStepResult(
        stepNumber = 5,
        title = "Simulate User Tapping 'Treat me to a coffee' (UI Dialog Closed)",
        isPassed = p5,
        detailMessage =
          if (p5) "hasTipped set to true permanently & dialog closed" else "Failed to record tip completed",
      )
    )
    delay(1500.milliseconds)

    // Step 6: Verify Support Dialog Permanently Blocked After Tipping
    dataStore.debugForceShowSupportDialog() // Try to force 15 mins support time
    uiEffectController.sendEffect(UiEffect.ShowDebugSupportDialog(false))
    delay(500.milliseconds)
    val supportBlockedAfterTip = dataStore.shouldShowSupportDialog.first()
    val p6 = !supportBlockedAfterTip
    onStepResult(
      TestStepResult(
        stepNumber = 6,
        title = "Verify Support Dialog Blocked Permanently After Tip",
        isPassed = p6,
        detailMessage =
          if (p6) "Support dialog blocked permanently because hasTipped is true" else "Support dialog erroneously showed after tip",
      )
    )
    delay(1500.milliseconds)

    // Cleanup: Reset tipped status & usage time back to default clean state
    dataStore.setTippedStatus(false)
    dataStore.recordSupportDialogShown()
    uiEffectController.sendEffect(UiEffect.ShowDebugSupportDialog(false))
  }
}
