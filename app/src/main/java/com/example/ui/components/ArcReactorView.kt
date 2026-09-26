package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalJarvisColors
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class ReactorState {
  IDLE,
  LISTENING,
  PROCESSING,
  SPEAKING
}

@Composable
fun ArcReactorView(
  modifier: Modifier = Modifier,
  size: Dp = 280.dp,
  reactorState: ReactorState = ReactorState.IDLE,
  audioAmplitude: Float = 0f,
  onCoreTapped: () -> Unit = {}
) {
  val colors = LocalJarvisColors.current
  val coroutineScope = rememberCoroutineScope()

  // 3D rotation angles controlled by finger gestures
  var yawAngle by remember { mutableFloatStateOf(0f) }
  var pitchAngle by remember { mutableFloatStateOf(0f) }

  // Infinite transitions for rotating mechanical layers
  val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

  // Outer ring rotation (clockwise)
  val outerAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (reactorState == ReactorState.PROCESSING) 3000 else 12000,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "OuterRingAngle"
  )

  // Middle ring rotation (counter-clockwise)
  val middleAngle by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (reactorState == ReactorState.PROCESSING) 2000 else 8000,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "MiddleRingAngle"
  )

  // Inner core pulsing
  val corePulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (reactorState == ReactorState.LISTENING) 600 else 1800,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "CorePulse"
  )

  // Soundwave ripple expansion
  val waveProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "WaveProgress"
  )

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(size)
      .graphicsLayer {
        // Apply 3D perspective rotation from touch gestures
        rotationX = pitchAngle.coerceIn(-35f, 35f)
        rotationY = yawAngle.coerceIn(-35f, 35f)
        cameraDistance = 16f * density
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            change.consume()
            yawAngle += dragAmount.x * 0.4f
            pitchAngle -= dragAmount.y * 0.4f
          },
          onDragEnd = {
            // Spring smoothly back towards center when released
            coroutineScope.launch {
              val startYaw = yawAngle
              val startPitch = pitchAngle
              val anim = androidx.compose.animation.core.Animatable(0f)
              anim.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 200f)) {
                yawAngle = startYaw * (1f - this.value)
                pitchAngle = startPitch * (1f - this.value)
              }
            }
          }
        )
      }
      .pointerInput(Unit) {
        detectTapGestures {
          onCoreTapped()
        }
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(this.size.width / 2f, this.size.height / 2f)
      val maxRadius = (this.size.minDimension / 2f) * 0.95f

      // 1. Hologram Outer Glow Vignette
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            colors.arcGlow.copy(alpha = 0.25f),
            colors.arcGlow.copy(alpha = 0.05f),
            Color.Transparent
          ),
          center = center,
          radius = maxRadius * 1.1f
        ),
        radius = maxRadius * 1.1f,
        center = center
      )

      // 2. Soundwave Ripples when Speaking or Listening
      if (reactorState == ReactorState.LISTENING || reactorState == ReactorState.SPEAKING) {
        val extraWave = if (reactorState == ReactorState.LISTENING) audioAmplitude * 40f else 25f
        for (i in 0..2) {
          val ripplePhase = (waveProgress + i * 0.33f) % 1f
          val waveRadius = maxRadius * (0.6f + ripplePhase * 0.45f) + extraWave
          val alpha = ((1f - ripplePhase) * 0.5f).coerceIn(0f, 1f)
          drawCircle(
            color = colors.arcPrimary.copy(alpha = alpha),
            radius = waveRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
          )
        }
      }

      // 3. Layer 1: Outermost Stator Ring with Compass Indices & Ticks
      drawOuterStatorRing(
        center = center,
        radius = maxRadius,
        rotation = outerAngle,
        primaryColor = colors.arcPrimary,
        dimColor = colors.borderNormal
      )

      // 4. Layer 2: Segmented Arc Ring (Counter-rotating)
      drawSegmentedEnergyRing(
        center = center,
        radius = maxRadius * 0.78f,
        rotation = middleAngle,
        color = colors.arcPrimary,
        accentColor = colors.accentSecondary
      )

      // 5. Layer 3: Triangular Flux Conductors (The iconic Mark III / Mark IV Arc nodes)
      drawFluxConductors(
        center = center,
        radius = maxRadius * 0.60f,
        rotation = outerAngle * 0.5f,
        primaryColor = colors.arcPrimary,
        accentColor = colors.warningGold
      )

      // 6. Layer 4: Central Reactor Power Core with pulsating multi-layer gradient
      val effectivePulse = if (reactorState == ReactorState.LISTENING) {
        corePulse + (audioAmplitude * 0.35f)
      } else {
        corePulse
      }

      drawCentralPowerCore(
        center = center,
        radius = maxRadius * 0.34f * effectivePulse,
        coreColor = colors.arcCore,
        glowColor = colors.arcPrimary,
        accentColor = if (reactorState == ReactorState.PROCESSING) colors.warningGold else colors.textPrimary
      )
    }
  }
}

/**
 * Draws the outermost mechanical ring with tick marks and chevron guides.
 */
