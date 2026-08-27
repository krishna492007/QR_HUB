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

// ── Light Palette (Canonical Ceramic & Warm Gold from Design System) ──
val CeramicCanvas = Color(0xFFFAF8F5)      // Screen background (+ 0.035 opacity grain dots)
val CeramicSurface = Color(0xFFFFFFFF)     // Cards, tiles, surfaces
val CeramicSurface2 = Color(0xFFF3EFE9)    // Icon wraps, toggle track, resting CTA
val CeramicBorder = Color(0x0F000000)      // rgba(0,0,0,0.06) Hairline
val CeramicBorderStrong = Color(0x1A000000)// rgba(0,0,0,0.10)
val CeramicGold = Color(0xFFC48A37)        // Primary accent — icons, borders, brackets
val CeramicAmber = Color(0xFFE5B56A)       // Gradient top-stop (#F2D299 / #E5B56A)
val CeramicGoldDim = Color(0x1AC48A37)     // rgba(196,138,55,0.10)
val CeramicGoldDim2 = Color(0x4DC48A37)    // rgba(196,138,55,0.30)
val CeramicTileBorderSelected = Color(0x73C48A37) // rgba(196,138,55,0.45)
val CeramicTeal = Color(0xFF22A897)        // Secondary accent — scanned badges, live dot
val CeramicTealDim = Color(0x1A22A897)     // rgba(34,168,151,0.10)
val CeramicTealDim2 = Color(0x4722A897)    // rgba(34,168,151,0.28)
val CeramicRust = Color(0xFFB6472E)        // Filled-favorite heart / warm danger
val CeramicRustDim = Color(0x1AB6472E)     // rgba(182,71,46,0.10)

val CeramicInk = Color(0xFF16161A)         // Text primary
val CeramicSlate = Color(0xFF686873)       // Text secondary
val CeramicSand = Color(0xFF9C9CA6)        // Text muted / sand
val CeramicCtaInk = Color(0xFF1A1208)      // Text on gold CTA button

// ── Border & Line Gradients (Dark Mode) ──
val BorderLine = Color(0xFFFFFFFF).copy(alpha = 0.07f)
val BorderLineStrong = Color(0xFFFFFFFF).copy(alpha = 0.14f)

// ── Accent Colors — Amber (Signature Dark Mode) ──
val AmberPrimary = Color(0xFFD9A257)
val AmberSoft = Color(0xFFEFCC8F)
val AmberDim = Color(0xFFD9A257).copy(alpha = 0.14f)
val AmberDim2 = Color(0xFFD9A257).copy(alpha = 0.28f)

// ── Accent Colors — Cyan (Live / Scanner Dark Mode) ──
val CyanAccent = Color(0xFF3FD8C4)
val CyanDim = Color(0xFF3FD8C4).copy(alpha = 0.14f)
val CyanDim2 = Color(0xFF3FD8C4).copy(alpha = 0.30f)

// ── Text Colors (Dark Mode) ──
val TextPrimary = Color(0xFFF1EFEA)
val TextSecondary = Color(0xFF94949E)
val TextTertiary = Color(0xFF5C5D66)

// ── Semantic Colors ──
val DangerRed = Color(0xFFE2664D)
val SuccessGreen = Color(0xFF22C55E)

// ── Dynamic Theme Token Resolvers ──
fun appBg(isDark: Boolean): Color = if (isDark) Ink950 else CeramicCanvas
fun appNavBg(isDark: Boolean): Color = if (isDark) Ink900 else CeramicSurface.copy(alpha = 0.85f)
fun appCardBg(isDark: Boolean): Color = if (isDark) Ink800 else CeramicSurface
fun appElevatedBg(isDark: Boolean): Color = if (isDark) Ink750 else CeramicSurface2
fun appDialogBg(isDark: Boolean): Color = if (isDark) Ink850 else CeramicSurface
fun appBorder(isDark: Boolean): Color = if (isDark) BorderLine else CeramicBorder
fun appBorderStrong(isDark: Boolean): Color = if (isDark) BorderLineStrong else CeramicBorderStrong
fun appTextPrimary(isDark: Boolean): Color = if (isDark) TextPrimary else CeramicInk
fun appTextSecondary(isDark: Boolean): Color = if (isDark) TextSecondary else CeramicSlate
fun appTextTertiary(isDark: Boolean): Color = if (isDark) TextTertiary else CeramicSand
fun appGoldPrimary(isDark: Boolean): Color = if (isDark) AmberPrimary else CeramicGold
fun appGoldSoft(isDark: Boolean): Color = if (isDark) AmberSoft else CeramicAmber
fun appGoldDim(isDark: Boolean): Color = if (isDark) AmberDim else CeramicGoldDim
fun appGoldDim2(isDark: Boolean): Color = if (isDark) AmberDim2 else CeramicGoldDim2
fun appTealAccent(isDark: Boolean): Color = if (isDark) CyanAccent else CeramicTeal
fun appTealDim(isDark: Boolean): Color = if (isDark) CyanDim else CeramicTealDim
fun appHeartColor(isDark: Boolean, isFilled: Boolean): Color = when {
    isFilled -> if (isDark) DangerRed else CeramicRust
    else -> if (isDark) TextTertiary else CeramicSand
}

// ── Gradients ──
val AmberCtaGradient = Brush.verticalGradient(
    colors = listOf(AmberSoft, AmberPrimary)
)
val CeramicCtaGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFF2D299), CeramicGold)
)

val CeramicTileWashGradient = Brush.verticalGradient(
    colors = listOf(Color(0x1FD9A257), Color(0xFFFFFFFF))
)

fun appCtaGradient(isDark: Boolean): Brush = if (isDark) AmberCtaGradient else CeramicCtaGradient
fun appCtaTextColor(isDark: Boolean): Color = if (isDark) Color(0xFF20140A) else CeramicCtaInk

// ── Legacy Compatibility Aliases ──
val CeramicCard = CeramicSurface
val CeramicElevated = CeramicSurface2
val CeramicBars = CeramicSurface2
val DarkPrimary = Ink950
val DarkSurface = Ink800
val DarkSecondary = Ink850
val DarkAccent = AmberPrimary
val DarkTextPrimary = TextPrimary
val DarkTextSecondary = TextSecondary
val LightPrimary = CeramicCanvas
val LightSecondary = CeramicSurface2
val LightSurface = CeramicSurface
val LightAccent = CeramicGold
val LightTextPrimary = CeramicInk
val LightTextSecondary = CeramicSlate
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
val GradientBrush = AmberCtaGradient

// Dotfield matrix background modifier
fun Modifier.dotfieldBackground(isDark: Boolean = true): Modifier = this
    .background(if (isDark) Ink950 else CeramicCanvas)
    .drawBehind {
        val dotRadius = 1.dp.toPx()
        val spacing = 14.dp.toPx()
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
