package com.qr.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qr.hub.generate.*
import com.qr.hub.history.HistoryScreen
import com.qr.hub.history.HistoryDetailScreen
import com.qr.hub.privacy.PrivacyPolicyScreen
import com.qr.hub.privacy.AboutLegalScreen
import com.qr.hub.privacy.TermsScreen
import com.qr.hub.data.repository.HistoryRepository
import com.qr.hub.model.ScannedQR
import com.qr.hub.scanner.ResultScreen
import com.qr.hub.scanner.ScannerScreen
import com.qr.hub.ui.theme.QRHUBTheme
import com.qr.hub.util.*

import com.qr.hub.util.ads.AdManager

private val DarkBg = DarkPrimary
private val LightBg = LightPrimary

sealed class Screen {
    object ScannerTab : Screen()
    object GenerateTab : Screen()
    object HistoryTab : Screen()
    object PrivacyPolicy : Screen()
    object AboutLegal : Screen()
    object TermsOfService : Screen()
    data class Result(val data: ScannedQR.RawResult) : Screen()
    data class HistoryDetail(val itemId: Long) : Screen()
    // Generate QR sub-screens
    data class GenerateForm(val type: QRType) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            QRHUBTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ScannerTab) }
    val context = LocalContext.current
    var themeMode by remember { mutableStateOf(ThemePreferenceManager.getThemeMode(context)) }
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    // Handle system back button
    BackHandler(enabled = true) {
        when (currentScreen) {
            is Screen.Result -> currentScreen = Screen.ScannerTab
            is Screen.GenerateForm -> currentScreen = Screen.GenerateTab
            is Screen.HistoryDetail -> currentScreen = Screen.HistoryTab
            is Screen.PrivacyPolicy -> currentScreen = Screen.AboutLegal
            is Screen.TermsOfService -> currentScreen = Screen.AboutLegal
            is Screen.AboutLegal -> currentScreen = Screen.ScannerTab
            is Screen.ScannerTab -> (context as? android.app.Activity)?.finish()
            else -> currentScreen = Screen.ScannerTab // GenerateTab, HistoryTab
        }
    }

    // Determine if bottom nav should be shown — hide on result, generate forms, history detail, privacy policy, and terms
    val showBottomNav = currentScreen !is Screen.Result && currentScreen !is Screen.GenerateForm && currentScreen !is Screen.HistoryDetail && currentScreen !is Screen.PrivacyPolicy && currentScreen !is Screen.TermsOfService

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBg(isDark))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main content area with smooth page transitions
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                         slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> (height * 0.02f).toInt() }) togetherWith
                        (fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)))
                    },
                    label = "AppScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.ScannerTab -> ScannerScreen(
                            isDark = isDark,
                            onNavigateToResult = { data ->
                                currentScreen = Screen.Result(data)
                            },
                            onBack = { (context as? ComponentActivity)?.finish() },
                            onPrivacyPolicyClick = { currentScreen = Screen.AboutLegal }
                        )
                        is Screen.GenerateTab -> GenerateQrTypeSelectionScreen(
                            isDark = isDark,
                            onTypeSelected = { type ->
                                currentScreen = Screen.GenerateForm(type)
                            },
                            onPrivacyPolicyClick = { currentScreen = Screen.AboutLegal }
                        )
                        is Screen.HistoryTab -> HistoryScreen(
                            isDark = isDark,
                            onItemClick = { item ->
                                currentScreen = Screen.HistoryDetail(item.id)
                            },
                            onBackClick = { currentScreen = Screen.ScannerTab },
                            onPrivacyPolicyClick = { currentScreen = Screen.AboutLegal }
                        )
                        is Screen.HistoryDetail -> {
                            val itemId = (screen as Screen.HistoryDetail).itemId
                            HistoryDetailWrapper(itemId = itemId, isDark = isDark, onBackClick = { currentScreen = Screen.HistoryTab })
                        }
                        is Screen.Result -> ResultScreen(
                            result = (screen as Screen.Result).data,
                            isDark = isDark,
                            onBack = { currentScreen = Screen.ScannerTab }
                        )
                        is Screen.GenerateForm -> {
                            val type = (screen as Screen.GenerateForm).type
                            GenerateQrFormContainer(
                                type = type,
                                isDark = isDark,
                                onBack = { currentScreen = Screen.GenerateTab }
                            )
                        }
                        is Screen.PrivacyPolicy -> PrivacyPolicyScreen(
                            isDark = isDark,
                            onBackClick = { currentScreen = Screen.AboutLegal }
                        )
                        is Screen.AboutLegal -> AboutLegalScreen(
                            isDark = isDark,
                            currentThemeMode = themeMode,
                            onThemeModeChange = { newMode ->
                                themeMode = newMode
                                ThemePreferenceManager.setThemeMode(context, newMode)
                            },
                            onBackClick = { currentScreen = Screen.ScannerTab },
                            onPrivacyPolicyClick = { currentScreen = Screen.PrivacyPolicy },
                            onTermsClick = { currentScreen = Screen.TermsOfService }
                        )
                        is Screen.TermsOfService -> TermsScreen(
                            isDark = isDark,
                            onBackClick = { currentScreen = Screen.AboutLegal }
                        )
                    }
                }
            }

            // Bottom navigation
            if (showBottomNav) {
                BottomNavigationBar(
                    currentTab = currentScreen,
                    onTabSelected = { tab -> currentScreen = tab },
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun GenerateQrFormContainer(
    type: QRType,
    isDark: Boolean,
    onBack: () -> Unit
) {
    when (type) {
        QRType.Bulk -> BatchQrGeneratorScreen(isDark = isDark, onBack = onBack)
        QRType.Barcode -> GenerateBarcodeScreen(isDark = isDark, onBack = onBack)
        QRType.Text -> GenerateTextQrScreen(isDark = isDark, onBack = onBack)
        QRType.URL -> GenerateUrlQrScreen(isDark = isDark, onBack = onBack)
        QRType.UPI -> GenerateUpiQrScreen(isDark = isDark, onBack = onBack)
        QRType.WhatsApp -> GenerateWhatsAppQrScreen(isDark = isDark, onBack = onBack)
        QRType.WAGroup -> GenerateWAGroupQrScreen(isDark = isDark, onBack = onBack)
        QRType.Phone -> GeneratePhoneQrScreen(isDark = isDark, onBack = onBack)
        QRType.SMS -> GenerateSmsQrScreen(isDark = isDark, onBack = onBack)
        QRType.Email -> GenerateEmailQrScreen(isDark = isDark, onBack = onBack)
        QRType.Contact -> GenerateContactQrScreen(isDark = isDark, onBack = onBack)
        QRType.WiFi -> GenerateWifiQrScreen(isDark = isDark, onBack = onBack)
        QRType.Location -> GenerateLocationQrScreen(isDark = isDark, onBack = onBack)
        QRType.Event -> GenerateEventQrScreen(isDark = isDark, onBack = onBack)
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: Screen,
    onTabSelected: (Screen) -> Unit,
    isDark: Boolean
) {
    val actualTab = when (currentTab) {
        is Screen.GenerateForm -> Screen.GenerateTab
        else -> currentTab
    }

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appNavBg(isDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, appBorder(isDark))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Generate
            BottomNavItem(
                icon = HtmlIcons.GenerateNav,
                label = "Generate",
                selected = actualTab == Screen.GenerateTab,
                isDark = isDark,
                onClick = { onTabSelected(Screen.GenerateTab) }
            )

            // Tab 2: Scan
            BottomNavItem(
                icon = HtmlIcons.ScanNav,
                label = "Scan",
                selected = actualTab == Screen.ScannerTab,
                isDark = isDark,
                onClick = { onTabSelected(Screen.ScannerTab) }
            )

            // Tab 3: History
            BottomNavItem(
                icon = HtmlIcons.HistoryNav,
                label = "History",
                selected = actualTab == Screen.HistoryTab,
                isDark = isDark,
                onClick = { onTabSelected(Screen.HistoryTab) }
            )

            // Tab 4: Settings
            BottomNavItem(
                icon = HtmlIcons.SettingsNav,
                label = "Settings",
                selected = actualTab == Screen.AboutLegal,
                isDark = isDark,
                onClick = { onTabSelected(Screen.AboutLegal) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    isDark: Boolean = true,
    onClick: () -> Unit
) {
    val activeColor = appGoldPrimary(isDark)
    val inactiveColor = appTextTertiary(isDark)

    val animatedIconColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "NavIconColor"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "NavTextColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NavIconScale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedIconColor,
            modifier = Modifier
                .size(23.dp)
                .scale(iconScale)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = animatedTextColor,
            fontSize = 10.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(3.5.dp)
                .clip(CircleShape)
                .background(if (selected) activeColor else Color.Transparent)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    QRHUBTheme {
        var currentTab by remember { mutableStateOf<Screen>(Screen.ScannerTab) }
        BottomNavigationBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            isDark = false
        )
    }
}

@Composable
fun HistoryDetailWrapper(itemId: Long, isDark: Boolean = true, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { HistoryRepository(context) }
    var item by remember { mutableStateOf<com.qr.hub.data.model.HistoryItem?>(null) }

    LaunchedEffect(itemId) {
        item = repository.getById(itemId)
    }

    if (item != null) {
        HistoryDetailScreen(
            item = item!!,
            isDark = isDark,
            onBackClick = onBackClick
        )
    } else {
        // Loading state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBg(isDark)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = appGoldPrimary(isDark),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Loading...",
                    color = appTextSecondary(isDark),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
