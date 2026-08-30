package com.qr.hub.util.ads

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.startapp.sdk.ads.banner.Banner

private const val TAG = "BannerAdView"

/**
 * High-Performance Hybrid Adaptive Banner Composable:
 * - Priority 1: Google AdMob Adaptive Banner (AdConfig.ADMOB_BANNER_ID)
 * - Fallback: Start.io Banner (if AdMob has Error 3: No Fill or pending server activation)
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    var isAdMobLoaded by remember { mutableStateOf(false) }
    var useStartIoFallback by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (useStartIoFallback) {
            // Start.io Fallback Banner
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent),
                factory = { ctx ->
                    Banner(ctx).apply {
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                }
            )
        } else {
            // Primary Google AdMob Adaptive Banner
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent),
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val adaptiveSize = try {
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, screenWidthDp)
                    } catch (_: Exception) {
                        AdSize.BANNER
                    }

                    val adView = AdView(ctx).apply {
                        adUnitId = AdConfig.ADMOB_BANNER_ID
                        setAdSize(adaptiveSize)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                Log.d(TAG, "Google AdMob Adaptive Banner loaded successfully!")
                                isAdMobLoaded = true
                                useStartIoFallback = false
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                Log.w(TAG, "Google AdMob Banner failed to load (code: ${error.code}, message: ${error.message}). Falling back to Start.io.")
                                isAdMobLoaded = false
                                useStartIoFallback = true
                            }
                        }
                    }

                    val adRequest = AdRequest.Builder().build()
                    adView.loadAd(adRequest)
                    frameLayout.addView(adView)
                    frameLayout
                },
                update = {
                    // Recompose update if needed
                }
            )
        }
    }
}
