package com.qr.hub.util

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ============================================
// QR HUB TOKENS — Ink & Ceramic Luxury Palette
// ============================================

// ── Dark Palette (Obsidian & Gold) ──
val Ink950 = Color(0xFF0A0A0D)       // Base app background
val Ink900 = Color(0xFF131318)       // Bottom nav & bars
val Ink850 = Color(0xFF17181F)       // Elevated dialogs & menus
val Ink800 = Color(0xFF1D1E27)       // Tiles, cards, icon buttons
val Ink750 = Color(0xFF25262F)       // Icon wrap background
val Ink700 = Color(0xFF2E2F39)       // Subtle elevated borders

// ── Light Palette (Warm Ceramic & Brushed Gold) ──
val CeramicCanvas = Color(0xFFFAF8F5)      // Warm Oyster / Ceramic Canvas
val CeramicBars = Color(0xFFF3EFE9)        // Nav & Bars Surface
val CeramicElevated = Color(0xFFEDE8E0)    // Icon wraps & elevated pill background
val CeramicCard = Color(0xFFFFFFFF)        // Ceramic Card Surface
val CeramicBorder = Color(0xFF000000).copy(alpha = 0.07f)
val CeramicBorderStrong = Color(0xFF000000).copy(alpha = 0.14f)
val CeramicTextPrimary = Color(0xFF16161A) // Deep Obsidian Ink
val CeramicTextSecondary = Color(0xFF686873) // Titanium Slate
val CeramicTextTertiary = Color(0xFF9C9CA6) // Warm Sand Grey
val CeramicGold = Color(0xFFC48A37)        // Brushed Champagne Gold
val CeramicGoldSoft = Color(0xFFE5B56A)    // Warm Amber Soft
val CeramicGoldDim = Color(0xFFC48A37).copy(alpha = 0.12f)
val CeramicGoldDim2 = Color(0xFFC48A37).copy(alpha = 0.24f)

// ── Border & Line Gradients ──
val BorderLine = Color(0xFFFFFFFF).copy(alpha = 0.07f)
val BorderLineStrong = Color(0xFFFFFFFF).copy(alpha = 0.14f)

// ── Accent Colors — Amber (Signature) ──
val AmberPrimary = Color(0xFFD9A257)
val AmberSoft = Color(0xFFEFCC8F)
val AmberDim = Color(0xFFD9A257).copy(alpha = 0.14f)
val AmberDim2 = Color(0xFFD9A257).copy(alpha = 0.28f)

// ── Accent Colors — Cyan (Live / Scanner) ──
val CyanAccent = Color(0xFF3FD8C4)
val CyanDim = Color(0xFF3FD8C4).copy(alpha = 0.14f)
val CyanDim2 = Color(0xFF3FD8C4).copy(alpha = 0.30f)

// ── Text Colors ──
val TextPrimary = Color(0xFFF1EFEA)
val TextSecondary = Color(0xFF94949E)
val TextTertiary = Color(0xFF5C5D66)

// ── Semantic Colors ──
val DangerRed = Color(0xFFE2664D)
val SuccessGreen = Color(0xFF22C55E)

// ── Dynamic Theme Token Resolvers ──
fun appBg(isDark: Boolean): Color = if (isDark) Ink950 else CeramicCanvas
fun appNavBg(isDark: Boolean): Color = if (isDark) Ink900 else CeramicBars
fun appCardBg(isDark: Boolean): Color = if (isDark) Ink800 else CeramicCard
fun appElevatedBg(isDark: Boolean): Color = if (isDark) Ink750 else CeramicElevated
fun appDialogBg(isDark: Boolean): Color = if (isDark) Ink850 else CeramicCard
fun appBorder(isDark: Boolean): Color = if (isDark) BorderLine else CeramicBorder
fun appBorderStrong(isDark: Boolean): Color = if (isDark) BorderLineStrong else CeramicBorderStrong
fun appTextPrimary(isDark: Boolean): Color = if (isDark) TextPrimary else CeramicTextPrimary
fun appTextSecondary(isDark: Boolean): Color = if (isDark) TextSecondary else CeramicTextSecondary
fun appTextTertiary(isDark: Boolean): Color = if (isDark) TextTertiary else CeramicTextTertiary
fun appGoldPrimary(isDark: Boolean): Color = if (isDark) AmberPrimary else CeramicGold
fun appGoldSoft(isDark: Boolean): Color = if (isDark) AmberSoft else CeramicGoldSoft
fun appGoldDim(isDark: Boolean): Color = if (isDark) AmberDim else CeramicGoldDim
fun appGoldDim2(isDark: Boolean): Color = if (isDark) AmberDim2 else CeramicGoldDim2

// ── Legacy Compatibility Aliases ──
val DarkPrimary = Ink950
val DarkSurface = Ink800
val DarkSecondary = Ink850
val DarkAccent = AmberPrimary
val DarkTextPrimary = TextPrimary
val DarkTextSecondary = TextSecondary
val LightPrimary = CeramicCanvas
val LightSecondary = CeramicBars
val LightSurface = CeramicCard
val LightAccent = CeramicGold
val LightTextPrimary = CeramicTextPrimary
val LightTextSecondary = CeramicTextSecondary
val GradientStart = AmberSoft
val GradientEnd = AmberPrimary
val ErrorRed = DangerRed
val WarningYellow = AmberPrimary
val BlueAccent = CyanAccent
val InfoBlue = CyanAccent
val TealAccent = CyanAccent
val PurpleBadge = AmberPrimary
val White = Color.White
val DeepNavy = Ink950
val CardNavy = Ink800
val LightGray = TextSecondary

// Amber CTA Gradient Brush
val AmberCtaGradient = Brush.verticalGradient(
    colors = listOf(AmberSoft, AmberPrimary)
)
val CeramicCtaGradient = Brush.verticalGradient(
    colors = listOf(CeramicGoldSoft, CeramicGold)
)

val GradientBrush = AmberCtaGradient

fun appCtaGradient(isDark: Boolean): Brush = if (isDark) AmberCtaGradient else CeramicCtaGradient

// Dotfield matrix background modifier
fun Modifier.dotfieldBackground(isDark: Boolean = true): Modifier = this
    .background(if (isDark) Ink950 else CeramicCanvas)
    .drawBehind {
        val dotRadius = 1.dp.toPx()
        val spacing = 16.dp.toPx()
        val dotColor = if (isDark) Color.White.copy(alpha = 0.045f) else Color.Black.copy(alpha = 0.035f)
        
        var x = 0f
        while (x < size.width) {
            var y = 0f
            while (y < size.height) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
                y += spacing
            }
            x += spacing
        }
    }
