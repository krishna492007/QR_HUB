package com.qr.hub.util.ads

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "BannerAdView"

enum class BannerAdType {
    ADAPTIVE,          // Full width x ~50-90dp
    MEDIUM_RECTANGLE,  // 300x250dp Big Box
    LARGE              // 320x100dp Double Height
}

/**
 * 100% Pure Google AdMob Banner with Multi-Size Support
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    type: BannerAdType = BannerAdType.ADAPTIVE,
    showAdBadge: Boolean = false
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    var isAdMobLoaded by remember { mutableStateOf(false) }

    val adSize = remember(type, screenWidthDp) {
        when (type) {
            BannerAdType.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
            BannerAdType.LARGE -> AdSize.LARGE_BANNER
            BannerAdType.ADAPTIVE -> {
                try {
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
                } catch (_: Exception) {
                    AdSize.BANNER
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentSize()
        ) {
            if (showAdBadge && isAdMobLoaded) {
                Text(
                    text = "ADVERTISEMENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            AndroidView(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent),
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val adView = AdView(ctx).apply {
                        adUnitId = AdConfig.ADMOB_BANNER_ID
                        setAdSize(adSize)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                Log.d(TAG, "Google AdMob Banner ($type) loaded successfully!")
                                isAdMobLoaded = true
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                Log.w(TAG, "Google AdMob Banner ($type) failed to load (code: ${error.code}: ${error.message})")
                                isAdMobLoaded = false
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
