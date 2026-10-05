package com.nndwn.runtext.data.font

import kotlinx.serialization.Serializable

@Serializable
data class FontBundleManifest(
  val version: String,
  val baseUrl: String? = null,
  val bundles: Map<String, FontBundleInfo>,
)

@Serializable
data class FontBundleInfo(
  val url: String? = null,
  val file: String? = null,
  val sha256: String? = null,
) {
  fun resolveUrl(manifestBaseUrl: String?, manifestVersion: String, scriptName: String): String {
    if (!url.isNullOrBlank()) return url
    val fileName = file ?: "${scriptName.lowercase()}.zip"
    val base = (manifestBaseUrl ?: "https://github.com/nndwn/temo/releases/download").trimEnd('/')
    return "$base/$manifestVersion/$fileName"
  }
}
