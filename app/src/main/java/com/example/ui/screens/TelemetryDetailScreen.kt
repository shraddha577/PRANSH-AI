package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.telemetry.DeviceTelemetry
import com.example.ui.components.SciFiCard
import com.example.ui.theme.LocalJarvisColors

@Composable
fun TelemetryDetailScreen(
  modifier: Modifier = Modifier,
  telemetry: DeviceTelemetry
) {
  val colors = LocalJarvisColors.current
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Text(
      text = "HARDWARE & SYSTEM TELEMETRY",
      style = MaterialTheme.typography.titleMedium.copy(
        color = colors.textPrimary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      )
    )
    Text(
      text = "LIVE SENSOR READINGS FROM MARK IV MOBILE CHASSIS",
      style = MaterialTheme.typography.labelSmall.copy(
        color = colors.arcPrimary,
        fontSize = 9.sp
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // 1. Power Cell & Battery Telemetry
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        TelemetrySectionHeader(
          icon = Icons.Default.BatteryChargingFull,
          title = "ARC POWER CELLS // BATTERY",
          color = colors.arcPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CHARGE LEVEL",
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
          )
          Text(
            text = "${telemetry.batteryLevel}% [${if (telemetry.isCharging) "CHARGING" else "DISCHARGING"}]",
            style = MaterialTheme.typography.labelMedium.copy(
              color = if (telemetry.batteryLevel < 20) colors.alertCrimson else colors.arcPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { telemetry.batteryLevel / 100f },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = if (telemetry.batteryLevel < 20) colors.alertCrimson else colors.arcPrimary,
          trackColor = colors.surfaceHud
        )

        Spacer(modifier = Modifier.height(12.dp))

        TelemetryDataRow("POWER SOURCE", telemetry.chargeSource)
        TelemetryDataRow("TEMPERATURE", "${telemetry.batteryTempCelsius} °C")
        TelemetryDataRow("VOLTAGE", "${telemetry.batteryVoltageVolts} V")
        TelemetryDataRow("CELL HEALTH", telemetry.batteryHealth)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. RAM & Compute Memory Telemetry
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        TelemetrySectionHeader(
          icon = Icons.Default.Memory,
          title = "COMPUTE MEMORY // RAM MATRIX",
          color = colors.accentSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "RAM LOAD",
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
          )
          Text(
            text = "${telemetry.usedRamPercent}% (${telemetry.totalRamMb - telemetry.availRamMb} MB / ${telemetry.totalRamMb} MB)",
            style = MaterialTheme.typography.labelMedium.copy(
              color = colors.textPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { telemetry.usedRamPercent / 100f },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = if (telemetry.usedRamPercent > 85) colors.warningGold else colors.accentSecondary,
          trackColor = colors.surfaceHud
        )

        Spacer(modifier = Modifier.height(12.dp))

        TelemetryDataRow("TOTAL ALLOCATED", "${telemetry.totalRamMb} MB")
        TelemetryDataRow("AVAILABLE BUFFERS", "${telemetry.availRamMb} MB")
        TelemetryDataRow("LOW MEMORY FLAG", if (telemetry.isLowMemory) "ALERT (LOW)" else "NOMINAL")
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Storage Telemetry
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        TelemetrySectionHeader(
          icon = Icons.Default.SdStorage,
          title = "INTERNAL STORAGE // SOLID STATE",
          color = colors.warningGold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val usedStoragePercent = (((telemetry.storageTotalGb - telemetry.storageFreeGb) / telemetry.storageTotalGb) * 100).toInt().coerceIn(0, 100)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "STORAGE CONSUMPTION",
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
          )
          Text(
            text = "$usedStoragePercent% (${telemetry.storageFreeGb} GB FREE)",
            style = MaterialTheme.typography.labelMedium.copy(
              color = colors.warningGold,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { usedStoragePercent / 100f },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = colors.warningGold,
          trackColor = colors.surfaceHud
        )

        Spacer(modifier = Modifier.height(12.dp))

        TelemetryDataRow("CAPACITY", "${telemetry.storageTotalGb} GB")
        TelemetryDataRow("REMAINING", "${telemetry.storageFreeGb} GB")
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 4. Device Architecture Specs
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        TelemetrySectionHeader(
          icon = Icons.Default.Info,
          title = "DEVICE ARCHITECTURE & UPTIME",
          color = colors.healthyEmerald
        )
        Spacer(modifier = Modifier.height(10.dp))

        TelemetryDataRow("CHASSIS / MODEL", telemetry.deviceModel)
        TelemetryDataRow("OPERATING SYSTEM", telemetry.androidVersion)
        TelemetryDataRow("LOGICAL CPU CORES", "${telemetry.cpuCoreCount} CORES")
        TelemetryDataRow("NETWORK TELEMETRY", telemetry.networkType)
        TelemetryDataRow("SYSTEM UPTIME", telemetry.uptimeFormatted)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun TelemetrySectionHeader(
  icon: ImageVector,
  title: String,
  color: Color
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(color.copy(alpha = 0.2f))
        .border(1.dp, color, RoundedCornerShape(4.dp)),
      contentAlignment = Alignment.Center
    ) {
      androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(14.dp)
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.labelMedium.copy(
        color = color,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
    )
  }
}

@Composable
private fun TelemetryDataRow(label: String, value: String) {
  val colors = LocalJarvisColors.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(
        color = colors.textMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp
      )
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        color = colors.textPrimary,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
      )
    )
  }
}
