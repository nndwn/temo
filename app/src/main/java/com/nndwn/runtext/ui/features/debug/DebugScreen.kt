package com.nndwn.runtext.ui.features.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nndwn.runtext.AppFlavor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(viewModel: DebugViewModel = hiltViewModel(), onBack: () -> Unit = {}) {
  val hasTipped by viewModel.hasTipped.collectAsStateWithLifecycle()
  val accumulatedSupportTime by viewModel.accumulatedSupportTime.collectAsStateWithLifecycle()
  val accumulatedReviewTime by viewModel.accumulatedReviewTime.collectAsStateWithLifecycle()
  val hasRequestedReview by viewModel.hasRequestedReview.collectAsStateWithLifecycle()
  val shouldShowSupportDialog by viewModel.shouldShowSupportDialog.collectAsStateWithLifecycle()
  val shouldShowReviewPrompt by viewModel.shouldShowReviewPrompt.collectAsStateWithLifecycle()

  val testResults by viewModel.testResults.collectAsStateWithLifecycle()
  val isRunningTest by viewModel.isRunningTest.collectAsStateWithLifecycle()
  val currentRunningScenarioId by viewModel.currentRunningScenarioId.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Debug Panel") },
        navigationIcon = {
          IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        },
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 📊 Live Status Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text("📊 Live Debug Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          HorizontalDivider()

          Text("App Flavor: ${AppFlavor.current}")
          Text("Has Tipped: $hasTipped")
          Text("Support Usage Time: ${accumulatedSupportTime / 1000}s / 900s (Active: $shouldShowSupportDialog)")
          Text("Review Usage Time: ${accumulatedReviewTime / 1000}s / 3600s")
          Text("Has Requested Review: $hasRequestedReview")
          Text("Review Prompt Active: $shouldShowReviewPrompt", fontWeight = FontWeight.Bold)
        }
      }

      // 🧪 Automated Test Scenario Runners
      Text("🧪 Automated Scenario Tests", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

      viewModel.scenarios.forEach { scenario ->
        val isThisScenarioRunning = isRunningTest && currentRunningScenarioId == scenario.id
        Button(
          onClick = { viewModel.runScenario(scenario) },
          enabled = !isRunningTest,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (scenario.id == "review_dialog") {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.secondary
            }
          ),
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (isThisScenarioRunning) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
              )
              Text("Running Scenario...")
            } else {
              Text("${scenario.buttonEmoji} ${scenario.buttonText}")
            }
          }
        }
      }

      OutlinedButton(
        onClick = { viewModel.resetDataStore() },
        enabled = !isRunningTest,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text("🔄 Reset DataStore & Logs")
      }

      // 📋 Test Logs & Results
      if (testResults.isNotEmpty()) {
        Text("📋 Test Results Log", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

        testResults.forEach { result ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
              containerColor = if (result.isPassed) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.errorContainer
              }
            ),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              val icon = if (result.isPassed) "✅" else "❌"
              Text(
                "$icon Step ${result.stepNumber}: ${result.title}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
              )
              Text(
                result.detailMessage,
                style = MaterialTheme.typography.bodySmall,
              )
            }
          }
        }

        if (!isRunningTest && testResults.all { it.isPassed }) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
          ) {
            Text(
              "🎉 SCENARIO PASSED SUCCESSFULLY!",
              modifier = Modifier.padding(12.dp),
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
          }
        }
      }
    }
  }
}
