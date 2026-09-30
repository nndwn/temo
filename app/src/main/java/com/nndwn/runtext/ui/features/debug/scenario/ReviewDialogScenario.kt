package com.nndwn.runtext.ui.features.debug.scenario

import com.nndwn.runtext.AppFlavor
import com.nndwn.runtext.data.datastore.SettingsDataStore
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class ReviewDialogScenario : DebugScenario {
  override val id: String = "review_dialog"
  override val title: String = "Review Dialog Flow"
  override val buttonText: String = "Run Automated Review Test Scenarios"
  override val buttonEmoji: String = "🚀"

  override suspend fun run(
    dataStore: SettingsDataStore,
    onStepResult: (TestStepResult) -> Unit,
  ) {
    // Step 1: Initial Reset
    dataStore.reset()
    delay(500.milliseconds)
    val initialReviewPrompt = dataStore.shouldShowReviewPrompt.first()
    val initialHasRequested = dataStore.hasRequestedReview.first()
    val p1 = !initialReviewPrompt && !initialHasRequested
    onStepResult(
      TestStepResult(
        stepNumber = 1,
        title = "Initial DataStore Reset",
        isPassed = p1,
        detailMessage = if (p1) "Review prompt is inactive as expected" else "Failed to reset DataStore",
      )
    )
    delay(1500.milliseconds)

    // Step 2: Force Set 1 Hour Review Time (Triggers UI Dialog Pop-up)
    dataStore.debugForceShowReviewPrompt()
    delay(500.milliseconds)
    val isPlaystore = AppFlavor.current == AppFlavor.PLAYSTORE
    val reviewActive = dataStore.shouldShowReviewPrompt.first()
    val p2 = reviewActive == isPlaystore
    onStepResult(
      TestStepResult(
        stepNumber = 2,
        title = "Simulate 1 Hour Usage Time (UI Dialog Popped Up)",
        isPassed = p2,
        detailMessage =
          if (p2) "Dialog Review popped up on screen" else "Review prompt did not trigger correctly",
      )
    )
    delay(2000.milliseconds) // Pause so user physically sees DialogReview on screen!

    // Step 3: Simulate 'Lain Waktu' (Dismiss Dialog & Reset Cooldown)
    dataStore.recordReviewPromptDismissed()
    delay(500.milliseconds)
    val afterDismissPrompt = dataStore.shouldShowReviewPrompt.first()
    val reviewTimeAfterDismiss = dataStore.accumulatedReviewTime.first()
    val hasRequestedAfterDismiss = dataStore.hasRequestedReview.first()
    val p3 = !afterDismissPrompt && reviewTimeAfterDismiss == 0L && !hasRequestedAfterDismiss
    onStepResult(
      TestStepResult(
        stepNumber = 3,
        title = "Simulate User Tapping 'Lain Waktu' (UI Dialog Closed)",
        isPassed = p3,
        detailMessage =
          if (p3) "Dialog closed, cooldown reset to 0ms" else "Failed to reset cooldown on dismiss",
      )
    )
    delay(1500.milliseconds)

    // Step 4: Simulate Next 1 Hour Usage (Re-triggers UI Dialog)
    dataStore.debugForceShowReviewPrompt()
    delay(500.milliseconds)
    val reviewActiveAgain = dataStore.shouldShowReviewPrompt.first()
    val p4 = reviewActiveAgain == isPlaystore
    onStepResult(
      TestStepResult(
        stepNumber = 4,
        title = "Simulate Next 1 Hour Usage (UI Dialog Popped Up Again)",
        isPassed = p4,
        detailMessage =
          if (p4) "Dialog Review popped up on screen again" else "Failed to re-trigger review prompt",
      )
    )
    delay(2000.milliseconds) // Pause so user physically sees DialogReview on screen again!

    // Step 5: Simulate 'Rate App Now' (Record Review Completed & Close Dialog)
    dataStore.recordReviewPromptShown()
    delay(500.milliseconds)
    val hasRequestedFinal = dataStore.hasRequestedReview.first()
    val promptAfterRate = dataStore.shouldShowReviewPrompt.first()
    val p5 = hasRequestedFinal && !promptAfterRate
    onStepResult(
      TestStepResult(
        stepNumber = 5,
        title = "Simulate User Tapping 'Rate App Now' (UI Dialog Closed)",
        isPassed = p5,
        detailMessage =
          if (p5) "hasRequestedReview set to true permanently" else "Failed to record review completed",
      )
    )
    delay(1500.milliseconds)

    // Step 6: Verify Review Prompt Never Triggers Again
    dataStore.incrementUsageTime(3_600_000L)
    delay(500.milliseconds)
    val promptBlocked = dataStore.shouldShowReviewPrompt.first()
    val p6 = !promptBlocked
    onStepResult(
      TestStepResult(
        stepNumber = 6,
        title = "Verify Prompt Blocked Permanently (No UI Dialog)",
        isPassed = p6,
        detailMessage =
          if (p6) "Review prompt will NEVER show again as expected" else "Prompt erroneously triggered after rating!",
      )
    )
  }
}
