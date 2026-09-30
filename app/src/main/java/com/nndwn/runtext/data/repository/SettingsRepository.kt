package com.nndwn.runtext.data.repository

import com.nndwn.runtext.data.datastore.SettingsDataStore
import com.nndwn.runtext.data.model.AppSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction over [SettingsDataStore]. Keeps the ViewModel decoupled from the persistence implementation.
 */
@Singleton
class SettingsRepository @Inject constructor(private val dataStore: SettingsDataStore) {
  /** Emits the latest [AppSettings]. Always emits at least the default values. */
  val settingsFlow: Flow<AppSettings> = dataStore.settingsFlow

  val hasTipped: Flow<Boolean> = dataStore.hasTipped

  val shouldShowSupportDialog: Flow<Boolean> = dataStore.shouldShowSupportDialog

  val shouldShowReviewPrompt: Flow<Boolean> = dataStore.shouldShowReviewPrompt

  /** Persist the entire [AppSettings] object to DataStore. */
  suspend fun saveSettings(settings: AppSettings) {
    dataStore.saveSettings(settings)
  }

  suspend fun setTippedStatus(hasTipped: Boolean) {
    dataStore.setTippedStatus(hasTipped)
  }

  suspend fun resetCooldownSupportDialog() {
    dataStore.recordSupportDialogShown()
  }

  suspend fun recordReviewCompleted() {
    dataStore.recordReviewPromptShown()
  }

  suspend fun resetCooldownReviewPrompt() {
    dataStore.recordReviewPromptDismissed()
  }

  suspend fun initialCooldownSupportDialog() {
    dataStore.recordSupportDialogShownIfFirstTime()
  }

  suspend fun incrementUsageTime(durationMs: Long) {
    dataStore.incrementUsageTime(durationMs)
  }
}
