package com.nndwn.runtext.data.font

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class FontBundleWorker(
  appContext: Context,
  params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    val scriptName = inputData.getString(KEY_SCRIPT) ?: return Result.failure()
    val url = inputData.getString(KEY_URL) ?: return Result.failure()
    val version = inputData.getString(KEY_VERSION) ?: return Result.failure()

    val fontsRoot = File(applicationContext.noBackupFilesDir, "fonts/$version")
    val finalDir = File(fontsRoot, scriptName)
    val stagingDir = File(fontsRoot, "$scriptName.tmp")
    val zipFile = File(File(applicationContext.cacheDir, "fonts-download/$version"), "$scriptName.zip")

    return try {
      download(url, zipFile)
      extract(zipFile, stagingDir)
      File(stagingDir, COMPLETE_MARKER).writeText("ok")
      if (finalDir.exists()) finalDir.deleteRecursively()
      if (!stagingDir.renameTo(finalDir)) {
        stagingDir.copyRecursively(finalDir, overwrite = true)
        stagingDir.deleteRecursively()
      }
      zipFile.delete()
      Result.success()
    } catch (e: Exception) {
      stagingDir.deleteRecursively()
      zipFile.delete()
      if (isStopped) Result.failure() else Result.retry()
    }
  }

  private fun download(url: String, target: File) {
    target.parentFile?.mkdirs()
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
      connection.requestMethod = "GET"
      connection.connectTimeout = 15_000
      connection.readTimeout = 60_000
      connection.instanceFollowRedirects = true
      connection.connect()
      val code = connection.responseCode
      if (code !in 200..299) throw IOException("HTTP $code")

      connection.inputStream.use { input ->
        FileOutputStream(target).use { output ->
          val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
          while (true) {
            if (isStopped) throw IOException("Cancelled")
            val read = input.read(buffer)
            if (read < 0) break
            output.write(buffer, 0, read)
          }
        }
      }
    } finally {
      connection.disconnect()
    }
  }

  private fun extract(zipFile: File, targetDir: File) {
    targetDir.mkdirs()
    ZipInputStream(zipFile.inputStream()).use { zis ->
      var entry = zis.nextEntry
      while (entry != null) {
        if (isStopped) throw IOException("Cancelled")
        if (!entry.isDirectory) {
          val outFile = File(targetDir, entry.name)
          outFile.parentFile?.mkdirs()
          FileOutputStream(outFile).use { out ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
              val read = zis.read(buffer)
              if (read < 0) break
              out.write(buffer, 0, read)
            }
          }
        }
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }
  }

  companion object {
    const val KEY_SCRIPT = "script"
    const val KEY_URL = "url"
    const val KEY_VERSION = "version"
    private const val COMPLETE_MARKER = ".complete"
  }
}
