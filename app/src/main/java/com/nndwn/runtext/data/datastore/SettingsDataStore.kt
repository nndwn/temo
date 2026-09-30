package com.nndwn.runtext.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nndwn.runtext.AppFlavor
import com.nndwn.runtext.BuildConfig
import com.nndwn.runtext.data.model.AppSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class SettingsDataStore @Inject constructor(
  private val dataStore: DataStore<Preferences>,
  private val deviceStatsDataStore: DataStore<Preferences> = dataStore,
) {
  private companion object Keys {
    val APP_SETTINGS = stringPreferencesKey("app_settings")

    val HAS_TIPPED = booleanPreferencesKey("has_tipped")
    val ACCUMULATED_USAGE_TIME_KEY = longPreferencesKey("accumulated_usage_time")
    val ACCUMULATED_REVIEW_TIME_KEY = longPreferencesKey("accumulated_review_time")
    val HAS_REQUESTED_REVIEW = booleanPreferencesKey("has_requested_review")

    const val SUPPORT_DIALOG_COOLDOWN_MS = 900_000L
    const val REVIEW_COOLDOWN_MS = 3_600_000L
  }

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  val hasTipped: Flow<Boolean> =
    dataStore.data.catch { emit(emptyPreferences()) }.map { preferences -> preferences[HAS_TIPPED] ?: false }

  val shouldShowSupportDialog: Flow<Boolean> =
    combine(
      dataStore.data.catch { emit(emptyPreferences()) },
      deviceStatsDataStore.data.catch { emit(emptyPreferences()) },
    ) { prefs, devicePrefs ->
      val hasTipped = prefs[HAS_TIPPED] ?: false
      if (hasTipped) return@combine false

      val accumulatedTime = devicePrefs[ACCUMULATED_USAGE_TIME_KEY] ?: 0L
      accumulatedTime >= SUPPORT_DIALOG_COOLDOWN_MS
    }

  val shouldShowReviewPrompt: Flow<Boolean> =
    deviceStatsDataStore.data
      .catch { emit(emptyPreferences()) }
      .map { preferences ->
        if (AppFlavor.current != AppFlavor.PLAYSTORE) return@map false

        val hasRequestedReview = preferences[HAS_REQUESTED_REVIEW] ?: false
        if (hasRequestedReview) return@map false

        val accumulatedReviewTime = preferences[ACCUMULATED_REVIEW_TIME_KEY] ?: 0L
        accumulatedReviewTime >= REVIEW_COOLDOWN_MS
      }

  suspend fun incrementUsageTime(durationMs: Long) {
    deviceStatsDataStore.edit { preferences ->
      val currentSupport = preferences[ACCUMULATED_USAGE_TIME_KEY] ?: 0L
      preferences[ACCUMULATED_USAGE_TIME_KEY] = currentSupport + durationMs

      val hasRequestedReview = preferences[HAS_REQUESTED_REVIEW] ?: false
      if (AppFlavor.current == AppFlavor.PLAYSTORE && !hasRequestedReview) {
        val currentReview = preferences[ACCUMULATED_REVIEW_TIME_KEY] ?: 0L
        preferences[ACCUMULATED_REVIEW_TIME_KEY] = currentReview + durationMs
      }
    }
  }

  suspend fun setTippedStatus(hasTipped: Boolean) {
    dataStore.edit { preferences -> preferences[HAS_TIPPED] = hasTipped }
  }

  suspend fun recordSupportDialogShown() {
    deviceStatsDataStore.edit { preferences -> preferences[ACCUMULATED_USAGE_TIME_KEY] = 0L }
  }

  suspend fun recordReviewPromptShown() {
    deviceStatsDataStore.edit { preferences -> preferences[HAS_REQUESTED_REVIEW] = true }
  }

  suspend fun recordReviewPromptDismissed() {
    deviceStatsDataStore.edit { preferences -> preferences[ACCUMULATED_REVIEW_TIME_KEY] = 0L }
  }

  suspend fun recordSupportDialogShownIfFirstTime() {
    deviceStatsDataStore.edit { preferences ->
      if (preferences[ACCUMULATED_USAGE_TIME_KEY] == null) {
        preferences[ACCUMULATED_USAGE_TIME_KEY] = 0L
      }
    }
  }

  val accumulatedSupportTime: Flow<Long> =
    deviceStatsDataStore.data.catch { emit(emptyPreferences()) }.map { preferences -> preferences[ACCUMULATED_USAGE_TIME_KEY] ?: 0L }

  val accumulatedReviewTime: Flow<Long> =
    deviceStatsDataStore.data.catch { emit(emptyPreferences()) }.map { preferences -> preferences[ACCUMULATED_REVIEW_TIME_KEY] ?: 0L }

  val hasRequestedReview: Flow<Boolean> =
    deviceStatsDataStore.data.catch { emit(emptyPreferences()) }.map { preferences -> preferences[HAS_REQUESTED_REVIEW] ?: false }

  suspend fun debugTipped() {
    if (BuildConfig.DEBUG) {
      dataStore.edit { preferences -> preferences[HAS_TIPPED] = true }
    }
  }

  suspend fun debugForceShowSupportDialog() {
    if (BuildConfig.DEBUG) {
      deviceStatsDataStore.edit { preferences -> preferences[ACCUMULATED_USAGE_TIME_KEY] = SUPPORT_DIALOG_COOLDOWN_MS }
    }
  }

  suspend fun debugForceShowReviewPrompt() {
    if (BuildConfig.DEBUG) {
      deviceStatsDataStore.edit { preferences ->
        preferences[ACCUMULATED_REVIEW_TIME_KEY] = REVIEW_COOLDOWN_MS
        preferences[HAS_REQUESTED_REVIEW] = false
      }
    }
  }

  suspend fun debugResetReviewStatus() {
    if (BuildConfig.DEBUG) {
      deviceStatsDataStore.edit { preferences ->
        preferences[ACCUMULATED_REVIEW_TIME_KEY] = 0L
        preferences[HAS_REQUESTED_REVIEW] = false
      }
    }
  }

  suspend fun reset() {
    if (BuildConfig.DEBUG) {
      dataStore.edit { preferences -> preferences.clear() }
      deviceStatsDataStore.edit { preferences -> preferences.clear() }
    }
  }

  val settingsFlow: Flow<AppSettings> =
    dataStore.data
      .catch { emit(emptyPreferences()) }
      .map { prefs ->
        val jsonString = prefs[APP_SETTINGS]
        if (jsonString != null) {
          runCatching { json.decodeFromString<AppSettings>(jsonString) }.getOrDefault(AppSettings())
        } else {
          AppSettings()
        }
      }

  suspend fun saveSettings(settings: AppSettings) {
    dataStore.edit { prefs ->
      prefs[APP_SETTINGS] = json.encodeToString(settings)
    }
  }
}
