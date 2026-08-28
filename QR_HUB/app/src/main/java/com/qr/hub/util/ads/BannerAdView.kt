package com.qr.hub.util.ads

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
 * High-Performance Hybrid Banner Composable:
 * - Priority 1: Google AdMob Banner (AdConfig.ADMOB_BANNER_ID)
 * - Fallback: Start.io Banner (if AdMob is pending review or fails to fill)
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
            // Primary Google AdMob Banner
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

                    val adView = AdView(ctx).apply {
                        adUnitId = AdConfig.ADMOB_BANNER_ID
                        setAdSize(AdSize.BANNER)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                Log.d(TAG, "Google AdMob Banner loaded successfully!")
                                isAdMobLoaded = true
                                useStartIoFallback = false
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                Log.w(TAG, "Google AdMob Banner failed to load (${error.code}: ${error.message}). Falling back to Start.io.")
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
                    // Update if needed
                }
            )
        }
    }
}
