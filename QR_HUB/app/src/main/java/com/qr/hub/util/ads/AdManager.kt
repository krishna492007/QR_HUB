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
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * High-Performance Hybrid AdManager:
 * - Primary: Google AdMob (Industry-highest eCPM & revenue)
 * - Fallback: Start.io (100% Instant Fill Rate & zero revenue loss)
 */
object AdManager {
    private const val TAG = "QR_HUB_AdManager"

    private var isInitialized = false

    // AdMob Interstitial state
    private var admobInterstitialAd: InterstitialAd? = null
    private var isAdMobLoading = false

    // Start.io Interstitial state
    private var startIoPreloadedAd: StartAppAd? = null
    private var isStartIoLoaded = false

    // Action counter for smart frequency capping (shows ad once every 2 actions)
    private val actionCounter = AtomicInteger(0)

    /**
     * Initialize both Google AdMob and Start.io SDKs asynchronously
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.Main).launch {
            // 1. Initialize Google Mobile Ads (AdMob)
            try {
                MobileAds.initialize(appContext) { initStatus ->
                    Log.d(TAG, "Google AdMob initialized successfully: $initStatus")
                    preloadAdMobInterstitial(appContext)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google AdMob init error: ${e.message}")
            }

            // 2. Initialize Start.io (with auto-consent and splash suppression)
            try {
                try {
                    StartAppSDK.setUserConsent(appContext, "pas", System.currentTimeMillis(), true)
                } catch (_: Exception) {}

                StartAppSDK.enableReturnAds(false)
                StartAppSDK.init(appContext, AdConfig.STARTAPP_APP_ID, false)
                StartAppAd.disableSplash()
                Log.d(TAG, "Start.io initialized successfully with App ID: ${AdConfig.STARTAPP_APP_ID}")
                preloadStartIoInterstitial(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Start.io init error: ${e.message}")
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
                    Log.w(TAG, "Google AdMob Interstitial failed to load: ${loadAdError.message}. Start.io ready as backup.")
                }
            }
        )
    }

    /**
     * Background Preload Start.io Interstitial
     */
    fun preloadStartIoInterstitial(context: Context) {
        val ad = StartAppAd(context)
        ad.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(p0: Ad) {
                startIoPreloadedAd = ad
                isStartIoLoaded = true
                Log.d(TAG, "Start.io Interstitial Preloaded successfully!")
            }

            override fun onFailedToReceiveAd(p0: Ad?) {
                isStartIoLoaded = false
                Log.w(TAG, "Start.io Interstitial failed to load: ${p0?.errorMessage}")
            }
        })
    }

    /**
     * Show full-screen interstitial ad with frequency capping (AdMob First -> Start.io Secondary)
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
            // Keep ads preloaded in background
            if (admobInterstitialAd == null) preloadAdMobInterstitial(activity)
            if (!isStartIoLoaded) preloadStartIoInterstitial(activity)
            onComplete()
        }
    }

    /**
     * Smart Waterfall Show:
     * 1. Try Google AdMob (High Revenue)
     * 2. If AdMob unavailable, Try Start.io (100% Fill Rate)
     * 3. If neither available, proceed immediately (Zero User Lag)
     */
    fun showInterstitial(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        val admobAd = admobInterstitialAd

        // LEVEL 1: Try Google AdMob
        if (admobAd != null) {
            admobAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial Dismissed/Closed by user")
                    admobInterstitialAd = null
                    preloadAdMobInterstitial(activity)
                    onComplete()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "AdMob Failed to show: ${adError.message}. Falling back to Start.io.")
                    admobInterstitialAd = null
                    preloadAdMobInterstitial(activity)
                    showStartIoFallback(activity, onComplete)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial Displayed Full Screen!")
                }
            }
            admobAd.show(activity)
            return
        }

        // LEVEL 2: Fallback to Start.io
        Log.d(TAG, "AdMob not ready yet, serving Start.io fallback ad...")
        showStartIoFallback(activity, onComplete)
    }

    /**
     * Show Start.io Fallback Interstitial
     */
    private fun showStartIoFallback(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        val startAd = startIoPreloadedAd
        if (isStartIoLoaded && startAd != null) {
            startAd.showAd(object : AdDisplayListener {
                override fun adHidden(p0: Ad?) {
                    Log.d(TAG, "Start.io Fallback Ad Closed")
                    isStartIoLoaded = false
                    startIoPreloadedAd = null
                    preloadStartIoInterstitial(activity)
                    preloadAdMobInterstitial(activity)
                    onComplete()
                }

                override fun adDisplayed(p0: Ad?) {
                    Log.d(TAG, "Start.io Fallback Ad Displayed!")
                }

                override fun adClicked(p0: Ad?) {
                    Log.d(TAG, "Start.io Fallback Ad Clicked!")
                }

                override fun adNotDisplayed(p0: Ad?) {
                    Log.w(TAG, "Start.io Fallback Ad Not Displayed: ${p0?.errorMessage}")
                    isStartIoLoaded = false
                    startIoPreloadedAd = null
                    preloadStartIoInterstitial(activity)
                    preloadAdMobInterstitial(activity)
                    onComplete()
                }
            })
        } else {
            // Direct Start.io load attempt
            val directAd = StartAppAd(activity)
            directAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(p0: Ad) {
                    directAd.showAd(object : AdDisplayListener {
                        override fun adHidden(ad: Ad?) { onComplete() }
                        override fun adDisplayed(ad: Ad?) {}
                        override fun adClicked(ad: Ad?) {}
                        override fun adNotDisplayed(ad: Ad?) { onComplete() }
                    })
                }

                override fun onFailedToReceiveAd(p0: Ad?) {
                    preloadAdMobInterstitial(activity)
                    onComplete()
                }
            })
        }
    }
}

