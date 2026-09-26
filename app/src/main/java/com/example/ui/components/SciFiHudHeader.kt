package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.telemetry.DeviceTelemetry
import com.example.ui.theme.LocalJarvisColors

@Composable
fun SciFiHudHeader(
  modifier: Modifier = Modifier,
  telemetry: DeviceTelemetry,
  isTorchActive: Boolean = false,
  onTorchToggle: () -> Unit = {},
  onTelemetryClick: () -> Unit = {}
) {
  val colors = LocalJarvisColors.current

  // Pulsing dot animation for LIVE status
  val infiniteTransition = rememberInfiniteTransition(label = "StatusDotPulse")
  val alphaPulse by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "AlphaPulse"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(colors.surfaceHud.copy(alpha = 0.95f))
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Top Row: Title, Live indicator, and Torch trigger
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Pulsing green/cyan indicator dot
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(colors.healthyEmerald.copy(alpha = alphaPulse))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "MYRAAA // MARK IV",
            style = MaterialTheme.typography.titleMedium.copy(
              color = colors.textPrimary,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp
            )
          )
          Text(
            text = "HOLOGRAPHIC AI INTERFACE",
            style = MaterialTheme.typography.labelSmall.copy(
              color = colors.arcPrimary,
              fontSize = 9.sp
            )
          )
        }
      }

      // Quick Torch / Photon Emitter button
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .border(
            1.dp,
            if (isTorchActive) colors.warningGold else colors.borderNormal,
            RoundedCornerShape(6.dp)
          )
          .background(if (isTorchActive) colors.warningGold.copy(alpha = 0.25f) else Color.Transparent)
          .clickable { onTorchToggle() }
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("torch_toggle_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.FlashlightOn,
            contentDescription = "Torch Protocol",
            tint = if (isTorchActive) colors.warningGold else colors.textMuted,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isTorchActive) "LIGHT ON" else "TORCH",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isTorchActive) colors.warningGold else colors.textMuted,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Bottom Telemetry Chips Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onTelemetryClick() },
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Battery Chip
      TelemetryChip(
        icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
        label = "PWR",
        value = "${telemetry.batteryLevel}%",
        valueColor = if (telemetry.batteryLevel < 20) colors.alertCrimson else colors.arcPrimary
      )

      // 2. RAM Chip
      TelemetryChip(
        icon = Icons.Default.Memory,
        label = "RAM",
        value = "${telemetry.usedRamPercent}%",
        valueColor = if (telemetry.usedRamPercent > 85) colors.warningGold else colors.textPrimary
      )

      // 3. Network Chip
      TelemetryChip(
        icon = Icons.Default.Wifi,
        label = "NET",
        value = if (telemetry.isConnected) "ONLINE" else "OFFLINE",
        valueColor = if (telemetry.isConnected) colors.healthyEmerald else colors.alertCrimson
      )

      // 4. System Uptime Chip
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(4.dp))
          .background(colors.surfaceCard)
          .border(1.dp, colors.borderNormal.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
          .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd
      ) {
        Text(
          text = "UP: ${telemetry.uptimeFormatted}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = colors.textMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
          )
        )
      }
    }
  }
}

@Composable
private fun TelemetryChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
  valueColor: Color
) {
  val colors = LocalJarvisColors.current
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(colors.surfaceCard)
      .border(1.dp, colors.borderNormal.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = valueColor,
      modifier = Modifier.size(12.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$label:",
      style = MaterialTheme.typography.labelSmall.copy(
        color = colors.textMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp
      )
    )
    Spacer(modifier = Modifier.width(3.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.labelSmall.copy(
        color = valueColor,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
      )
    )
  }
}
