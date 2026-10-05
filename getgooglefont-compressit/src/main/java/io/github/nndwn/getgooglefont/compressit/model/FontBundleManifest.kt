package io.github.nndwn.getgooglefont.compressit.model

import kotlinx.serialization.Serializable

@Serializable
data class FontBundleManifest(
    val version: String,
    val bundles: Map<String, FontBundleInfo>,
)

@Serializable
data class FontBundleInfo(
    val url: String,
    val sha256: String? = null,
)
