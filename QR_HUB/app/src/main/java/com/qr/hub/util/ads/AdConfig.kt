package com.qr.hub.util.ads

import com.qr.hub.BuildConfig

/**
 * Google AdMob Configuration & Ad Unit IDs
 */
object AdConfig {
    // Official Live AdMob IDs
    const val APP_ID = "ca-app-pub-7266647940952906~4589127679"
    const val LIVE_BANNER_ID = "ca-app-pub-7266647940952906/5664708416"
    const val LIVE_INTERSTITIAL_ID = "ca-app-pub-7266647940952906/4573998340"

    // Official Google Test Ad Unit IDs (used during debug so AdMob account is 100% safe from invalid traffic)
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    /**
     * Active Banner Ad Unit ID based on build mode
     */
    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else LIVE_BANNER_ID

    /**
     * Active Interstitial Ad Unit ID based on build mode
     */
    val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else LIVE_INTERSTITIAL_ID
}
