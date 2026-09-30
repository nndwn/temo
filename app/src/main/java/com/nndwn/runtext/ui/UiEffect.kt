package com.nndwn.runtext.ui

import android.net.Uri
import androidx.annotation.StringRes
import com.nndwn.runtext.ui.navigation.AppRoute


data class ToastData(
  @param:StringRes val message: Int,
  val text : String? = null
)
sealed interface UiEffect {
  data class ShowToast(val message: ToastData) : UiEffect

  data class NavigateTo(val route: AppRoute) : UiEffect

  data object NavigateBack : UiEffect

  data object RequestNavigateBackWithSupportDialogCheck : UiEffect

  data class ShareVideo(val videoUri: Uri) : UiEffect

  data class ShowDebugReviewDialog(val show: Boolean) : UiEffect

  data class ShowDebugSupportDialog(val show: Boolean) : UiEffect
}
