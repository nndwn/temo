package com.nndwn.runtext.helper

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class StubBillingHelper : BillingHelper {
  override val appPrice: StateFlow<String?> = MutableStateFlow<String?>(null).asStateFlow()
  override val purchaseSuccessEvent: SharedFlow<Unit> = MutableSharedFlow<Unit>().asSharedFlow()

  override fun startConnection(setPurchased: (Boolean) -> Unit, billingDisconnected: () -> Unit) {
    setPurchased(false)
  }

  override fun launchBillingFlow(activity: Activity) {
    val intent = Intent(Intent.ACTION_VIEW, "https://buymeacoffee.com/nndwn".toUri())
    activity.startActivity(intent)
  }
}
