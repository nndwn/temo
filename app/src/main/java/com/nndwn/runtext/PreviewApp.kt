package com.nndwn.runtext

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.ui.LocalMenuOptionHandler
import com.nndwn.runtext.ui.LocalSizeHeight
import com.nndwn.runtext.ui.LocalSizeWidth
import com.nndwn.runtext.ui.LocalToggleSidebar
import com.nndwn.runtext.ui.component.MainLayout
import com.nndwn.runtext.ui.component.MainLayoutState
import com.nndwn.runtext.ui.features.main.MainScreenContent
import com.nndwn.runtext.ui.features.main.MainUiState
import com.nndwn.runtext.ui.navigation.AppRoute
import com.nndwn.runtext.ui.navigation.rememberNavigationState
import com.nndwn.runtext.ui.navigation.toEntries
import com.nndwn.runtext.ui.theme.RuntextTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun InteractivePreviewWrapper(
  widowSizeHeight: WindowHeightSizeClass = WindowHeightSizeClass.Compact,
  windowWidth: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {
  val navigationState = rememberNavigationState(
    startRoute = AppRoute.Input,
    topLevelRoutes = setOf(AppRoute.Input)
  )

  val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.last()
  var isSidebarOpen by remember { mutableStateOf(false) }
  val sidebarAllowed = isSidebarOpen && currentRoute != AppRoute.Display

  var settings by remember {
    mutableStateOf(AppSettings(lastText = "test preview"))
  }

  var noticeMessage by remember { mutableStateOf<Int?>(null) }

  CompositionLocalProvider(
    LocalSizeHeight provides widowSizeHeight,
    LocalSizeWidth provides windowWidth,
    LocalToggleSidebar provides {},
    LocalMenuOptionHandler provides {},
  ) {
    RuntextTheme {
      MainLayout(
        state =
          MainLayoutState()
            .copy(
              isOpen = sidebarAllowed,
              sidebarBackgroundColor = MaterialTheme.colorScheme.secondary,
            ),
        onCloseSidebar = { isSidebarOpen = false },
        sideBarEnd = {},
      ) { innerPadding ->
        val entryProvider = remember(innerPadding) {
          entryProvider<NavKey> {
            entry<AppRoute.Input> {
              MainScreenContent(
                uiState = MainUiState.Success(settings, settings.lastText),
                fonts = emptyList(),
                readyScripts = emptySet(),
                onEvent = {},
                padding = innerPadding,
              )
            }
            entry<AppRoute.Display> {
              // Display Screen
            }
          }
        }

        NavDisplay(
          entries = navigationState.toEntries(entryProvider),
          onBack = {}
        )
      }
    }
  }
}

@Preview(
  device = "spec:width=673dp,height=841dp",
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewTabletDark() {
  InteractivePreviewWrapper(
    widowSizeHeight = WindowHeightSizeClass.Medium,
    windowWidth = WindowWidthSizeClass.Medium,
  )
}
