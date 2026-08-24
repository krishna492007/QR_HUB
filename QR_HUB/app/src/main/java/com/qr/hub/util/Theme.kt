package com.qr.hub.util

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ============================================
// QR HUB REDESIGN TOKENS (Ink & Amber Aesthetic)
// ============================================

// Dark Palette — Ink Tones
val Ink950 = Color(0xFF0A0A0D)       // Base app background
val Ink900 = Color(0xFF131318)       // Bottom nav & bars
val Ink850 = Color(0xFF17181F)       // Elevated dialogs & menus
val Ink800 = Color(0xFF1D1E27)       // Tiles, cards, icon buttons
val Ink750 = Color(0xFF25262F)       // Icon wrap background
val Ink700 = Color(0xFF2E2F39)       // Subtle elevated borders

// Border & Line Gradients
val BorderLine = Color(0xFFFFFFFF).copy(alpha = 0.07f)
val BorderLineStrong = Color(0xFFFFFFFF).copy(alpha = 0.14f)

// Accent Colors — Amber (Signature)
val AmberPrimary = Color(0xFFD9A257)
val AmberSoft = Color(0xFFEFCC8F)
val AmberDim = Color(0xFFD9A257).copy(alpha = 0.14f)
val AmberDim2 = Color(0xFFD9A257).copy(alpha = 0.28f)

// Accent Colors — Cyan (Live / Scanner)
val CyanAccent = Color(0xFF3FD8C4)
val CyanDim = Color(0xFF3FD8C4).copy(alpha = 0.14f)
val CyanDim2 = Color(0xFF3FD8C4).copy(alpha = 0.30f)

// Text Colors
val TextPrimary = Color(0xFFF1EFEA)
val TextSecondary = Color(0xFF94949E)
val TextTertiary = Color(0xFF5C5D66)

// Semantic Colors
val DangerRed = Color(0xFFE2664D)
val SuccessGreen = Color(0xFF22C55E)

// Legacy Compatibility Aliases
val DarkPrimary = Ink950
val DarkSurface = Ink800
val DarkSecondary = Ink850
val DarkAccent = AmberPrimary
val DarkTextPrimary = TextPrimary
val DarkTextSecondary = TextSecondary
val LightPrimary = Color(0xFFECEEEA)
val LightSecondary = Color(0xFFFBFBF9)
val LightSurface = Color(0xFFFFFFFF)
val LightAccent = Color(0xFFA8721E)
val LightTextPrimary = Color(0xFF16171A)
val LightTextSecondary = Color(0xFF63646A)
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

val GradientBrush = AmberCtaGradient

// Dotfield matrix background modifier
fun Modifier.dotfieldBackground(): Modifier = this
    .background(Ink950)
    .drawBehind {
        val dotRadius = 1.dp.toPx()
        val spacing = 16.dp.toPx()
        val dotColor = Color.White.copy(alpha = 0.045f)
        
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

