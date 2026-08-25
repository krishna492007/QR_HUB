package com.qr.hub.util.ads

import android.app.Activity
import android.content.Context
import android.util.Log
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
 * Singleton AdManager powered by Start.io for Instant Live Ads on direct APK
 */
object AdManager {
    private const val TAG = "QR_HUB_AdManager"

    private var isInitialized = false
    private var preloadedAd: StartAppAd? = null
    private var isAdLoaded = false

    // Action counter for smart frequency capping (shows ad once every 2 actions)
    private val actionCounter = AtomicInteger(0)

    /**
     * Initialize Start.io SDK on Main Thread
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Initialize Start.io with App ID
                StartAppSDK.init(appContext, AdConfig.STARTAPP_APP_ID, false)
                StartAppAd.disableSplash()
                StartAppSDK.enableReturnAds(false)
                Log.d(TAG, "Start.io SDK initialized successfully with App ID: ${AdConfig.STARTAPP_APP_ID}")
                preloadInterstitial(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Start.io init error: ${e.message}")
            }
        }
    }

    /**
     * Preload Full-Screen Interstitial Ad
     */
    fun preloadInterstitial(context: Context) {
        val ad = StartAppAd(context)
        ad.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
            override fun onReceiveAd(p0: Ad) {
                preloadedAd = ad
                isAdLoaded = true
                Log.d(TAG, "Start.io Interstitial Preloaded successfully!")
            }

            override fun onFailedToReceiveAd(p0: Ad?) {
                isAdLoaded = false
                Log.w(TAG, "Start.io Interstitial failed to load: ${p0?.errorMessage}")
            }
        })
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
            if (!isAdLoaded) preloadInterstitial(activity)
            onComplete()
        }
    }

    /**
     * Show full-screen interstitial ad
     */
    fun showInterstitial(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        val ad = preloadedAd
        if (isAdLoaded && ad != null) {
            ad.showAd(object : AdDisplayListener {
                override fun adHidden(p0: Ad?) {
                    Log.d(TAG, "Start.io Ad Hidden/Closed")
                    isAdLoaded = false
                    preloadedAd = null
                    preloadInterstitial(activity)
                    onComplete()
                }

                override fun adDisplayed(p0: Ad?) {
                    Log.d(TAG, "Start.io Ad Displayed Full Screen!")
                }

                override fun adClicked(p0: Ad?) {
                    Log.d(TAG, "Start.io Ad Clicked!")
                }

                override fun adNotDisplayed(p0: Ad?) {
                    Log.w(TAG, "Start.io Ad Not Displayed: ${p0?.errorMessage}")
                    isAdLoaded = false
                    preloadedAd = null
                    preloadInterstitial(activity)
                    onComplete()
                }
            })
        } else {
            // Direct load and show if not preloaded
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
                    onComplete()
                }
            })
        }
    }
}
