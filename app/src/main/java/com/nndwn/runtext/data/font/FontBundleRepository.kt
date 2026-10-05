package com.nndwn.runtext.data.font

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.ScriptCategory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Singleton
class FontBundleRepository @Inject constructor(
  @ApplicationContext private val context: Context,
) {

  private val json = Json { ignoreUnknownKeys = true }
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  val manifest: FontBundleManifest = runCatching {
    val raw = context.resources.openRawResource(R.raw.bundles).bufferedReader().use { it.readText() }
    json.decodeFromString<FontBundleManifest>(raw)
  }.getOrElse {
    FontBundleManifest(version = "none", bundles = emptyMap())
  }

  private val _readyScripts = MutableStateFlow<Set<ScriptCategory>>(emptySet())
  val readyScripts: StateFlow<Set<ScriptCategory>> = _readyScripts.asStateFlow()

  init {
    refreshReadyScripts()
  }

  fun isBundleReady(script: ScriptCategory): Boolean = completeMarker(script).exists()

  fun cachedFontFile(script: ScriptCategory, idFont: String): File? {
    if (!isBundleReady(script)) return null
    val file = File(scriptDir(script), "${idFont.lowercase()}.ttf")
    return if (file.exists()) file else null
  }

  fun fontFamilyFromCache(script: ScriptCategory, idFont: String): FontFamily? {
    val file = cachedFontFile(script, idFont) ?: return null
    return try {
      FontFamily(Typeface.createFromFile(file))
    } catch (_: Exception) {
      null
    }
  }

  fun ensureBundle(script: ScriptCategory) {
    val info = manifest.bundles[script.name] ?: return
    if (isBundleReady(script)) return

    val uniqueName = uniqueWorkName(script)
    val data = Data.Builder()
      .putString(FontBundleWorker.KEY_SCRIPT, script.name)
      .putString(FontBundleWorker.KEY_URL, info.url)
      .putString(FontBundleWorker.KEY_VERSION, manifest.version)
      .build()

    val request = OneTimeWorkRequestBuilder<FontBundleWorker>()
      .setInputData(data)
      .build()

    WorkManager.getInstance(context)
      .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.KEEP, request)

    observeUntilReady(script)
  }

  private fun observeUntilReady(script: ScriptCategory) {
    val uniqueName = uniqueWorkName(script)
    scope.launch {
      runCatching {
        WorkManager.getInstance(context)
          .getWorkInfosForUniqueWorkFlow(uniqueName)
          .first { infos ->
            infos.any {
              it.state == WorkInfo.State.SUCCEEDED ||
                it.state == WorkInfo.State.FAILED ||
                it.state == WorkInfo.State.CANCELLED
            }
          }
      }
      refreshReadyScripts()
    }
  }

  private fun refreshReadyScripts() {
    _readyScripts.value = ScriptCategory.entries
      .filter { isBundleReady(it) }
      .toSet()
  }

  private fun uniqueWorkName(script: ScriptCategory): String =
    "font-bundle-${manifest.version}-${script.name}"

  private fun completeMarker(script: ScriptCategory): File =
    File(scriptDir(script), ".complete")

  private fun scriptDir(script: ScriptCategory): File =
    File(File(context.noBackupFilesDir, "fonts/${manifest.version}"), script.name)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FontBundleEntryPoint {
  fun fontBundleRepository(): FontBundleRepository
}