private fun DrawScope.drawOuterStatorRing(
  center: Offset,
  radius: Float,
  rotation: Float,
  primaryColor: Color,
  dimColor: Color
) {
  // Outermost boundary ring
  drawCircle(
    color = dimColor,
    radius = radius,
    center = center,
    style = Stroke(width = 1.5.dp.toPx())
  )

  // Rotating tick indicators
  rotate(rotation, center) {
    val tickCount = 36
    val tickAngleStep = 360f / tickCount
    for (i in 0 until tickCount) {
      val angleDeg = i * tickAngleStep
      val angleRad = Math.toRadians(angleDeg.toDouble())
      val isMajor = i % 3 == 0

      val innerDist = if (isMajor) radius - 14.dp.toPx() else radius - 8.dp.toPx()
      val strokeW = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()
      val color = if (isMajor) primaryColor else dimColor

      val startX = center.x + (cos(angleRad) * innerDist).toFloat()
      val startY = center.y + (sin(angleRad) * innerDist).toFloat()
      val endX = center.x + (cos(angleRad) * radius).toFloat()
      val endY = center.y + (sin(angleRad) * radius).toFloat()

      drawLine(
        color = color,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
      )
    }

    // 4 Corner Chevrons / Sci-Fi Arcs
    for (quadrant in 0..3) {
      val startDeg = quadrant * 90f + 15f
      drawArc(
        color = primaryColor,
        startAngle = startDeg,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
      )
    }
  }
}

/**
 * Draws rotating segmented blocks resembling electromagnetic containment coils.
 */
private fun DrawScope.drawSegmentedEnergyRing(
  center: Offset,
  radius: Float,
  rotation: Float,
  color: Color,
  accentColor: Color
) {
  // Guide circle
  drawCircle(
    color = color.copy(alpha = 0.35f),
    radius = radius,
    center = center,
    style = Stroke(width = 2.dp.toPx())
  )

  rotate(rotation, center) {
    val segmentCount = 10
    val sweepAngle = 20f
    val step = 360f / segmentCount

    for (i in 0 until segmentCount) {
      val startAngle = i * step
      val segmentColor = if (i % 2 == 0) color else accentColor
      drawArc(
        color = segmentColor,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
      )
    }
  }
}

/**
 * Draws the 3 triangular flux conductors (Iron Man Mark series signature geometry).
 */
private fun DrawScope.drawFluxConductors(
  center: Offset,
  radius: Float,
  rotation: Float,
  primaryColor: Color,
  accentColor: Color
) {
  rotate(rotation, center) {
    val nodeCount = 3
    val step = 360.0 / nodeCount

    for (i in 0 until nodeCount) {
      val angle = Math.toRadians(i * step)
      val x = center.x + (cos(angle) * radius).toFloat()
      val y = center.y + (sin(angle) * radius).toFloat()

      // Triangular conductor node
      val nodeSize = 18.dp.toPx()
      val path = Path().apply {
        moveTo(x, y - nodeSize)
        lineTo(x + nodeSize * 0.866f, y + nodeSize * 0.5f)
        lineTo(x - nodeSize * 0.866f, y + nodeSize * 0.5f)
        close()
      }

      drawPath(
        path = path,
        color = primaryColor,
        style = Stroke(width = 2.dp.toPx())
      )

      drawCircle(
        color = accentColor,
        radius = 4.dp.toPx(),
        center = Offset(x, y)
      )
    }

    // Connect nodes with an inner equilateral triangle
    val triPath = Path().apply {
      for (i in 0 until nodeCount) {
        val angle = Math.toRadians(i * step)
        val x = center.x + (cos(angle) * radius).toFloat()
        val y = center.y + (sin(angle) * radius).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
      }
      close()
    }

    drawPath(
      path = triPath,
      color = primaryColor.copy(alpha = 0.5f),
      style = Stroke(width = 1.5.dp.toPx())
    )
  }
}

/**
 * Draws the high-energy central power orb with intense radial neon emission.
 */
private fun DrawScope.drawCentralPowerCore(
  center: Offset,
  radius: Float,
  coreColor: Color,
  glowColor: Color,
  accentColor: Color
) {
  // Core radial glow
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        accentColor,
        coreColor,
        glowColor.copy(alpha = 0.6f),
        Color.Transparent
      ),
      center = center,
      radius = radius * 1.4f
    ),
    radius = radius * 1.4f,
    center = center
  )

  // Inner containment ring
  drawCircle(
    color = coreColor,
    radius = radius,
    center = center,
    style = Stroke(width = 4.dp.toPx())
  )

  // Core center high-density plasma orb
  drawCircle(
    color = accentColor,
    radius = radius * 0.45f,
    center = center
  )

  // Micro crosshair in the center
  val crosshairLen = radius * 0.3f
  drawLine(
    color = Color.Black,
    start = Offset(center.x - crosshairLen, center.y),
    end = Offset(center.x + crosshairLen, center.y),
    strokeWidth = 1.5.dp.toPx()
  )
  drawLine(
    color = Color.Black,
    start = Offset(center.x, center.y - crosshairLen),
    end = Offset(center.x, center.y + crosshairLen),
    strokeWidth = 1.5.dp.toPx()
  )
}
