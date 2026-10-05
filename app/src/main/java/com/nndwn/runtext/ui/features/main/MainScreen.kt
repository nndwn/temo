package com.nndwn.runtext.ui.features.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nndwn.runtext.R
import com.nndwn.runtext.data.model.AppMode
import com.nndwn.runtext.data.model.AppSettings
import com.nndwn.runtext.data.font.FontBundleEntryPoint
import com.nndwn.runtext.data.model.FontData
import com.nndwn.runtext.data.model.ScriptCategory
import com.nndwn.runtext.ui.LocalMenuOptionHandler
import com.nndwn.runtext.ui.LocalSizeHeight
import com.nndwn.runtext.ui.LocalSizeWidth
import com.nndwn.runtext.ui.LocalToggleSidebar
import com.nndwn.runtext.ui.component.LoadingScreen
import com.nndwn.runtext.ui.component.MenuOptions
import com.nndwn.runtext.ui.component.Skeleton
import com.nndwn.runtext.ui.component.ThreeDotsHorizontal
import com.nndwn.runtext.ui.features.main.components.AppModeSettings
import com.nndwn.runtext.ui.features.main.components.LogoText
import com.nndwn.runtext.ui.features.main.components.MorseCodeSettingsList
import com.nndwn.runtext.ui.features.main.components.PreviewAndStart
import com.nndwn.runtext.ui.features.main.components.SelectorFonts
import com.nndwn.runtext.ui.features.main.components.TextInputConfig
import com.nndwn.runtext.ui.features.main.components.TextSettingsList
import com.nndwn.runtext.ui.theme.dimens

@Composable
fun MainScreen(
  viewModel: MainViewModel = hiltViewModel(),
  padding: PaddingValues,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val fonts by viewModel.fonts.collectAsStateWithLifecycle()

  val context = LocalContext.current
  val fontBundleRepository = remember(context) {
    EntryPointAccessors.fromApplication(context.applicationContext, FontBundleEntryPoint::class.java)
      .fontBundleRepository()
  }
  val readyScripts by fontBundleRepository.readyScripts.collectAsStateWithLifecycle()

  MainScreenContent(
    uiState = uiState,
    fonts = fonts,
    readyScripts = readyScripts,
    onEvent = viewModel::onEvent,
    padding = padding,
    limitText = viewModel.limitText,
  )
}

@Composable
fun MainScreenContent(
  padding: PaddingValues,
  uiState: MainUiState,
  fonts: List<FontData>,
  readyScripts: Set<ScriptCategory>,
  onEvent: (MainUiEvent) -> Unit,
  limitText: Int = 100,
) {
  val focusManager = LocalFocusManager.current

  var expandedPickerId by remember { mutableStateOf<String?>(null) }
  var showPanelFonts by remember { mutableStateOf(false) }

  val dispatch: (MainUiEvent) -> Unit = { event ->
    val isTextInput = event is MainUiEvent.UpdateText || event is MainUiEvent.ClearText
    if (!isTextInput) focusManager.clearFocus()
    onEvent(event)
  }

  val togglePicker: (String) -> Unit = { id ->
    expandedPickerId = if (expandedPickerId == id) null else id
    focusManager.clearFocus()
  }

  CompositionLocalProvider(
    LocalPadding provides padding,
    LocalLimitText provides limitText,
    LocalFonts provides fonts,
    LocalReadyScripts provides readyScripts,
  ) {

    MainScreenLayout(
      uiState = uiState,
      expandedPickerId = expandedPickerId,
      dispatch = dispatch,
      togglePicker = togglePicker,
      onFontPanelToggle = {
        expandedPickerId = null
        showPanelFonts = !showPanelFonts
      },
      onAutoExpandPicker = { expandedPickerId = it },
    )

    MainFontSelector(
      uiState = uiState,
      fonts = fonts,
      showPanelFonts = showPanelFonts,
      onDismiss = { showPanelFonts = false },
      onUpdateFont = { fontId ->
        focusManager.clearFocus()
        expandedPickerId = null
        dispatch(MainUiEvent.UpdateFontType(fontId))
      },
    )

    val successState = uiState as? MainUiState.Success
    LoadingScreen(
      show = successState?.isExportingVideo == true,
      value = successState?.exportProgress ?: 0,
    )
  }
}

