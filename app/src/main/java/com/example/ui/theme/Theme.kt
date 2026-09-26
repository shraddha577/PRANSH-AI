package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class JarvisColors(
  val arcPrimary: Color = ArcCyanBright,
  val arcGlow: Color = ArcCyanGlow,
  val arcCore: Color = ArcCyanCore,
  val accentSecondary: Color = ElectricBlue,
  val warningGold: Color = StarkGold,
  val alertCrimson: Color = NeonCrimson,
  val healthyEmerald: Color = CyberEmerald,
  val backgroundVoid: Color = VoidBlack,
  val surfaceHud: Color = SurfaceHud,
  val surfaceCard: Color = SurfaceCard,
  val textPrimary: Color = TextCyanWhite,
  val textMuted: Color = TextCyanMuted,
  val borderNormal: Color = BorderCyan,
  val borderActive: Color = BorderCyanActive,
  val gridOverlay: Color = GridLine
)

val LocalJarvisColors = staticCompositionLocalOf { JarvisColors() }

private val DarkColorScheme = darkColorScheme(
  primary = ArcCyanBright,
  onPrimary = VoidBlack,
  primaryContainer = SurfaceCard,
  onPrimaryContainer = TextCyanWhite,
  secondary = ElectricBlue,
  onSecondary = VoidBlack,
  secondaryContainer = SurfaceHud,
  onSecondaryContainer = TextCyanWhite,
  tertiary = StarkGold,
  onTertiary = VoidBlack,
  background = VoidBlack,
  onBackground = TextCyanWhite,
  surface = SurfaceHud,
  onSurface = TextCyanWhite,
  surfaceVariant = SurfaceCard,
  onSurfaceVariant = TextCyanMuted,
  error = NeonCrimson,
  onError = VoidBlack
)

enum class JarvisThemeMode {
  ARC_CYAN,
  STARK_GOLD,
  NEON_CRIMSON,
  CYBER_EMERALD
}

fun getJarvisColorsForTheme(mode: JarvisThemeMode): JarvisColors {
  return when (mode) {
    JarvisThemeMode.ARC_CYAN -> JarvisColors(
      arcPrimary = ArcCyanBright,
      arcGlow = ArcCyanGlow,
      arcCore = ArcCyanCore,
      borderNormal = BorderCyan,
      borderActive = BorderCyanActive
    )
    JarvisThemeMode.STARK_GOLD -> JarvisColors(
      arcPrimary = StarkGold,
      arcGlow = StarkGoldDim,
      arcCore = StarkAmber,
      borderNormal = Color(0x4DFFD700),
      borderActive = Color(0xCCFFD700)
    )
    JarvisThemeMode.NEON_CRIMSON -> JarvisColors(
      arcPrimary = NeonCrimson,
      arcGlow = CrimsonGlow,
      arcCore = Color(0xFFFF4081),
      borderNormal = Color(0x4DFF2A6D),
      borderActive = Color(0xCCFF2A6D)
    )
    JarvisThemeMode.CYBER_EMERALD -> JarvisColors(
      arcPrimary = CyberEmerald,
      arcGlow = EmeraldGlow,
      arcCore = Color(0xFF00E676),
      borderNormal = Color(0x4D05FFA1),
      borderActive = Color(0xCC05FFA1)
    )
  }
}

@Composable
fun MyApplicationTheme(
  themeMode: JarvisThemeMode = JarvisThemeMode.ARC_CYAN,
  content: @Composable () -> Unit
) {
  val jarvisColors = getJarvisColorsForTheme(themeMode)
  val colorScheme = DarkColorScheme.copy(
    primary = jarvisColors.arcPrimary,
    primaryContainer = jarvisColors.surfaceCard
  )

  CompositionLocalProvider(LocalJarvisColors provides jarvisColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
