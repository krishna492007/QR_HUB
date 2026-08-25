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
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsLoadOptions
import com.unity3d.ads.UnityAdsShowOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Robust Singleton AdManager supporting Unity Ads (Live APK) & Google AdMob
 */
object AdManager {
    private const val TAG = "QR_HUB_AdManager"

    private var isUnityInitialized = false
    private var isAdMobInitialized = false
    private var isUnityAdLoaded = false

    private var admobInterstitialAd: InterstitialAd? = null
    private var isAdMobLoading = false

    // Action counter for smart frequency capping (shows ad once every 2 actions)
    private val actionCounter = AtomicInteger(0)

    /**
     * Initialize Ad Networks on Main Thread
     */
    fun initialize(context: Context) {
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.Main).launch {
            // 1. Initialize Unity Ads
            if (!isUnityInitialized) {
                try {
                    UnityAds.initialize(
                        appContext,
                        AdConfig.UNITY_GAME_ID,
                        AdConfig.UNITY_TEST_MODE,
                        object : IUnityAdsInitializationListener {
                            override fun onInitializationComplete() {
                                isUnityInitialized = true
                                Log.d(TAG, "Unity Ads initialized successfully (Live Mode = ${!AdConfig.UNITY_TEST_MODE})")
                                loadUnityInterstitial()
                            }

                            override fun onInitializationFailed(
                                error: UnityAds.UnityAdsInitializationError,
                                message: String
                            ) {
                                Log.e(TAG, "Unity Ads Init Failed: $error - $message")
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Unity Ads init error: ${e.message}")
                }
            }

            // 2. Initialize Google Mobile Ads (AdMob)
            if (!isAdMobInitialized) {
                try {
                    MobileAds.initialize(appContext) {
                        isAdMobInitialized = true
                        Log.d(TAG, "Google AdMob initialized successfully")
                        loadAdMobInterstitial(appContext)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "AdMob init error: ${e.message}")
                }
            }
        }
    }

    /**
     * Preload Unity Interstitial
     */
    fun loadUnityInterstitial() {
        val loadOptions = UnityAdsLoadOptions()
        UnityAds.load(
            AdConfig.UNITY_INTERSTITIAL_PLACEMENT,
            loadOptions,
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String) {
                    isUnityAdLoaded = true
                    Log.d(TAG, "Unity Interstitial loaded successfully: $placementId")
                }

                override fun onUnityAdsFailedToLoad(
                    placementId: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    isUnityAdLoaded = false
                    Log.w(TAG, "Unity Interstitial failed to load: $placementId ($error: $message)")
                }
            }
        )
    }

    /**
     * Preload AdMob Interstitial
     */
    fun loadAdMobInterstitial(context: Context) {
        if (admobInterstitialAd != null || isAdMobLoading) return
        isAdMobLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            AdConfig.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    admobInterstitialAd = ad
                    isAdMobLoading = false
                    Log.d(TAG, "AdMob Interstitial loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    admobInterstitialAd = null
                    isAdMobLoading = false
                    Log.w(TAG, "AdMob Interstitial failed: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Show full-screen interstitial ad with smart frequency capping.
     * @param activity Current activity
     * @param interval Frequency interval (default: 2 actions)
     * @param onComplete Callback invoked when ad is closed or if skipped
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
            // Trigger preloads if needed
            if (!isUnityAdLoaded) loadUnityInterstitial()
            if (admobInterstitialAd == null) loadAdMobInterstitial(activity)
            onComplete()
        }
    }

    /**
     * Show full-screen interstitial ad (Unity Ads primary with AdMob fallback)
     */
    fun showInterstitial(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        // Priority 1: Unity Ads
        if (isUnityAdLoaded) {
            val showOptions = UnityAdsShowOptions()
            UnityAds.show(
                activity,
                AdConfig.UNITY_INTERSTITIAL_PLACEMENT,
                showOptions,
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowFailure(
                        placementId: String,
                        error: UnityAds.UnityAdsShowError,
                        message: String
                    ) {
                        Log.w(TAG, "Unity Show Failed: $message")
                        isUnityAdLoaded = false
                        loadUnityInterstitial()
                        // Fallback to AdMob if Unity show fails
                        showAdMobFallback(activity, onComplete)
                    }

                    override fun onUnityAdsShowStart(placementId: String) {
                        Log.d(TAG, "Unity Ad Show Start")
                    }

                    override fun onUnityAdsShowClick(placementId: String) {
                        Log.d(TAG, "Unity Ad Clicked")
                    }

                    override fun onUnityAdsShowComplete(
                        placementId: String,
                        state: UnityAds.UnityAdsShowCompletionState
                    ) {
                        Log.d(TAG, "Unity Ad Completed: $state")
                        isUnityAdLoaded = false
                        loadUnityInterstitial()
                        onComplete()
                    }
                }
            )
            return
        }

        // Priority 2: AdMob
        showAdMobFallback(activity, onComplete)
    }

    private fun showAdMobFallback(activity: Activity, onComplete: () -> Unit) {
        val adMobAd = admobInterstitialAd
        if (adMobAd != null) {
            adMobAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    admobInterstitialAd = null
                    loadAdMobInterstitial(activity)
                    onComplete()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "AdMob failed to show: ${adError.message}")
                    admobInterstitialAd = null
                    loadAdMobInterstitial(activity)
                    onComplete()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "AdMob showed full screen")
                }
            }
            adMobAd.show(activity)
        } else {
            loadUnityInterstitial()
            loadAdMobInterstitial(activity)
            onComplete()
        }
    }
}