@Composable
private fun MainScreenLayout(
  uiState: MainUiState,
  expandedPickerId: String?,
  dispatch: (MainUiEvent) -> Unit,
  togglePicker: (String) -> Unit,
  onFontPanelToggle: () -> Unit,
  onAutoExpandPicker: (String) -> Unit,
) {

  val widowSizeHeight = LocalSizeHeight.current
  val widowSizeWidth = LocalSizeWidth.current
  val focusManager = LocalFocusManager.current
  val paddingFromRoot = LocalPadding.current
  val toggleSidebar = LocalToggleSidebar.current
  val onMenuSelected = LocalMenuOptionHandler.current
  val listState = rememberLazyListState()

  LaunchedEffect(listState.isScrollInProgress) {
    if (listState.isScrollInProgress) focusManager.clearFocus()
  }

  if (uiState is MainUiState.Success) {
    LaunchedEffect(uiState.settings.mode) {
      val id =
        when (uiState.settings.mode) {
          AppMode.MORSE_CODE -> "morse_color"
          AppMode.RUNNING_TEXT -> "text_presets"
        }
      onAutoExpandPicker(id)
    }
  }

  Row(modifier = Modifier.fillMaxWidth().padding(paddingFromRoot)) {
    if (widowSizeWidth != WindowWidthSizeClass.Compact) {
      MainSidebar(
        uiState = uiState,
        widowSizeHeight = widowSizeHeight,
        menus = onMenuSelected,
        onNavigateToDisplay = { dispatch(MainUiEvent.NavigateToDisplay) },
      )
    }

    MainConfigList(
      listState = listState,
      uiState = uiState,
      expandedPickerId = expandedPickerId,
      dispatch = dispatch,
      togglePicker = togglePicker,
      sideBarEnd = toggleSidebar,
      onFontPanelToggle = onFontPanelToggle,
    )
  }
}

@Composable
private fun RowScope.MainConfigList(
  listState: LazyListState,
  uiState: MainUiState,
  expandedPickerId: String?,
  dispatch: (MainUiEvent) -> Unit,
  togglePicker: (String) -> Unit,
  sideBarEnd: () -> Unit,
  onFontPanelToggle: () -> Unit,
) {

  val widowSizeHeight = LocalSizeHeight.current
  val widowSizeWidth = LocalSizeWidth.current

  LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize().weight(1f),
    contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.medium),
  ) {
    item {
      MainTopBar(
        isCompactWidth = widowSizeWidth == WindowWidthSizeClass.Compact,
        onSidebarToggle = sideBarEnd,
      )
    }

    when (uiState) {
      is MainUiState.Loading -> loadingContent(widowSizeHeight)
      is MainUiState.Success ->
        successContent(
          settings = uiState.settings,
          enteredText = uiState.enteredText,
          widowSizeHeight = widowSizeHeight,
          expandedPickerId = expandedPickerId,
          togglePicker = togglePicker,
          dispatch = dispatch,
          dispatchAndClosePicker = {
            togglePicker("") // Simplifies dispatchAndClosePicker
            dispatch(it)
          },
          onFontPanelToggle = onFontPanelToggle,
        )
    }
  }
}

@Composable
private fun MainFontSelector(
  uiState: MainUiState,
  fonts: List<FontData>,
  showPanelFonts: Boolean,
  onDismiss: () -> Unit,
  onUpdateFont: (String) -> Unit,
) {
  (uiState as? MainUiState.Success)?.let { success ->
    SelectorFonts(
      settings = success.settings,
      fonts = fonts,
      onUpdateFontType = onUpdateFont,
      showPanelFonts = showPanelFonts,
      dismissPanel = onDismiss,
    )
  }
}

@Composable
private fun MainSidebar(
  uiState: MainUiState,
  widowSizeHeight: WindowHeightSizeClass,
  menus: (MenuOptions) -> Unit,
  onNavigateToDisplay: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxHeight().width(250.dp),
    color = MaterialTheme.colorScheme.secondary,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      LogoText(
        modifier =
          Modifier.padding(
            vertical = MaterialTheme.dimens.medium,
            horizontal = MaterialTheme.dimens.small,
          )
      )
      Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
      if (widowSizeHeight == WindowHeightSizeClass.Compact && uiState is MainUiState.Success) {
        PreviewAndStart(
          settings = uiState.settings,
          modifier = Modifier.padding(horizontal = MaterialTheme.dimens.small),
          onNavigateToDisplay = onNavigateToDisplay,
        )
      }
      if (widowSizeHeight != WindowHeightSizeClass.Compact) {
        MenuOptions(onMenuSelected = menus)
      }
    }
  }
}

@Composable
private fun MainTopBar(
  isCompactWidth: Boolean,
  onSidebarToggle: () -> Unit,
) {
  AnimatedVisibility(
    visible = isCompactWidth,
    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
  ) {
    LogoText(
      modifier = Modifier.padding(vertical = MaterialTheme.dimens.small),
      content = {
        ThreeDotsHorizontal(onClick = onSidebarToggle)
      },
    )
  }
}

