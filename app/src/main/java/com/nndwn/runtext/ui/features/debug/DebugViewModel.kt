package com.nndwn.runtext.ui.features.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nndwn.runtext.data.datastore.SettingsDataStore
import com.nndwn.runtext.ui.features.debug.scenario.DebugScenario
import com.nndwn.runtext.ui.features.debug.scenario.ReviewDialogScenario
import com.nndwn.runtext.ui.features.debug.scenario.SupportDialogScenario
import com.nndwn.runtext.ui.features.debug.scenario.TestStepResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DebugViewModel @Inject constructor(private val dataStore: SettingsDataStore) : ViewModel() {

  val scenarios: List<DebugScenario> = listOf(
    ReviewDialogScenario(),
    SupportDialogScenario(),
  )

  val hasTipped: StateFlow<Boolean> =
    dataStore.hasTipped.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val accumulatedSupportTime: StateFlow<Long> =
    dataStore.accumulatedSupportTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

  val accumulatedReviewTime: StateFlow<Long> =
    dataStore.accumulatedReviewTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

  val hasRequestedReview: StateFlow<Boolean> =
    dataStore.hasRequestedReview.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val shouldShowSupportDialog: StateFlow<Boolean> =
    dataStore.shouldShowSupportDialog.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val shouldShowReviewPrompt: StateFlow<Boolean> =
    dataStore.shouldShowReviewPrompt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  private val _testResults = MutableStateFlow<List<TestStepResult>>(emptyList())
  val testResults: StateFlow<List<TestStepResult>> = _testResults

  private val _isRunningTest = MutableStateFlow(false)
  val isRunningTest: StateFlow<Boolean> = _isRunningTest

  private val _currentRunningScenarioId = MutableStateFlow<String?>(null)
  val currentRunningScenarioId: StateFlow<String?> = _currentRunningScenarioId

  fun runScenario(scenario: DebugScenario) {
    if (_isRunningTest.value) return

    viewModelScope.launch {
      _isRunningTest.value = true
      _currentRunningScenarioId.value = scenario.id
      _testResults.value = emptyList()

      scenario.run(dataStore) { result ->
        _testResults.value = _testResults.value + result
      }

      _currentRunningScenarioId.value = null
      _isRunningTest.value = false
    }
  }

  fun resetDataStore() {
    viewModelScope.launch {
      dataStore.reset()
      _testResults.value = emptyList()
    }
  }
}
