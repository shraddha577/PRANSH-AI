package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalJarvisColors
import kotlin.math.sin

@Composable
fun AudioWaveformBar(
  modifier: Modifier = Modifier,
  isActive: Boolean = false,
  amplitude: Float = 0f,
  height: Dp = 36.dp,
  barCount: Int = 24,
  customColor: Color? = null
) {
  val colors = LocalJarvisColors.current
  val barColor = customColor ?: colors.arcPrimary

  val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "WavePhase"
  )

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
  ) {
    val totalWidth = size.width
    val totalHeight = size.height
    val barWidth = (totalWidth / (barCount * 1.8f)).coerceAtLeast(3f)
    val spacing = (totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

    for (i in 0 until barCount) {
      val x = i * (barWidth + spacing)
      val normalizedIndex = i.toFloat() / barCount

      // Calculate bar height based on activity and amplitude
      val baseWave = sin(normalizedIndex * 5f + phase).toFloat() * 0.5f + 0.5f
      val barHeightFraction = if (isActive) {
        val ampBoost = amplitude.coerceIn(0.1f, 1f)
        (0.2f + baseWave * 0.8f * ampBoost).coerceIn(0.1f, 1f)
      } else {
        0.08f + sin(normalizedIndex * 3f + phase * 0.5f).toFloat() * 0.04f
      }

      val barH = totalHeight * barHeightFraction
      val y = (totalHeight - barH) / 2f

      drawRoundRect(
        color = barColor.copy(alpha = if (isActive) 0.9f else 0.35f),
        topLeft = Offset(x, y),
        size = Size(barWidth, barH),
        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
      )
    }
  }
}