private fun LazyListScope.loadingContent(widowSizeHeight: WindowHeightSizeClass) {
  item {
    val transition = rememberInfiniteTransition(label = "MainShimmerTransition")
    val shimmerProgress by
      transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
          ),
        label = "MainShimmerProgress",
      )

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)) {
      if (widowSizeHeight != WindowHeightSizeClass.Compact) {
        Skeleton(shimmerProgress = shimmerProgress, height = 140.dp)
      }
      Skeleton(shimmerProgress = shimmerProgress, height = 120.dp)
      repeat(7) {
        Skeleton(shimmerProgress = shimmerProgress, height = 56.dp)
      }
    }
  }
}

private fun LazyListScope.successContent(
  settings: AppSettings,
  enteredText: String,
  widowSizeHeight: WindowHeightSizeClass,
  expandedPickerId: String?,
  togglePicker: (String) -> Unit,
  dispatch: (MainUiEvent) -> Unit,
  dispatchAndClosePicker: (MainUiEvent) -> Unit,
  onFontPanelToggle: () -> Unit,
) {
  if (widowSizeHeight != WindowHeightSizeClass.Compact) {
    stickyHeader {
      PreviewAndStart(
        modifier =
          Modifier.clip(
              MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(0),
                topEnd = CornerSize(0),
              )
            )
            .background(color = MaterialTheme.colorScheme.background)
            .padding(top = MaterialTheme.dimens.medium),
        settings = settings,
        onNavigateToDisplay = { dispatch(MainUiEvent.NavigateToDisplay) },
      )
    }
  }

  item {
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
    TextInputConfig(
      text = enteredText,
      onTextChange = { dispatch(MainUiEvent.UpdateText(it)) },
      onClearText = { dispatch(MainUiEvent.ClearText) },
    )
  }

  item {
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
    ButtonDownloadAndShare(onClick = { dispatch(MainUiEvent.ExportAndShareVideo) })
  }

  item {
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
    AppModeSettings(
      currentMode = settings.mode,
      onModeChange = { dispatch(MainUiEvent.UpdateMode(it)) },
    )
  }

  item {
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
    ModeSpecificSettings(
      settings = settings,
      expandedPickerId = expandedPickerId,
      togglePicker = togglePicker,
      dispatch = dispatch,
      dispatchAndClosePicker = dispatchAndClosePicker,
      onFontPanelToggle = onFontPanelToggle,
    )
  }
}


@Composable fun ButtonDownloadAndShare(onClick: () -> Unit = {}) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(56.dp)
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        .clickable(
          indication = ripple(),
          interactionSource = remember { MutableInteractionSource() },
          onClick = onClick,
        )
        .padding(
          horizontal = MaterialTheme.dimens.small,
          vertical = MaterialTheme.dimens.small,
        ),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(MaterialTheme.dimens.small),
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_share),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(MaterialTheme.dimens.iconMedium),
      )
      Spacer(modifier = Modifier.width(MaterialTheme.dimens.extraSmall))
      Text(
        text = stringResource(R.string.btn_download_and_share),
        style =
          MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
          ),
      )
    }
  }
}
@Composable
private fun ModeSpecificSettings(
  settings: AppSettings,
  expandedPickerId: String?,
  togglePicker: (String) -> Unit,
  dispatch: (MainUiEvent) -> Unit,
  dispatchAndClosePicker: (MainUiEvent) -> Unit,
  onFontPanelToggle: () -> Unit,
) {
  AnimatedContent(
    targetState = settings.mode,
    transitionSpec = {
      if (targetState == AppMode.MORSE_CODE) {
        slideInHorizontally { width -> width } + fadeIn() togetherWith
          slideOutHorizontally { width -> -width } + fadeOut()
      } else {
        slideInHorizontally { width -> -width } + fadeIn() togetherWith
          slideOutHorizontally { width -> width } + fadeOut()
      }
    },
    label = "ModeSettingsTransition",
  ) { mode ->
    when (mode) {
      AppMode.RUNNING_TEXT -> {
        TextSettingsList (
          settings = settings,
          expandedPickerId = expandedPickerId,
          togglePicker = togglePicker,
          dispatch = dispatch,
          dispatchAndClosePicker = dispatchAndClosePicker,
          onFontPanelToggle = onFontPanelToggle,
        )
      }
      AppMode.MORSE_CODE -> {
        MorseCodeSettingsList(
          settings = settings,
          expandedPickerId = expandedPickerId,
          togglePicker = togglePicker,
          dispatch = dispatch,
        )
      }
    }
  }
}




