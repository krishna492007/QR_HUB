package com.qr.hub.util.ads

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.startapp.sdk.ads.banner.Banner

/**
 * High-Performance Hybrid Banner AdView:
 * 1. Primary: Google AdMob Banner (AdConfig.ADMOB_BANNER_ID)
 * 2. Fallback: Start.io Banner if AdMob fails or is warming up
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    var isAdMobFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdMobFailed) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdConfig.ADMOB_BANNER_ID
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d("QR_HUB_BannerAdView", "Google AdMob Banner loaded successfully!")
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.w("QR_HUB_BannerAdView", "AdMob Banner failed (${loadAdError.code}: ${loadAdError.message}). Switching to Start.io fallback.")
                                isAdMobFailed = true
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        } else {
            // Fallback to Start.io Banner
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent),
                factory = { context ->
                    Banner(context).apply {
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                }
            )
        }
    }
}
