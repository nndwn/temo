package com.nndwn.runtext.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nndwn.runtext.AppFlavor
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.data.model.TextConfig
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsDataStoreTest {

  @get:Rule val tmpFolder = TemporaryFolder()

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var settingsDataStore: SettingsDataStore

  @Before
  fun setup() {
    // Create a temporary DataStore in the temp folder
    testDataStore =
      PreferenceDataStoreFactory.create(
        scope = testScope,
        produceFile = { File(tmpFolder.root, "test_settings.preferences_pb") },
      )
    settingsDataStore = SettingsDataStore(testDataStore)
  }

  @Test
  fun `settingsFlow returns default AppSettings when empty`() =
    runTest(testDispatcher) {
      val settings = settingsDataStore.settingsFlow.first()
      assertEquals(AppSettings(), settings)
    }

  @Test
  fun `saveSettings persists data correctly using JSON`() =
    runTest(testDispatcher) {
      val customSettings =
        AppSettings(
          lastText = "Hello Test",
          textConfig = TextConfig(speed = 200f),
        )

      settingsDataStore.saveSettings(customSettings)

      val savedSettings = settingsDataStore.settingsFlow.first()
      assertEquals("Hello Test", savedSettings.lastText)
      assertEquals(200f, savedSettings.textConfig.speed)
    }

  @Test
  fun `settingsFlow handles corrupted JSON by returning default`() =
    runTest(testDispatcher) {
      val corruptedJson = "{ invalid_json }"
      testDataStore.edit { prefs ->
        prefs[stringPreferencesKey("app_settings")] = corruptedJson
      }

      val settings = settingsDataStore.settingsFlow.first()
      // Should return default AppSettings if JSON decoding fails
      assertEquals(AppSettings(), settings)
    }

  @Test
  fun `setTippedStatus persists tipped flag independently`() =
    runTest(testDispatcher) {
      settingsDataStore.setTippedStatus(true)

      val hasTipped = settingsDataStore.hasTipped.first()
      assertEquals(true, hasTipped)
    }

  @Test
  fun `incrementUsageTime accumulates time for support dialog after 15 mins`() =
    runTest(testDispatcher) {
      // Pastikan awalnya false
      var shouldShowSupport = settingsDataStore.shouldShowSupportDialog.first()
      assertEquals(false, shouldShowSupport)

      // Tambah waktu di bawah threshold (misal 5 menit = 300_000ms)
      settingsDataStore.incrementUsageTime(300_000L)
      shouldShowSupport = settingsDataStore.shouldShowSupportDialog.first()
      assertEquals(false, shouldShowSupport)

      // Tambah waktu lagi hingga melewati threshold 15 menit (900_000ms)
      settingsDataStore.incrementUsageTime(600_000L)
      shouldShowSupport = settingsDataStore.shouldShowSupportDialog.first()
      assertEquals(true, shouldShowSupport)

      // Setelah dialog direset (muncul), waktu akumulasi kembali ke 0
      settingsDataStore.recordSupportDialogShown()
      shouldShowSupport = settingsDataStore.shouldShowSupportDialog.first()
      assertEquals(false, shouldShowSupport)
    }

  @Test
  fun `incrementUsageTime accumulates time for review prompt after 1 hour on playstore and shows only once`() =
    runTest(testDispatcher) {
      val isPlaystore = AppFlavor.current == AppFlavor.PLAYSTORE

      var shouldShowReview = settingsDataStore.shouldShowReviewPrompt.first()
      assertEquals(false, shouldShowReview)

      // Tambah total waktu hingga 1 jam (3_600_000ms)
      settingsDataStore.incrementUsageTime(3_600_000L)
      shouldShowReview = settingsDataStore.shouldShowReviewPrompt.first()
      assertEquals(isPlaystore, shouldShowReview)

      // Tandai review prompt sudah pernah ditampilkan
      settingsDataStore.recordReviewPromptShown()
      shouldShowReview = settingsDataStore.shouldShowReviewPrompt.first()
      assertEquals(false, shouldShowReview)

      // Tambah waktu lagi (misal 1 jam lagi), review prompt harus TETAP false (hanya muncul sekali)
      settingsDataStore.incrementUsageTime(3_600_000L)
      shouldShowReview = settingsDataStore.shouldShowReviewPrompt.first()
      assertEquals(false, shouldShowReview)
    }

  @Test
  fun `recordReviewPromptDismissed resets review time and allows prompt again after another hour`() =
    runTest(testDispatcher) {
      val isPlaystore = AppFlavor.current == AppFlavor.PLAYSTORE

      // Accumulate 1 hour
      settingsDataStore.incrementUsageTime(3_600_000L)
      var shouldShowReview = settingsDataStore.shouldShowReviewPrompt.first()
      assertEquals(isPlaystore, shouldShowReview)

      // User dismisses ("Lain Waktu") -> reset cooldown
      settingsDataStore.recordReviewPromptDismissed()
      shouldShowReview = settingsDataStore.shouldShowReviewPrompt.first()
      assertEquals(false, shouldShowReview)

      // Accumulate another 1 hour -> should show again if Play Store
      settingsDataStore.incrementUsageTime(3_600_000L)
      shouldShowReview = settingsDataStore.shouldShowReviewPrompt.first()
      assertEquals(isPlaystore, shouldShowReview)
    }
}
