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
 * 100% Pure Google AdMob AdManager (Start.io Disabled)
 */
object AdManager {
    private const val TAG = "QR_HUB_AdManager"

    private var isInitialized = false

    // AdMob Interstitial state
    private var admobInterstitialAd: InterstitialAd? = null
    private var isAdMobLoading = false

    // Action counter for frequency capping (shows ad once every 2 actions)
    private val actionCounter = AtomicInteger(0)

    /**
     * Initialize Google AdMob SDK
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestConfig = com.google.android.gms.ads.RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf("7A8DA5A46C233CCB74A4A9204D4D0827", "2624F5EBBB2A79FADA8466E8F406CB7D", "28FB779D1A707B78E3D4EF8619190FE7"))
                    .build()
                MobileAds.setRequestConfiguration(requestConfig)

                MobileAds.initialize(appContext) { initStatus ->
                    Log.d(TAG, "Google AdMob initialized successfully: $initStatus")
                    preloadAdMobInterstitial(appContext)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google AdMob init error: ${e.message}")
            }
        }
    }

    /**
     * Background Preload Google AdMob Interstitial
     */
    fun preloadAdMobInterstitial(context: Context) {
        if (admobInterstitialAd != null || isAdMobLoading) return
        isAdMobLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdConfig.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    admobInterstitialAd = interstitialAd
                    isAdMobLoading = false
                    Log.d(TAG, "Google AdMob Interstitial Preloaded Successfully!")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    admobInterstitialAd = null
                    isAdMobLoading = false
                    Log.w(TAG, "Google AdMob Interstitial failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Show full-screen interstitial ad with frequency capping (Only Google AdMob)
     */
    fun showInterstitialWithFrequency(
        activity: Activity?,
        interval: Int = 2,
        onComplete: () -> Unit
    ) {
        if (activity == null) {
            onComplete()
            return
        }

        val count = actionCounter.incrementAndGet()
        if (count % interval == 0) {
            showInterstitial(activity, onComplete)
        } else {
            // Keep AdMob preloaded in background
            if (admobInterstitialAd == null) preloadAdMobInterstitial(activity)
            onComplete()
        }
    }

    /**
     * Show ONLY Google AdMob Interstitial Ad
     */
    fun showInterstitial(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        val admobAd = admobInterstitialAd

        if (admobAd != null) {
            admobAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial Dismissed/Closed by user")
                    admobInterstitialAd = null
                    preloadAdMobInterstitial(activity)
                    onComplete()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "AdMob Failed to show: ${adError.message}")
                    admobInterstitialAd = null
                    preloadAdMobInterstitial(activity)
                    onComplete()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial Displayed Full Screen!")
                }
            }
            admobAd.show(activity)
        } else {
            Log.d(TAG, "AdMob Interstitial not ready yet, preloading for next time...")
            preloadAdMobInterstitial(activity)
            onComplete()
        }
    }
}
