package com.qr.hub.util.ads

/**
 * Ad Configuration for Unity Ads (Live APK) & Google AdMob
 */
object AdConfig {
    // ----------------------------------------------------
    // UNITY ADS (Instant Real Live Commercial Ads on APK)
    // ----------------------------------------------------
    const val UNITY_GAME_ID = "800361444"
    const val UNITY_INTERSTITIAL_PLACEMENT = "Interstitial_Android"
    const val UNITY_BANNER_PLACEMENT = "Banner_Android"
    const val UNITY_TEST_MODE = false // Set to false for REAL LIVE COMMERCIAL ADS!

    // ----------------------------------------------------
    // GOOGLE ADMOB (For Google Play Store Release)
    // ----------------------------------------------------
    const val ADMOB_APP_ID = "ca-app-pub-7266647940952906~4589127679"
    const val ADMOB_BANNER_ID = "ca-app-pub-7266647940952906/5664708416"
    const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-7266647940952906/4573998340"

    enum class AdProvider { UNITY, ADMOB }

    // Active ad network: UNITY for direct live real ads right now!
    var activeProvider: AdProvider = AdProvider.UNITY
}
