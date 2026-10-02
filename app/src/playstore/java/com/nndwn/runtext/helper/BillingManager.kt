package com.nndwn.runtext.helper

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.nndwn.runtext.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(@param:ApplicationContext private val context: Context) :
  PurchasesUpdatedListener, BillingHelper {
  private companion object {
    const val SUPPORT_PRODUCT_ID = "remove_ads_permanent"
  }

  private val scope = CoroutineScope(Dispatchers.Main)

  private val _purchaseSuccessEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  override val purchaseSuccessEvent: SharedFlow<Unit> = _purchaseSuccessEvent.asSharedFlow()

  private val billingClient: BillingClient =
    BillingClient.newBuilder(context)
      .setListener(this)
      .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
      .build()

  private val _productDetails = MutableStateFlow<ProductDetails?>(null)

  override val appPrice: StateFlow<String?> =
    _productDetails
      .map { details -> details?.oneTimePurchaseOfferDetails?.formattedPrice }
      .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

  private var onPurchasedListener: ((Boolean) -> Unit)? = null

  override fun launchBillingFlow(activity: Activity) {
    if (SUPPORT_PRODUCT_ID.isBlank()) {
      if (BuildConfig.DEBUG) {
        Log.e("BillingManager", "Cannot launch billing flow: SUPPORT_PRODUCT_ID is empty.")
      }
      return
    }
    val details = _productDetails.value ?: return
    val productDetailsParamsList =
      listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build())
    val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()

    billingClient.launchBillingFlow(activity, billingFlowParams)
  }

  override fun startConnection(setPurchased: (Boolean) -> Unit, billingDisconnected: () -> Unit) {
    this.onPurchasedListener = setPurchased
    if (billingClient.isReady) {
      queryPurchases(setPurchased)
      return
    }
    billingClient.startConnection(
      object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
          if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            queryPurchases(setPurchased)
            queryProductDetails()
          }
        }

        override fun onBillingServiceDisconnected() {
          billingDisconnected()
        }
      }
    )
  }

  private fun queryProductDetails() {
    if (SUPPORT_PRODUCT_ID.isBlank()) {
      if (BuildConfig.DEBUG) {
        Log.w("BillingManager", "SUPPORT_PRODUCT_ID is empty. Skipping product details query.")
      }
      return
    }
    val productList =
      listOf(
        QueryProductDetailsParams.Product.newBuilder()
          .setProductId(SUPPORT_PRODUCT_ID)
          .setProductType(BillingClient.ProductType.INAPP)
          .build()
      )
    val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
    billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        val list = queryProductDetailsResult.productDetailsList.toList()
        _productDetails.value = list.firstOrNull()
      }
    }
  }

  fun queryPurchases(setPurchased: (Boolean) -> Unit) {
    if (!billingClient.isReady) return
    if (SUPPORT_PRODUCT_ID.isBlank()) {
      setPurchased(false)
      return
    }
    val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
    billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        val hasPremium = purchases.any { item ->
          item.products.contains(SUPPORT_PRODUCT_ID) && item.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        setPurchased(hasPremium)
      }
      purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach { consumePurchase(it) }
    }
  }

  private fun consumePurchase(purchase: Purchase) {
    val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
    billingClient.consumeAsync(params) { billingResult, _ ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        onPurchasedListener?.invoke(true)
        _purchaseSuccessEvent.tryEmit(Unit)
      }
    }
  }

  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase?>?) {
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      for (purchase in purchases) {
        if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
          consumePurchase(purchase)
        }
      }
    }
  }
}