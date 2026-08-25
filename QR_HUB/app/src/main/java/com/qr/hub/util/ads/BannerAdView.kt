package com.qr.hub.util.ads

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.qr.hub.util.Ink900
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

/**
 * Jetpack Compose Composable for Banner Ads (Unity Ads primary with AdMob fallback)
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(Ink900),
            factory = { context ->
                val activity = context as? Activity
                if (AdConfig.activeProvider == AdConfig.AdProvider.UNITY && activity != null) {
                    BannerView(activity, AdConfig.UNITY_BANNER_PLACEMENT, UnityBannerSize(320, 50)).apply {
                        load()
                    }
                } else {
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = AdConfig.ADMOB_BANNER_ID
                        loadAd(AdRequest.Builder().build())
                    }
                }
            }
        )
    }
}
