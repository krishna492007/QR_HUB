package com.qr.hub.util.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Singleton AdManager for Google Mobile Ads (AdMob)
 */
object AdManager {
    private const val TAG = "QR_HUB_AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var isInitialized = false

    // Action counter for smart frequency capping (e.g. show 1 ad every 3 user actions)
    private val actionCounter = AtomicInteger(0)

    /**
     * Initialize MobileAds SDK once on app launch
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "AdMob initialized successfully: ${status.adapterStatusMap}")
                }
                loadInterstitial(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing AdMob: ${e.message}")
            }
        }
    }

    /**
     * Preload Interstitial Ad in background
     */
    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = AdConfig.interstitialAdUnitId

        InterstitialAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.d(TAG, "Interstitial Ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.w(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Show full-screen interstitial ad with smart frequency capping.
     * @param activity Current activity
     * @param interval Show ad once every `interval` actions (default: 3)
     * @param onComplete Callback invoked when ad is closed or if ad is skipped/not ready
     */
    fun showInterstitialWithFrequency(
        activity: Activity?,
        interval: Int = 3,
        onComplete: () -> Unit
    ) {
        if (activity == null) {
            onComplete()
            return
        }

        val count = actionCounter.incrementAndGet()
        // If count reached the frequency threshold and ad is loaded
        if (count % interval == 0 && interstitialAd != null) {
            showInterstitial(activity, onComplete)
        } else {
            // If ad not loaded, make sure we trigger background reload and proceed immediately
            if (interstitialAd == null) {
                loadInterstitial(activity)
            }
            onComplete()
        }
    }

    /**
     * Direct show full-screen interstitial ad
     */
    fun showInterstitial(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onComplete()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onComplete()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showed full screen")
                }
            }
            ad.show(activity)
        } else {
            loadInterstitial(activity)
            onComplete()
        }
    }
}
