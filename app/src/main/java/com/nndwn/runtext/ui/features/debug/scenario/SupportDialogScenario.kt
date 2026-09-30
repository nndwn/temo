package com.nndwn.runtext.ui.features.debug.scenario

import com.nndwn.runtext.data.datastore.SettingsDataStore
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
    onStepResult: (TestStepResult) -> Unit,
  ) {
    // Step 1: Initial Reset
    dataStore.setTippedStatus(false)
    dataStore.recordSupportDialogShown()
    delay(500.milliseconds)
    val initialSupport = dataStore.shouldShowSupportDialog.first()
    val p1 = !initialSupport
    onStepResult(
      TestStepResult(
        stepNumber = 1,
        title = "Initial Support State Reset",
        isPassed = p1,
        detailMessage = if (p1) "Support dialog is inactive as expected" else "Failed to reset support state",
      )
    )
    delay(1500.milliseconds)

    // Step 2: Force Set 15 Mins Support Time (Triggers UI Support Dialog Pop-up)
    dataStore.debugForceShowSupportDialog()
    delay(500.milliseconds)
    val supportActive = dataStore.shouldShowSupportDialog.first()
    val p2 = supportActive
    onStepResult(
      TestStepResult(
        stepNumber = 2,
        title = "Simulate 15 Mins Usage Time (Support UI Dialog Popped Up)",
        isPassed = p2,
        detailMessage = if (p2) "Dialog Support popped up on screen" else "Support dialog did not trigger",
      )
    )
    delay(2000.milliseconds) // Pause so user physically sees DialogSupport on screen!

    // Step 3: Simulate 'Lain Waktu' (Dismiss Support Dialog & Reset Usage Time)
    dataStore.recordSupportDialogShown()
    delay(500.milliseconds)
    val afterDismissSupport = dataStore.shouldShowSupportDialog.first()
    val supportTimeAfterDismiss = dataStore.accumulatedSupportTime.first()
    val p3 = !afterDismissSupport && supportTimeAfterDismiss == 0L
    onStepResult(
      TestStepResult(
        stepNumber = 3,
        title = "Simulate User Tapping 'Lain Waktu' (Support UI Dialog Closed)",
        isPassed = p3,
        detailMessage =
          if (p3) "Dialog closed, support time reset to 0ms" else "Failed to reset support time on dismiss",
      )
    )
    delay(1500.milliseconds)

    // Step 4: Simulate User Tipped / Buy Coffee (`hasTipped = true`)
    dataStore.setTippedStatus(true)
    dataStore.debugForceShowSupportDialog() // Try to force support time
    delay(500.milliseconds)
    val supportBlockedAfterTip = dataStore.shouldShowSupportDialog.first()
    val p4 = !supportBlockedAfterTip
    onStepResult(
      TestStepResult(
        stepNumber = 4,
        title = "Verify Support Dialog Blocked After Tipping",
        isPassed = p4,
        detailMessage =
          if (p4) "Support dialog blocked permanently because hasTipped is true" else "Support dialog erroneously showed after tip",
      )
    )
  }
}
