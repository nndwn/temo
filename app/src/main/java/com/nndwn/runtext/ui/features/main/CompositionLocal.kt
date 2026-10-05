package com.nndwn.runtext.ui.features.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.ScriptCategory

val LocalLimitText = compositionLocalOf { 100 }

val LocalPadding = compositionLocalOf { PaddingValues() }

val LocalFonts = compositionLocalOf { emptyList<FontData>() }

val LocalReadyScripts = compositionLocalOf { emptySet<ScriptCategory>() }
